package cn.beinet.codegenerate.otpcode.controller;

import cn.beinet.codegenerate.configs.AuthDetails;
import cn.beinet.codegenerate.otpcode.controller.dto.OtpCodeDto;
import cn.beinet.codegenerate.otpcode.service.OtpCodeService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * @param codeNum   要生成的验证码个数,默认3
     * @param loginInfo 登录用户
     * @return 列表
     */
    @GetMapping("list")
    public List<OtpCodeDto> getCode(@RequestParam(required = false) int codeNum, AuthDetails loginInfo) {
        String username = loginInfo == null ? null : loginInfo.getAccount();
        Assert.hasLength(username, "未登录");
        return otpCodeService.getOtpCodesByUser(codeNum, username);
    }

    /**
     * 返回指定记录的实时otpcode
     *
     * @param codeNum   要生成的验证码个数,默认3
     * @param loginInfo 登录用户
     * @return 列表
     */
    @GetMapping("list/{id}")
    public Map<String, String> refreshCode(@RequestParam(required = false) int codeNum, @PathVariable int id, AuthDetails loginInfo) {
        String username = loginInfo == null ? null : loginInfo.getAccount();
        Assert.hasLength(username, "未登录");
        return otpCodeService.getOtpCodesById(id, codeNum, username);
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
        Assert.notNull(dto.getUrl(), "url不能为null");
        Assert.notNull(dto.getMemo(), "备注不能为null");

        String username = loginInfo == null ? null : loginInfo.getAccount();
        Assert.notNull(username, "未登录");
        dto.setUsername(username);
        return otpCodeService.saveOtpCode(dto);
    }

    /**
     * 删除当前登录用户的一条密钥记录
     *
     * @param id        记录id
     * @param loginInfo 登录用户
     * @return 影响行数
     */
    @DeleteMapping("")
    public int delCode(@RequestParam int id, AuthDetails loginInfo) {
        String username = loginInfo == null ? null : loginInfo.getAccount();
        Assert.notNull(username, "未登录");
        return otpCodeService.delOtpCode(id, username);
    }

    /**
     * 生成OTPCode密钥的二维码图片并返回
     *
     * @param id        记录id
     * @param loginInfo 登录信息
     * @param response  响应上下文
     */
    @GetMapping("export")
    @SneakyThrows
    public void getQRCodeImg(@RequestParam int id, AuthDetails loginInfo, HttpServletResponse response) {
        String qrCodeUrl = otpCodeService.getQRCodeUrl(id, loginInfo.getAccount());

        // 画图并返回
        // 下面是绘制二维码，并输出到响应流
        Map<EncodeHintType, Object> hints = new HashMap<>();
        //编码
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        //边框距
        hints.put(EncodeHintType.MARGIN, 1);

        //I've decided to generate QRCode on backend site
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeUrl, BarcodeFormat.QR_CODE, 400, 400, hints);

        //Simple writing to outputstream
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        }
    }
}
