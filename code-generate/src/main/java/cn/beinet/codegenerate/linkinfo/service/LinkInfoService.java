package cn.beinet.codegenerate.linkinfo.service;

import cn.beinet.codegenerate.controller.dto.NacosDto;
import cn.beinet.codegenerate.controller.dto.RedisDto;
import cn.beinet.codegenerate.controller.dto.SqlDto;
import cn.beinet.codegenerate.linkinfo.controller.dto.LinkInfoDto;
import cn.beinet.codegenerate.linkinfo.service.entity.LinkInfo;
import cn.beinet.codegenerate.repository.ColumnRepository;
import cn.beinet.codegenerate.repository.MySqlExecuteRepository;
import cn.beinet.codegenerate.repository.RedisRepository;
import cn.beinet.codegenerate.util.AESUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用连接信息的服务类，把所有工具所需的mysql、redis、nacos连接信息统一保存，避免前端泄露
 *
 * @author youbl
 * @since 2023/8/15 14:57
 */
@Service
@RequiredArgsConstructor
public class LinkInfoService {
    private final JdbcTemplate jdbcTemplate;

    /**
     * 获取连接信息清单，不返回密码
     *
     * @param type 连接类型
     * @return 清单
     */
    public List<LinkInfoDto> getLinkInfoDto(String type) {
        List<LinkInfo> infos = getLinkInfo(type);

        List<LinkInfoDto> ret = new ArrayList<>(infos.size());
        for (LinkInfo item : infos) {
            LinkInfoDto dto = convertToDto(item, false); // 密码不返回
            ret.add(dto);
        }
        return ret;
    }

    /**
     * 获取连接信息清单，带密码 返回
     *
     * @param type 连接类型
     * @return 清单
     */
    public List<LinkInfo> getLinkInfo(String type) {
        String sql = "SELECT * FROM linkinfo a WHERE a.link_type=? ORDER BY name";
        List<LinkInfo> infos = jdbcTemplate.query(sql, new Object[]{type}, new BeanPropertyRowMapper<>(LinkInfo.class));
        if (infos == null)
            return new ArrayList<>();
        for (LinkInfo info : infos) {
            info.setPwd(descrypt(info.getPwd()));
        }
        return infos;
    }

    private LinkInfoDto convertToDto(LinkInfo item, boolean fillPassword) {
        LinkInfoDto ret = new LinkInfoDto()
                .setAccount(item.getAccount())
                .setAddress(item.getAddress())
                .setCreate_time(item.getCreate_time())
                .setUpdate_time(item.getUpdate_time())
                .setId(item.getId())
                .setInfo(item.getInfo())
                .setLink_type(item.getLink_type())
                .setName(item.getName())
                .setPort(item.getPort())
                .setPwd("");
        if (fillPassword) {
            ret.setPwd(item.getPwd());
        }
        return ret;
    }

    public ColumnRepository getRepository(SqlDto dto) {
        fillLinkInfo(dto);
        return ColumnRepository.getRepository(
                dto.getIp(), dto.getPort(), dto.getUser(), dto.getPwd());
    }

    public MySqlExecuteRepository getExeRepository(SqlDto dto) {
        fillLinkInfo(dto);
        return new MySqlExecuteRepository(
                dto.getIp(), dto.getPort(), dto.getUser(), dto.getPwd(), dto.getDb(), dto.getDbTimeout());
    }

    private void fillLinkInfo(SqlDto dto) {
        if (!StringUtils.hasLength(dto.getName()))
            return;

        LinkInfo info = getLinkInfoByName(dto.getName());
        if (info == null)
            return;
        dto.setIp(info.getAddress())
                .setPort(info.getPort())
                .setUser(info.getAccount())
                .setPwd(descrypt(info.getPwd()));
    }

    public RedisRepository getRedisRepository(RedisDto dto) {
        fillLinkInfo(dto);
        return new RedisRepository(
                dto.getIp(), dto.getPort(), dto.getDb(), dto.getPwd());
    }

    private void fillLinkInfo(RedisDto dto) {
        if (!StringUtils.hasLength(dto.getName()))
            return;

        LinkInfo info = getLinkInfoByName(dto.getName());
        if (info == null)
            return;
        dto.setIp(info.getAddress())
                .setPwd(descrypt(info.getPwd()))
                .setPort(info.getPort());
    }

    /**
     * 根据name获取nacos配置信息填充
     *
     * @param dto dto
     */
    public void fillLinkInfo(NacosDto dto) {
        if (!StringUtils.hasLength(dto.getName()))
            return;
        LinkInfo info = getLinkInfoByName(dto.getName());
        if (info == null)
            return;
        dto.setUrl(info.getAddress())
                .setUser(info.getAccount())
                .setPwd(descrypt(info.getPwd()));
    }


    /**
     * 获取单个连接信息
     *
     * @param name 连接名称
     * @return 连接信息
     */
    private LinkInfo getLinkInfoByName(String name) {
        String sql = "SELECT * FROM linkinfo a WHERE a.name=? LIMIT 1";
        List<LinkInfo> lst = jdbcTemplate.query(sql, new Object[]{name}, new BeanPropertyRowMapper<>(LinkInfo.class));
        if (!lst.isEmpty()) {
            LinkInfo item = lst.get(0);
            // 防止数据库里有空格，导致dns解析失败
            item.setAddress(item.getAddress().trim());
            item.setAccount(item.getAccount().trim());
            item.setPwd(item.getPwd().trim());
            return item;
        }
        return null;
    }

    public int saveInfo(LinkInfoDto dto) {
        String encryptedPwd = encrypt(dto.getPwd());

        String insertSql = "INSERT INTO linkinfo(link_type, name, address, account, pwd, port, info)VALUES(?,?,?,?,?,?,?)";
        return jdbcTemplate.update(insertSql, new Object[]{
                dto.getLink_type(),
                dto.getName(),
                dto.getAddress(),
                dto.getAccount(),
                encryptedPwd,
                dto.getPort(),
                dto.getInfo()
        });
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
