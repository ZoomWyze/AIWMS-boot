package com.jsh.erp.utils;


/**
 * 拼音工具类
 * 提供汉字转拼音的方法，用于商品名称的助记码生成
 *
 * @author jishenghua
 */
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * @Author jishenghua
 * @Date 2024-01-08 23:03
 */
@Slf4j
public class PinYinUtil {

    public static String getFirstLettersLo(String ChineseLanguage) {
        return getFirstLetters(ChineseLanguage, HanyuPinyinCaseType.LOWERCASE);
    }

    public static String getFirstLetters(String chineseLanguage, HanyuPinyinCaseType caseType) {
        char[] cl_chars = chineseLanguage.trim().toCharArray();
        StringBuilder pinyin = new StringBuilder();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        // 杈撳嚭鎷奸煶鍏ㄩ儴澶у啓
        defaultFormat.setCaseType(caseType);
        // 涓嶅甫澹拌皟
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        try {
            for (char cl_char : cl_chars) {
                String str = String.valueOf(cl_char);
                if (str.matches("[\u4e00-\u9fa5]+")) {
                    // 濡傛灉瀛楃鏄腑鏂?鍒欏皢涓枃杞负姹夎鎷奸煶,骞跺彇绗竴涓瓧姣?                    pinyin.append(PinyinHelper.toHanyuPinyinStringArray(cl_char, defaultFormat)[0].substring(0, 1));
                } else if (str.matches("[0-9]+")) {
                    // 濡傛灉瀛楃鏄暟瀛?鍙栨暟瀛?                    pinyin.append(cl_char);
                } else if (str.matches("[a-zA-Z]+")) {
                    // 濡傛灉瀛楃鏄瓧姣?鍙栧瓧姣?                    pinyin.append(cl_char);
                } else {
                    // 鍚﹀垯涓嶈浆鎹?                    //濡傛灉鏄爣鐐圭鍙风殑璇濓紝甯︾潃
                    pinyin.append(cl_char);
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            log.error(chineseLanguage + "杞嫾闊冲け璐ワ紒", e);
        }
        return pinyin.toString();
    }
}
