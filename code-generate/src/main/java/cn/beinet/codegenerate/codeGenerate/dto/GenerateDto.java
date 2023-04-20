package cn.beinet.codegenerate.codeGenerate.dto;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 新类
 *
 * @author youbl
 * @date 2023/4/20 10:13
 */
@Data
public class GenerateDto {
    private String packageName;
    private String database;
    private String[] tables;
    private String removePrefix;
}
