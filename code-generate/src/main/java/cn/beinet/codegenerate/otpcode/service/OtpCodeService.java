package cn.beinet.codegenerate.otpcode.service;

import cn.beinet.codegenerate.otpcode.controller.dto.OtpCodeDto;
import cn.beinet.codegenerate.otpcode.service.entity.OtpCode;
import cn.beinet.codegenerate.util.AESUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OtpCode服务类，懒得引用ORM了
 *
 * @author youbl
 * @since 2023/8/11 13:46
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OtpCodeService {
    private final JdbcTemplate jdbcTemplate;

    /**
     * 根据用户名，读取并生成otpcode清单
     *
     * @param username 用户
     * @return code清单
     */
    public Map<String, String> getOtpCodesById(int id, int codeNum, String username) {
        OtpCode code = getRecordById(id);
        if (code == null || !username.equals(code.getUsername()))
            return new HashMap<>();
        return countCode(code.getSecure(), codeNum);
    }

    /**
     * 根据用户名，读取并生成otpcode清单
     *
     * @param username 用户
     * @return code清单
     */
    public List<OtpCodeDto> getOtpCodesByUser(int codeNum, String username) {
        List<OtpCode> codes = getSavedRecord(username);
        if (codes == null || codes.size() <= 0)
            return new ArrayList<>();

        List<OtpCodeDto> ret = new ArrayList<>(codes.size());
        for (OtpCode item : codes) {
            Map<String, String> code = countCode(item.getSecure(), codeNum);

            OtpCodeDto dto = new OtpCodeDto()
                    .setId(item.getId())
                    .setCode(code)
                    .setCreateTime(item.getCreate_time())
                    .setTitle(item.getTitle())
                    .setUsername(item.getUsername())
                    .setUrl(item.getUrl())
                    .setMemo(item.getMemo())
                    .setSecure(""); // 密钥默认不返回
            ret.add(dto);
        }
        return ret;
    }

    /**
     * 保存一个密钥
     *
     * @param dto 用户信息
     * @return 影响行数
     */
    public int saveOtpCode(OtpCodeDto dto) {
        String encryptedPwd = encrypt(dto.getSecure());

        String insertSql = "INSERT INTO otpcode (username,title,secure,url,memo)VALUES(?,?,?,?,?)";
        return jdbcTemplate.update(insertSql, new Object[]{
                dto.getUsername(),
                dto.getTitle(),
                encryptedPwd,
                dto.getUrl(),
                dto.getMemo(),
        });
    }

    /**
     * 按id和用户名，删除密钥
     *
     * @param id       记录id
     * @param username 所属用户，防止删除别人的数据
     * @return 影响行数
     */
    public int delOtpCode(int id, String username) {
        String insertSql = "DELETE FROM otpcode WHERE id=? and username=?";
        return jdbcTemplate.update(insertSql, new Object[]{
                id, username
        });
    }

    /**
     * 生成二维码url并返回
     *
     * @return url
     */
    public String getQRCodeUrl(int id, String username) {
        OtpCode codeObj = getRecordById(id);
        if (codeObj == null || !codeObj.getUsername().equals(username)) {
            throw new RuntimeException("指定的记录不存在，或不是您的数据");
        }

        String secure = descrypt(codeObj.getSecure());
        return OtpCodeGeneratorTool.getQRBarcode(codeObj.getTitle(), username, secure);
    }

    private Map<String, String> countCode(String secure, int codeNum) {
        String descryptedSecure;
        try {
            // 解密密钥后，进行code计算生成
            descryptedSecure = descrypt(secure);
        } catch (Exception exp) {
            log.error("{} 解密失败: {}", secure, exp.getMessage());
            return new HashMap<>();
        }
        return OtpCodeGeneratorTool.countCodeStr(descryptedSecure, codeNum);
    }

    /**
     * 从数据库读取保存的密钥清单
     *
     * @param username 用户名
     * @return 密钥清单
     */
    private List<OtpCode> getSavedRecord(String username) {
        String sql = "SELECT * FROM otpcode a WHERE a.username=? ORDER BY title";
        // List<Map<String, Object>> codes = jdbcTemplate.queryForList(sql, new Object[]{username});
        List<OtpCode> codes = jdbcTemplate.query(sql, new Object[]{username}, new BeanPropertyRowMapper<>(OtpCode.class));
        return codes;
    }

    private OtpCode getRecordById(int id) {
        String sql = "SELECT * FROM otpcode a WHERE a.id=?";
        List<OtpCode> lst = jdbcTemplate.query(sql, new Object[]{id}, new BeanPropertyRowMapper<>(OtpCode.class));
        if (lst.size() > 0)
            return lst.get(0);
        return null;
    }

    // 入库前要加密
    private String encrypt(String sourceStr) {
        if (StringUtils.hasLength(sourceStr))
            return AESUtil.encrypt(sourceStr);
        return sourceStr;
    }

    // 出库后要解密
    private String descrypt(String encryptedStr) {
        if (StringUtils.hasLength(encryptedStr))
            return AESUtil.decrypt(encryptedStr);
        return encryptedStr;
    }
}
