package com.jsh.erp.utils;


/**
 * 验证码图片工具类
 * 提供图形验证码的生成方法，返回 Base64 编码的验证码图片
 *
 * @author jishenghua
 */
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

/**
 * 鐧诲綍楠岃瘉鐮佸伐鍏风被
 */
public class RandImageUtil {

    public static final String key = "JEECG_LOGIN_KEY";

    /**
     * 瀹氫箟鍥惧舰澶у皬
     */
    private static final int width = 105;
    /**
     * 瀹氫箟鍥惧舰澶у皬
     */
    private static final int height = 35;

    /**
     * 瀹氫箟骞叉壈绾挎暟閲?     */
    private static final int count = 200;

    /**
     * 骞叉壈绾跨殑闀垮害=1.414*lineWidth
     */
    private static final int lineWidth = 2;

    /**
     * 鍥剧墖鏍煎紡
     */
    private static final String IMG_FORMAT = "JPEG";

    /**
     * base64 鍥剧墖鍓嶇紑
     */
    private static final String BASE64_PRE = "data:image/jpg;base64,";

    /**
     * 鐩存帴閫氳繃response 杩斿洖鍥剧墖
     * @param response
     * @param resultCode
     * @throws IOException
     */
    public static void generate(HttpServletResponse response, String resultCode) throws IOException {
        BufferedImage image = getImageBuffer(resultCode);
        // 杈撳嚭鍥捐薄鍒伴〉闈?        ImageIO.write(image, IMG_FORMAT, response.getOutputStream());
    }

    /**
     * 鐢熸垚base64瀛楃涓?     * @param resultCode
     * @return
     * @throws IOException
     */
    public static String generate(String resultCode) throws IOException {
        BufferedImage image = getImageBuffer(resultCode);

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        //鍐欏叆娴佷腑
        ImageIO.write(image, IMG_FORMAT, byteStream);
        //杞崲鎴愬瓧鑺?        byte[] bytes = byteStream.toByteArray();
        //杞崲鎴恇ase64涓?        String base64 = Base64.getEncoder().encodeToString(bytes).trim();
        base64 = base64.replaceAll("\n", "").replaceAll("\r", "");//鍒犻櫎 \r\n

        //鍐欏埌鎸囧畾浣嶇疆
        //ImageIO.write(bufferedImage, "png", new File(""));

        return BASE64_PRE+base64;
    }

    private static BufferedImage getImageBuffer(String resultCode){
        // 鍦ㄥ唴瀛樹腑鍒涘缓鍥捐薄
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 鑾峰彇鍥惧舰涓婁笅鏂?        final Graphics2D graphics = (Graphics2D) image.getGraphics();
        // 璁惧畾鑳屾櫙棰滆壊
        graphics.setColor(Color.WHITE); // ---1
        graphics.fillRect(0, 0, width, height);
        // 璁惧畾杈规棰滆壊
//		graphics.setColor(getRandColor(100, 200)); // ---2
        graphics.drawRect(0, 0, width - 1, height - 1);

        final Random random = new Random();
        // 闅忔満浜х敓骞叉壈绾匡紝浣垮浘璞′腑鐨勮璇佺爜涓嶆槗琚叾瀹冪▼搴忔帰娴嬪埌
        for (int i = 0; i < count; i++) {
            graphics.setColor(getRandColor(150, 200)); // ---3

            final int x = random.nextInt(width - lineWidth - 1) + 1; // 淇濊瘉鐢诲湪杈规涔嬪唴
            final int y = random.nextInt(height - lineWidth - 1) + 1;
            final int xl = random.nextInt(lineWidth);
            final int yl = random.nextInt(lineWidth);
            graphics.drawLine(x, y, x + xl, y + yl);
        }
        // 鍙栭殢鏈轰骇鐢熺殑璁よ瘉鐮?        for (int i = 0; i < resultCode.length(); i++) {
            // 灏嗚璇佺爜鏄剧ず鍒板浘璞′腑,璋冪敤鍑芥暟鍑烘潵鐨勯鑹茬浉鍚岋紝鍙兘鏄洜涓虹瀛愬お鎺ヨ繎锛屾墍浠ュ彧鑳界洿鎺ョ敓鎴?            // graphics.setColor(new Color(20 + random.nextInt(130), 20 + random
            // .nextInt(130), 20 + random.nextInt(130)));
            // 璁剧疆瀛椾綋棰滆壊
            graphics.setColor(Color.BLACK);
            // 璁剧疆瀛椾綋鏍峰紡
//			graphics.setFont(new Font("Arial Black", Font.ITALIC, 18));
            graphics.setFont(new Font("Times New Roman", Font.BOLD, 24));
            // 璁剧疆瀛楃锛屽瓧绗﹂棿璺濓紝涓婅竟璺?            graphics.drawString(String.valueOf(resultCode.charAt(i)), (23 * i) + 8, 26);
        }
        // 鍥捐薄鐢熸晥
        graphics.dispose();
        return image;
    }

    private static Color getRandColor(int fc, int bc) { // 鍙栧緱缁欏畾鑼冨洿闅忔満棰滆壊
        final Random random = new Random();
        if (fc > 255) {
            fc = 255;
        }
        if (bc > 255) {
            bc = 255;
        }

        final int r = fc + random.nextInt(bc - fc);
        final int g = fc + random.nextInt(bc - fc);
        final int b = fc + random.nextInt(bc - fc);

        return new Color(r, g, b);
    }
}
