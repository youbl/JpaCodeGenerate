package cn.beinet.codegenerate.dataClean;

import cn.beinet.codegenerate.dataClean.configDal.CleanConfigDal;
import cn.beinet.codegenerate.dataClean.configDal.entity.CleanConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据清理配置的接口类
 *
 * @author youbl
 * @since 2024/1/17 11:35
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class CleanupController {
    private final CleanConfigDal cleanConfigDal;

    @GetMapping("dataClean")
    public List<CleanConfig> getConfig() {
        return cleanConfigDal.getAllConfig(false);
    }

    @PostMapping("dataClean")
    public int saveConfig(@RequestBody CleanConfig config) {
        return cleanConfigDal.saveConfig(config);
    }

    @PostMapping("dataClean/status")
    public int changeStatus(@RequestParam int id, @RequestParam boolean status) {
        return cleanConfigDal.changeStatus(id, status);
    }
}
