package cn.beinet.codegenerate.util;


import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 生成图形验证码的类
 *
 * @author youbl
 * @since 2023/9/2 11:26
 */
@Component
public class ImgCodeUtil {

    private final int img_width = 136;            // 验证码图片的长
    private final int blank_width = 8;        // 有时字符会超出范围，这个用于留白的宽度
    private final int img_height = 46;            // 验证码图片的宽
    private final int blank_height = 3;       // 有时字符会超出范围，这个用于留白的高度


    //private String[] fontNames = {"宋体", "华文楷体", "黑体", "微软雅黑", "楷体_GB2312"};   //字体数组
    //字体数组
    //private String[] fontNames = {"Arial"};// {"Georgia"}; Georgia默认Centos上没有，会报异常：sun.awt.fontconfiguration.getversion nullpointerexception
    private final String[] fontNames = {"DejaVu Sans"};  // 很多linux也没有Arial字体，换这个试试

    private final char[] RND_STR_SOURCE = "abcdefghjkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private final char[] RND_NUM_SOURCE = "0123456789".toCharArray();


    private ThreadLocalRandom getRnd() {
        return ThreadLocalRandom.current();
    }

    /**
     * 生成指定长度的随机数字串
     *
     * @param len 生成字符串的长度
     * @return 随机数字串
     */
    public String getRndNum(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            //while (sb.length() < len) {
            int idx = getRnd().nextInt(RND_NUM_SOURCE.length);
            sb.append(RND_NUM_SOURCE[idx]);
        }
        return sb.toString();
    }

    /**
     * 生成指定长度的随机字符串
     *
     * @param len 生成字符串的长度
     * @return 随机字符串
     */
    public String getRndTxt(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            //while (sb.length() < len) {
            int idx = getRnd().nextInt(RND_STR_SOURCE.length);
            sb.append(RND_STR_SOURCE[idx]);
        }
        String ret = sb.toString();
        System.out.println(ret);
        return ret;
    }


    /**
     * 获取验证码图片字符串
     *
     * @param text 难码
     * @return 图片的base64流
     */
    @SneakyThrows
    public String getImageBase64(String text) {
        BufferedImage img = getImage(text);
        byte[] bytes;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            ImageIO.write(img, "JPEG", stream);
            bytes = stream.toByteArray();
        }
        String base64 = Base64.getEncoder().encodeToString(bytes).trim();//转换成base64串
        return base64.replaceAll("[\\r\\n]", "");
    }

    /**
     * 获取验证码图片对象
     *
     * @param text 难码
     * @return 图片
     */
    private BufferedImage getImage(String text) {
        //创建图片缓冲区
        BufferedImage image = new BufferedImage(img_width, img_height, BufferedImage.TYPE_INT_RGB);
        //获取画笔
        Graphics2D graphic = (Graphics2D) image.getGraphics(); //获取画笔

        try {
            fillImage(graphic);     // 填充随机底色
            drawTxt(graphic, text);
            drawLine(graphic, 5, 10); // 画随机线
            drawPoint(graphic, 20, 100); // 画随机点
            return image;
        } finally {
            if (graphic != null)
                graphic.dispose();
        }
    }

    /**
     * 使用画笔，绘制一串文本字符串
     *
     * @param graphic 画笔
     * @param text    文本
     */
    private void drawTxt(Graphics2D graphic, String text) {
        // 循环绘制
        for (int i = 0; i < text.length(); i++) {
            String chr = text.substring(i, i + 1);
            graphic.setFont(randomFont());           //设置随机字体
            graphic.setColor(randomColor());         //设置随机颜色

            //定义字符的x,y坐标
            int x = countX(i, text.length());
            int y = countY(i, text.length());
            // System.out.println(x + "---" + y);
            {
                // 用于倾斜绘制的对象
                AffineTransform transform = new AffineTransform();
                // 倾斜度数
                int radians = getRnd().nextInt(-40, 40);
                // 这里的x和y，是旋转点的坐标
                transform.rotate(Math.toRadians(radians), x, y);
                graphic.setTransform(transform);
            }

            graphic.drawString(chr, x, y);
        }
    }

    // 计算第n个字符，在画布上的x坐标
    private int countX(int chIdx, int txtLen) {
        int realWidth = img_width - blank_width * 2;
        // rndX用于定义随机起始位置差值
        int rndX = chIdx > 0 ? getRnd().nextInt(-3, 4) : 3;
        int x = (int) (chIdx * 1.0F * realWidth / txtLen) + rndX;   //定义字符的x坐标
        return x + blank_width;
    }

    // 计算第n个字符，在画布上的y坐标
    private int countY(int chIdx, int txtLen) {
        int realHeight = img_height - blank_height * 2;
        // y坐标，定义为随机高度
        int y = realHeight - getRnd().nextInt(4, realHeight / 2);
        return y + blank_height;
    }

    /**
     * 获取随机颜色
     *
     * @return 颜色
     */
    private Color randomColor() {
        int r = getRnd().nextInt(225);  //这里为什么是225，因为当r，g，b都为255时，即为白色，为了好辨认，需要颜色深一点。
        int g = getRnd().nextInt(225);
        int b = getRnd().nextInt(225);
        return new Color(r, g, b);            //返回一个随机颜色
    }

    /**
     * 获取随机字体
     *
     * @return 字体
     */
    private Font randomFont() {
        int index = getRnd().nextInt(fontNames.length);  //获取随机的字体
        String fontName = fontNames[index];
        int style = getRnd().nextInt(4);         //随机获取字体的样式，0是无样式，1是加粗，2是斜体，3是加粗加斜体
        int size = getRnd().nextInt(10) + 24;    //随机获取字体的大小
        return new Font(fontName, style, size);   //返回一个随机的字体
    }

    /**
     * 画干扰线，验证码干扰线用来防止计算机解析图片
     *
     * @param graphic 画笔
     * @param minNum  干扰线最小条数
     * @param maxNum  干扰线最大条数
     */
    private void drawLine(Graphics2D graphic, int minNum, int maxNum) {
        int num = getRnd().nextInt(minNum, maxNum); //定义干扰线的数量
        for (int i = 0; i < num; i++) {
            int x1 = getRnd().nextInt(img_width / 2);
            int y1 = getRnd().nextInt(img_height);
            int x2 = getRnd().nextInt(img_width / 2, img_width);
            int y2 = getRnd().nextInt(img_height);
            graphic.setColor(randomColor());
            graphic.drawLine(x1, y1, x2, y2);
        }
    }


    /**
     * 画干扰点，验证码干扰点用来防止计算机解析图片
     *
     * @param graphic 画笔
     * @param minNum  干扰点最小条数
     * @param maxNum  干扰点最大条数
     */
    private void drawPoint(Graphics2D graphic, int minNum, int maxNum) {
        int num = getRnd().nextInt(minNum, maxNum); //随机数量
        for (int i = 0; i < num; i++) {
            int x1 = getRnd().nextInt(img_width);
            int y1 = getRnd().nextInt(img_height);
            graphic.setColor(randomColor());
            int width = i % 2 + 1;
            graphic.fillOval(x1, y1, width, width);
        }
    }

    /**
     * 使用画笔填充图片底色
     *
     * @param graphics 画笔
     */
    private void fillImage(Graphics2D graphics) {
        //设置背景色随机
        graphics.setColor(new Color(255, 255, getRnd().nextInt(245) + 10));
        graphics.fillRect(0, 0, img_width, img_height);
    }

}
