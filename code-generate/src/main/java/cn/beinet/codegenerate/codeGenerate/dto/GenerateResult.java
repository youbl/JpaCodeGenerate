package cn.beinet.codegenerate.codeGenerate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/4/20 10:13
 */
@Data
@AllArgsConstructor
public class GenerateResult {
    /**
     * 生成的文件名
     */
    private String fileName;
    /**
     * 生成的文件内容
     */
    private String content;
}
