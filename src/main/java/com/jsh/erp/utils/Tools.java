package com.jsh.erp.utils;


/**
 * 通用工具类
 * 提供各种杂项工具方法（日期格式化、数字计算、ID 生成等）
 *
 * @author jishenghua
 */
import org.springframework.util.StringUtils;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 宸ュ叿绫? *
 * @author jishenghua  qq:7-5-2-7-1-8-9-2-0
 */
public class Tools {
    /**
     * 鑾峰緱32浣嶅敮涓€搴忓垪鍙?     *
     * @return 32涓篒D瀛楃涓?     */
    public static String getUUID_32() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 鑾峰緱褰撳ぉ鏃堕棿锛屾牸寮忎负yyyy-MM-dd
     *
     * @return 鏍煎紡鍖栧悗鐨勬棩鏈熸牸寮?     */
    public static String getNow() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    /**
     * 鑾峰彇鏄ㄥぉ鐨勬棩鏈熷瓧绗︿覆
     * @return
     */
    public static String getYesterday(){
        Date date=new Date();//鍙栨椂闂?        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        //鎶婃棩鏈熷線鍚庡鍔犱竴澶?鏁存暟寰€鍚庢帹,璐熸暟寰€鍓嶇Щ鍔?1:琛ㄧず鏄庡ぉ銆?1锛氳〃绀烘槰澶╋紝0锛氳〃绀轰粖澶?
        calendar.add(Calendar.DATE,-1);
        //杩欎釜鏃堕棿灏辨槸鏃ユ湡寰€鍓嶆帹涓€澶╃殑缁撴灉
        date=calendar.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }

    /**
     * 鑾峰彇褰撳勾鐨勭涓€澶?     * @return
     */
    public static String getYearBegin(){
        String yearStr = new SimpleDateFormat("yyyy").format(new Date());
        return yearStr + "-01-01";
    }

    /**
     * 鑾峰彇褰撳勾鐨勬渶鍚庝竴澶?     * @return
     */
    public static String getYearEnd(){
        String yearStr = new SimpleDateFormat("yyyy").format(new Date());
        return yearStr + "-12-31";
    }

    /**
     * 鑾峰彇褰撳墠鏈?yyyy-MM
     *
     * @return
     */
    public static String getCurrentMonth() {
        return new SimpleDateFormat("yyyy-MM").format(new Date());
    }

    /**
     * 鑾峰緱鎸囧畾鏃堕棿锛屾牸寮忎负yyyy-MM-dd HH:mm:ss鎴杫yyy-MM-dd
     *
     * @return 鏍煎紡鍖栧悗鐨勬棩鏈熸牸寮?     */
    public static String dateToStr(Date date, String format) {
        if(date!=null) {
            return new SimpleDateFormat(format).format(date);
        } else {
            return "";
        }
    }

