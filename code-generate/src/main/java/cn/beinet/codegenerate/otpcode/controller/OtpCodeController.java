package cn.beinet.codegenerate.otpcode.controller;

import cn.beinet.codegenerate.configs.AuthDetails;
import cn.beinet.codegenerate.otpcode.controller.dto.OtpCodeDto;
import cn.beinet.codegenerate.otpcode.service.OtpCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OtpCode保存和验证码生成接口
 *
 * @author youbl
 * @since 2023/8/11 13:32
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("otpcode")
public class OtpCodeController {
    private final OtpCodeService otpCodeService;

    /**
     * 返回当前登录用户的所有实时otpcode
     *
     * @param loginInfo 登录用户
     * @return 列表
     */
    @GetMapping("list")
    public List<OtpCodeDto> getCode(AuthDetails loginInfo) {
        String username = loginInfo == null ? null : loginInfo.getAccount();
        Assert.hasLength(username, "未登录");
        return otpCodeService.getOtpCodesByUser(username);
    }

    /**
     * 为当前登录用户，保存一条otpcode密钥记录
     *
     * @param dto       记录
     * @param loginInfo 登录用户
     * @return 影响行数
     */
    @PostMapping("")
    public int saveCode(@RequestBody OtpCodeDto dto, AuthDetails loginInfo) {
        Assert.notNull(dto, "信息不能为空");
        Assert.hasLength(dto.getTitle(), "标题不能为空");
        Assert.hasLength(dto.getSecure(), "密钥不能为空");

        String username = loginInfo == null ? null : loginInfo.getAccount();
        Assert.notNull(username, "未登录");
        dto.setUsername(username);
        return otpCodeService.saveOtpCode(dto);
    }
}
