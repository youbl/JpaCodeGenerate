package cn.beinet.codegenerate.linkinfo.service;

import cn.beinet.codegenerate.controller.dto.SqlDto;
import cn.beinet.codegenerate.linkinfo.controller.dto.LinkInfoDto;
import cn.beinet.codegenerate.linkinfo.service.entity.LinkInfo;
import cn.beinet.codegenerate.repository.ColumnRepository;
import cn.beinet.codegenerate.repository.MySqlExecuteRepository;
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
     * 获取连接信息清单
     *
     * @param type 连接类型
     * @return 清单
     */
    public List<LinkInfoDto> getLinkInfo(String type) {
        String sql = "SELECT * FROM linkinfo a WHERE a.link_type=? ORDER BY name";
        List<LinkInfo> infos = jdbcTemplate.query(sql, new Object[]{type}, new BeanPropertyRowMapper<>(LinkInfo.class));
        if (infos == null || infos.size() <= 0)
            return new ArrayList<>();

        List<LinkInfoDto> ret = new ArrayList<>(infos.size());
        for (LinkInfo item : infos) {
            LinkInfoDto dto = new LinkInfoDto()
                    .setAccount(item.getAccount())
                    .setAddress(item.getAddress())
                    .setCreate_time(item.getCreate_time())
                    .setUpdate_time(item.getUpdate_time())
                    .setId(item.getId())
                    .setInfo(item.getInfo())
                    .setLink_type(item.getLink_type())
                    .setName(item.getName())
                    .setPort(item.getPort())
                    .setPwd(""); // 密码不返回
            ret.add(dto);
        }
        return ret;
    }

    public ColumnRepository getRepository(SqlDto dto) {
        fillLinkInfo(dto);
        return ColumnRepository.getRepository(
                dto.getIp(), 3306, dto.getUser(), dto.getPwd());
    }

    public MySqlExecuteRepository getExeRepository(SqlDto dto) {
        fillLinkInfo(dto);
        return new MySqlExecuteRepository(
                dto.getIp(), 3306, dto.getUser(), dto.getPwd(), dto.getDb());
    }

    private void fillLinkInfo(SqlDto dto) {
        if (!StringUtils.hasLength(dto.getName()))
            return;

        LinkInfo info = getLinkInfoByName(dto.getName());
        if (info == null)
            return;
        dto.setIp(info.getAddress())
                .setUser(info.getAccount())
                .setPwd(info.getPwd());
    }

    /**
     * 获取单个连接信息
     *
     * @param name 连接名称
     * @return 连接信息
     */
    private LinkInfo getLinkInfoByName(String name) {
        String sql = "SELECT * FROM linkinfo a WHERE a.name=? LIMIT 1";
        return jdbcTemplate.queryForObject(sql, new Object[]{name}, new BeanPropertyRowMapper<>(LinkInfo.class));
    }

    public int saveInfo(LinkInfoDto dto) {
        String insertSql = "INSERT INTO linkinfo(link_type, name, address, account, pwd, port, info)VALUES(?,?,?,?,?,?,?)";
        return jdbcTemplate.update(insertSql, new Object[]{
                dto.getLink_type(),
                dto.getName(),
                dto.getAddress(),
                dto.getAccount(),
                dto.getPwd(),
                dto.getPort(),
                dto.getInfo()
        });
    }
}
