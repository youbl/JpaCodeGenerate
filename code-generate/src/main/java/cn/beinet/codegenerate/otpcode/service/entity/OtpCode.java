package cn.beinet.codegenerate.otpcode.service.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 新类
 *
 * @author youbl
 * @since 2023/8/14 16:00
 */
@Data
public class OtpCode {
    private int id;
    private String username;
    private String title;
    private String secure;
    private String memo;
    private String url;
    private LocalDateTime create_time;
}