    /**
     * 灏嗘棩鏈熺殑瀛楃涓叉牸寮忚浆涓烘椂闂存牸寮?     * @param dateString
     * @return
     * @throws ParseException
     */
    public static Date strToDate(String dateString) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 鏃ユ湡鏍煎紡
        return formatter.parse(dateString); // 瀛楃涓茶浆鎹负Date
    }
    /**
     * 鑾峰彇鎸囧畾鏃ユ湡鏍煎紡 yyyy-MM-dd
     *
     * @return
     */
    public static String parseDateToStr(Date date) {
        if(date!=null) {
            return new SimpleDateFormat("yyyy-MM-dd").format(date);
        } else {
            return "";
        }
    }

    /**
     * 鑾峰緱褰撳ぉ鏃堕棿锛屾牸寮忎负yyyyMMddHHmmss
     *
     * @return 鏍煎紡鍖栧悗鐨勬棩鏈熸牸寮?     */
    public static String getNow2(Date date) {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(date);
    }

    /**
     * 鑾峰緱褰撳ぉ鏃堕棿锛屾牸寮忎负yyyy-MM-dd HH:mm:ss
     *
     * @return 鏍煎紡鍖栧悗鐨勬棩鏈熸牸寮?     */
    public static String getNow3() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /**
     * 鑾峰緱鎸囧畾鏃堕棿锛屾牸寮忎负yyyy-MM-dd HH:mm:ss
     *
     * @return 鏍煎紡鍖栧悗鐨勬棩鏈熸牸寮?     */
    public static String getCenternTime(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    public static String parseDayToTime(String day, String timeStr) {
        if(StringUtil.isNotEmpty(day)){
            return day + timeStr;
        } else {
            return null;
        }
    }

    /**
     * 鑾峰緱鎸囧畾鏃堕棿锛屾牸寮忎负mm:ss
     *
     * @return 鏍煎紡鍖栧悗鐨勬棩鏈熸牸寮?     */
    public static String getTimeInfo(Date date) {
        return new SimpleDateFormat("mm:ss").format(date);
    }

    /**
     * 鑾峰彇褰撳墠鏃ユ湡鏄槦鏈熷嚑
     * return 鏄熸湡鍑?     */
    public static String getWeekDay() {
        Calendar c = Calendar.getInstance(Locale.CHINA);
        c.setTime(new Date());
        int day = c.get(Calendar.DAY_OF_WEEK);
        String weekDay = "";
        switch (day) {
            case 1:
                weekDay = "鏄熸湡鏃?;
                break;
            case 2:
                weekDay = "鏄熸湡涓€";
                break;
            case 3:
                weekDay = "鏄熸湡浜?;
                break;
            case 4:
                weekDay = "鏄熸湡涓?;
                break;
            case 5:
                weekDay = "鏄熸湡鍥?;
                break;
            case 6:
                weekDay = "鏄熸湡浜?;
                break;
            case 7:
                weekDay = "鏄熸湡鍏?;
                break;
            default:
                break;
        }
        return weekDay;
    }

    /**
     * 鍒ゆ柇瀛楃涓叉槸鍚﹀叏閮ㄤ负鏁板瓧
     *
     * @param checkStr
     * @return boolean鍊?     */
    public static boolean checkStrIsNum(String checkStr) {
        if (checkStr == null || checkStr.length() == 0)
            return false;
        return Pattern.compile("^[0-9]*.{1}[0-9]*$").matcher(checkStr).matches();
//		 return Pattern.compile("锛歗[0-9]+(.[0-9])*$").matcher(checkStr).matches();
    }

    /**
     * 鑾峰緱鍓嶄竴澶╃殑鏃堕棿
     *
     * @return 鍓嶄竴澶╂棩鏈?     */
    public static String getPreviousDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return new SimpleDateFormat("yyyy-MM").format(cal.getTime());
    }

    /**
     * 鑾峰彇褰撳墠鏈堜唤鐨勫墠6涓湀(鍚綋鍓嶆湀)
     * @param size  鏈堟暟
     * @return
     */
    public static List<String> getLastMonths(int size) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        List<String> list = new ArrayList(size);
        for (int i=0;i<size;i++) {
            c.setTime(new Date());
            c.add(Calendar.MONTH, -i);
            Date m = c.getTime();
            list.add(sdf.format(m));
        }
        Collections.reverse(list);
        return list;
    }

    /**
     * 鎴彇瀛楃涓查暱搴?     *
     * @param beforeStr
     * @param cutLeng
     * @return 鎴彇鍚庣殑瀛楃涓?     */
    public static String subStr(String beforeStr, int cutLeng) {
        if (beforeStr.length() > cutLeng)
            return beforeStr.substring(0, cutLeng) + "...";
        return beforeStr;
    }

    /**
     * 鐢熸垚闅忔満瀛楃涓诧紝瀛楁瘝鍜屾暟瀛楁贩鍚?     *
     * @return 缁勫悎鍚庣殑瀛楃涓?^[0-9a-zA-Z]
     */
    public static String getRandomChar() {
        //鐢熸垚涓€涓?銆?銆?鐨勯殢鏈烘暟瀛?        int rand = (int) Math.round(Math.random() * 1);
        long itmp = 0;
        char ctmp = '\u0000';
        switch (rand) {
            //鐢熸垚澶у啓瀛楁瘝 + 1000浠ュ唴鏁板瓧
            case 1:
                itmp = Math.round(Math.random() * 25 + 65);
                ctmp = (char) itmp;
                return String.valueOf(ctmp) + (int) Math.random() * 1000;
            //鐢熸垚灏忓啓瀛楁瘝
            case 2:
                itmp = Math.round(Math.random() * 25 + 97);
                ctmp = (char) itmp;
                return String.valueOf(ctmp) + (int) Math.random() * 1000;
            //鐢熸垚鏁板瓧
            default:
                itmp = Math.round(Math.random() * 1000);
                return itmp + "";
        }
    }

    /**
     * 鍒ゆ柇棣栧瓧姣嶄互鏁板瓧寮€澶?瀛楃涓插寘鎷暟瀛椼€佸瓧姣?浠ュ強绌烘牸
     *
     * @param str 妫€鏌ュ瓧绗︿覆
     * @return 鏄惁浠ユ暟瀛楀紑澶?     */
    public static boolean CheckIsStartWithNum(String str) {
        return Pattern.compile("^[0-9][a-zA-Z0-9%,\\s]*$").matcher(str).matches();
    }

    /**
     * 鍒ゆ柇棣栧瓧姣嶄互","寮€澶?瀛楃涓插寘鎷暟瀛椼€佸瓧姣?浠ュ強绌烘牸
     *
     * @param str 妫€鏌ュ瓧绗︿覆
     * @return 鏄惁浠ユ暟瀛楀紑澶?     */
    public static boolean CheckIsStartWithSpec(String str) {
        return Pattern.compile("^[,][a-zA-Z0-9%,\\s]*$").matcher(str).matches();
    }

    /**
     * 瀛楃杞爜
     *
     * @param aValue
     * @return
     * @see 杞爜鍚庣殑瀛楃涓?     */
    public static String encodeValue(String aValue) {
        if (aValue.trim().length() == 0) {
            return "";
        }
        String valueAfterTransCode = null;
        try {
            valueAfterTransCode = URLEncoder.encode(aValue, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.getMessage();
        }
        return valueAfterTransCode;
    }

    /**
     * 瀛楃杞爜
     *
     * @param aValue
     * @return
     * @see 杞爜鍚庣殑瀛楃涓?     */
    public static String decodeValue(String aValue) {
        if (aValue.trim().length() == 0) {
            return "";
        }
        String valueAfterTransCode = null;
        try {
            valueAfterTransCode = URLDecoder.decode(aValue, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.getMessage();
        }
        return valueAfterTransCode;
    }

    /**
     * 鍘婚櫎str涓殑'
     *
     * @param str
     * @return 闄ゅ幓'鍚庣殑瀛楃涓?     * @see [绫汇€佺被#鏂规硶銆佺被#鎴愬憳]
     */
    public static String afterDealStr(String str) {
        return str.replace("'", "");
    }

    /**
     * 浠嶳equest瀵硅薄涓幏寰楀鎴风IP锛屽鐞嗕簡HTTP浠ｇ悊鏈嶅姟鍣ㄥ拰Nginx鐨勫弽鍚戜唬鐞嗘埅鍙栦簡ip
     *
     * @param request
     * @return ip
     */
    public static String getLocalIp(HttpServletRequest request) {
        String remoteAddr = getIpAddr(request);
        String forwarded = request.getHeader("X-Forwarded-For");
        String realIp = request.getHeader("X-Real-IP");

        String ip = null;
        if (realIp == null) {
            if (forwarded == null) {
                ip = remoteAddr;
            } else {
                ip = remoteAddr + "/" + forwarded.split(",")[0];
            }
        } else {
            if (realIp.equals(forwarded)) {
                ip = realIp;
            } else {
                if (forwarded != null) {
                    forwarded = forwarded.split(",")[0];
                }
                ip = realIp + "/" + forwarded;
            }
        }
        return ip;
    }
    /**
     * 鑾峰彇璁块棶鑰匢P
     *
     * 鍦ㄤ竴鑸儏鍐典笅浣跨敤Request.getRemoteAddr()鍗冲彲锛屼絾鏄粡杩噉ginx绛夊弽鍚戜唬鐞嗚蒋浠跺悗锛岃繖涓柟娉曚細澶辨晥銆?     *
     * 鏈柟娉曞厛浠嶩eader涓幏鍙朮-Real-IP锛屽鏋滀笉瀛樺湪鍐嶄粠X-Forwarded-For鑾峰緱绗竴涓狪P(鐢?鍒嗗壊)锛?     * 濡傛灉杩樹笉瀛樺湪鍒欒皟鐢≧equest .getRemoteAddr()銆?     *
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");
        if (!StringUtils.isEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.isEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
            // 澶氭鍙嶅悜浠ｇ悊鍚庝細鏈夊涓狪P鍊硷紝绗竴涓负鐪熷疄IP銆?            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        } else {
            return request.getRemoteAddr();
        }
    }

    /**
     * 杞寲鍓嶅彴鎵归噺浼犲叆鐨処D鍊?     *
     * @param data
     * @return 杞寲鍚庣殑ID鍊兼暟缁?     */
    public static int[] changeDataForm(String data) {
        String[] dataStr = data.split(",");
        int[] dataInt = new int[dataStr.length];
        for (int i = 0; i < dataStr.length; i++)
            dataInt[i] = Integer.parseInt(dataStr[i]);
        return dataInt;
    }

    /**
     * 鍐欑悊璐㈡棩蹇楀唴瀹硅浆鍖栫壒娈婂瓧绗?     *
     * @param str 闇€瑕佽浆鍖栫殑瀛楃
     * @return 杞寲鍚庣殑瀛楃
     */
    public static String htmlspecialchars(String str) {
        str = str.replaceAll("&", "&amp;");
        str = str.replaceAll("<", "&lt;");
        str = str.replaceAll(">", "&gt;");
        str = str.replaceAll("\"", "&quot;");
        return str;
    }

    /**
     * 鏍规嵁娑堣垂鏃ユ湡鑾峰彇娑堣垂鏈?     *
     * @param consumeDate 娑堣垂鏃ユ湡
     * @return 杩斿洖娑堣垂鏈堜俊鎭?     */
    public static String getConsumeMonth(String consumeDate) {
        return consumeDate.substring(0, 7);
    }

    /**
     * 鑾峰彇褰撳墠鏃ユ湡鐨勫墠XX涓湀
     *
     * @param beforeMonth
     * @return 鍓峏X涓湀瀛楃涓?     */
    public static String getBeforeMonth(int beforeMonth) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -beforeMonth);
        return new SimpleDateFormat("yyyy-MM").format(c.getTime());
    }

    /**
     * 鏍规嵁鏈堜唤鑾峰彇褰撴湀绗竴澶?     * @param monthTime
     * @return
     * @throws ParseException
     */
    public static String firstDayOfMonth(String monthTime) throws ParseException {
        return monthTime + "-01";
    }

    /**
     * 鏍规嵁鏈堜唤鑾峰彇褰撴湀鏈€鍚庝竴澶?     * @param monthTime
     * @return
     * @throws ParseException
     */
    public static String lastDayOfMonth(String monthTime) throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM").parse(monthTime);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.roll(Calendar.DAY_OF_MONTH, -1);
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }

    /**
     * 鑾峰彇email鐢ㄦ埛濮撳悕
     *
     * @param emailAddress
     */
    public static String getEmailUserName(String emailAddress) {
        return emailAddress.substring(0, emailAddress.lastIndexOf("@"));
    }

    /**
     * 鍒ゆ柇userTel鏄惁鍚堟硶锛寀serTel鍙兘鏄暟瀛?     *
     * @param userTel
     * @return true 鍚堟硶 false涓嶅悎娉?     */
    public static boolean isTelNumber(String userTel) {
        String reg_phone = "^(\\(\\d{3,4}\\)|\\d{3,4}-)?\\d{7,8}$";
        String reg_tel = "^(1[0-9][0-9]|1[0-9][0|3|6|8|9])\\d{8}$";
        boolean b_phpne = Pattern.compile(reg_phone).matcher(userTel).matches();
        boolean b_tel = Pattern.compile(reg_tel).matcher(userTel).matches();
        return (b_phpne || b_tel);
    }

    /**
     * 妯＄硦鍒ゆ柇鐢佃瘽鍙风爜鏄惁鍚堟硶锛屽彧鑳芥槸鏁板瓧
     *
     * @param userTel
     * @return
     */
    public static boolean isTelNumberBySlur(String userTel) {
        return Pattern.compile("^([\\s0-9]{0,12}$)").matcher(userTel).matches();
    }

    /**
     * 鑾峰彇褰撳墠鏃堕棿鐨勫瓧绗︿覆绫诲瀷
     *
     * @return 澶勭悊鍚庣殑瀛楃涓茬被鍨?     */
    public static String getNowTime() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
    }

    /**
     * 鍒ゆ柇瀛楃涓蹭腑鏄惁鍚湁涓枃
     *
     * @param str
     * @return
     * @author jishenghua
     */
    public static boolean isContainsChinese(String str) {
        return Pattern.compile("[\u4e00-\u9fa5]").matcher(str).matches();
    }

    /**
     * 杩囨护html鏂囦欢涓殑鏂囨湰
     *
     * @param content
     * @return杩囨护鍚庣殑鏂囨湰
     */
    public static String filterText(String content) {
        return content.replace("/<(?:.|\\s)*?>/g", "");
    }

    /**
     * 鍘绘帀瀛楃涓蹭腑鎵€鏈夌鍙凤紝涓嶈鏄叏瑙掞紝杩樻槸鍗婅鐨勶紝鎴栨槸璐у竵绗﹀彿鎴栬€呯┖鏍肩瓑
     *
     * @param s
     * @return
     * @author jishenghua
     */
    public static String removeSymbolForString(String s) {
        StringBuffer buffer = new StringBuffer();
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if ((chars[i] >= 19968 && chars[i] <= 40869) || (chars[i] >= 97 && chars[i] <= 122) || (chars[i] >= 65 && chars[i] <= 90)) {
                buffer.append(chars[i]);
            }
        }
        return buffer.toString();
    }

    /**
     * 鑾峰彇涓€涓瓧绗︿覆鐨凪D5
     *
     * @param msg
     * @return 鍔犲瘑鍚庣殑MD5瀛楃涓?     * @throws NoSuchAlgorithmException
     */
    public static String md5Encryp(String msg) throws NoSuchAlgorithmException {
        // 鐢熸垚涓€涓狹D5鍔犲瘑璁＄畻鎽樿
        MessageDigest md = MessageDigest.getInstance("MD5");
        // 璁＄畻md5鍑芥暟
        md.update(msg.getBytes());
        return new BigInteger(1, md.digest()).toString(16);
    }

    /**
     * 鍒ゆ柇鏄惁鎻掍欢URL 
     *
     * @return
     */
    public static boolean isPluginUrl(String url) {
        if (url != null && (url.startsWith("/plugin"))) {
            return true;
        }
        return false;
    }

    /**
     * 澶勭悊瀛楃涓瞡ull鍊?     *
     * @param beforeStr 澶勭悊鍓嶅瓧绗︿覆
     * @return 澶勭悊鍚庣殑瀛楃涓?     */
    public static String dealNullStr(String beforeStr) {
        if (null == beforeStr || beforeStr.length() == 0)
            return "";
        return beforeStr;
    }

    /**
     * 鏍规嵁token鎴彇绉熸埛id
     * @param token
     * @return
     */
    public static Long getTenantIdByToken(String token) {
        Long tenantId = 0L;
        if(StringUtil.isNotEmpty(token) && token.indexOf("_")>-1) {
            String[] tokenArr = token.split("_");
            if (tokenArr.length == 2) {
                tenantId = Long.parseLong(tokenArr[1]);
            }
        }
        return tenantId;
    }

    /**
     * 浣跨敤鍙傛暟Format灏嗗瓧绗︿覆杞负Date
     *
     * @param strDate
     * @param pattern
     * @return
     * @throws ParseException
     * @author jishenghua
     */
    public static Date parse(String strDate, String pattern)
            throws ParseException {
        return new SimpleDateFormat(pattern).parse(strDate);
    }

    public static Date addDays(Date date, int num) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date); //闇€瑕佸皢date鏁版嵁杞Щ鍒癈alender瀵硅薄涓搷浣?        calendar.add(calendar.DATE, num);//鎶婃棩鏈熷線鍚庡鍔爊澶?姝ｆ暟寰€鍚庢帹,璐熸暟寰€鍓嶇Щ鍔?        date=calendar.getTime();   //杩欎釜鏃堕棿灏辨槸鏃ユ湡寰€鍚庢帹涓€澶╃殑缁撴灉
        return date;
    }

    /**
     * 鐢熸垚闅忔満鏁板瓧鍜屽瓧姣嶇粍鍚?     * @param length
     * @return
     */
    public static String getCharAndNum(int length) {
        Random random = new Random();
        StringBuffer valSb = new StringBuffer();
        String charStr = "0123456789abcdefghijklmnopqrstuvwxyz";
        int charLength = charStr.length();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(charLength);
            valSb.append(charStr.charAt(index));
        }
        return valSb.toString();
    }

    public static void main(String[] args) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(date);
        System.out.println(dateString);
    }
}
