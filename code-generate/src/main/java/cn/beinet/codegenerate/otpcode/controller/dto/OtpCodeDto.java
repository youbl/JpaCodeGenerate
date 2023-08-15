package cn.beinet.codegenerate.otpcode.controller.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/8/11 13:34
 */
@Data
@Accessors(chain = true)
public class OtpCodeDto {
    /**
     * 用户名
     */
    private String username;
    /**
     * OtpCode保存的标题
     */
    private String title;
    /**
     * 当前时间，生成的OtpCode
     */
    private String code;

    /**
     * 密钥，仅用于保存，不返回
     */
    private String secure;

    /**
     * 这个标题数据，保存时间
     */
    private LocalDateTime createTime;
}
