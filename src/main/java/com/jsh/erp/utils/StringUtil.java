package com.jsh.erp.utils;


/**
 * 字符串工具类
 * 提供字符串的常用操作方法（判空、去空格、格式化等）
 *
 * @author jishenghua
 */
import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author jishenghua qq752718920  2018-10-7 15:26:27
 */
public class StringUtil {

    private StringUtil() {

    }

    private static String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public final static String regex = "'|#|%|;|--| and | and|and | or | or|or | not | not|not " +
            "| use | use|use | insert | insert|insert | delete | delete|delete | update | update|update " +
            "| select | select|select | count | count|count | group | group|group | union | union|union " +
            "| create | create|create | drop | drop|drop | truncate | truncate|truncate | alter | alter|alter " +
            "| grant | grant|grant | execute | execute|execute | exec | exec|exec | xp_cmdshell | xp_cmdshell|xp_cmdshell " +
            "| call | call|call | declare | declare|declare | source | source|source | sql | sql|sql ";

    /** 涓嬪垝绾?*/
    private static final char SEPARATOR = '_';

    public static String filterNull(String str) {
        if (str == null) {
            return "";
        } else {
            return str.trim();
        }
    }

    public static boolean stringEquels(String source,String target) {
        if(isEmpty(source)||isEmpty(target)){
            return false;
        }else{
            return source.equals(target);
        }
    }

    public static int length(CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    /**
     * 椹煎嘲杞笅鍒掔嚎鍛藉悕
     */
    public static String toUnderScoreCase(String str)
    {
        if (str == null)
        {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        // 鍓嶇疆瀛楃鏄惁澶у啓
        boolean preCharIsUpperCase = true;
        // 褰撳墠瀛楃鏄惁澶у啓
        boolean curreCharIsUpperCase = true;
        // 涓嬩竴瀛楃鏄惁澶у啓
        boolean nexteCharIsUpperCase = true;
        for (int i = 0; i < str.length(); i++)
        {
            char c = str.charAt(i);
            if (i > 0)
            {
                preCharIsUpperCase = Character.isUpperCase(str.charAt(i - 1));
            }
            else
            {
                preCharIsUpperCase = false;
            }

            curreCharIsUpperCase = Character.isUpperCase(c);

            if (i < (str.length() - 1))
            {
                nexteCharIsUpperCase = Character.isUpperCase(str.charAt(i + 1));
            }

            if (preCharIsUpperCase && curreCharIsUpperCase && !nexteCharIsUpperCase)
            {
                sb.append(SEPARATOR);
            }
            else if ((i != 0 && !preCharIsUpperCase) && curreCharIsUpperCase)
            {
                sb.append(SEPARATOR);
            }
            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }

    /**
     * * 鍒ゆ柇涓€涓璞℃槸鍚︿负绌?     *
     * @param object Object
     * @return true锛氫负绌?false锛氶潪绌?     */
    public static boolean isNull(Object object)
    {
        return object == null;
    }

    /**
     * * 鍒ゆ柇涓€涓璞℃槸鍚﹂潪绌?     *
     * @param object Object
     * @return true锛氶潪绌?false锛氱┖
     */
    public static boolean isNotNull(Object object)
    {
        return !isNull(object);
    }

    public static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static String getSysDate(String format) {
        if (StringUtil.isEmpty(format)) {
            format = DEFAULT_FORMAT;
        }
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(new Date());
    }

    public static Date getDateByString(String date, String format) {
        if (StringUtil.isEmpty(format)) {
            format = DEFAULT_FORMAT;
        }
        if (StringUtil.isNotEmpty(date)) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            try {
                return sdf.parse(date);
            } catch (ParseException e) {
                throw new RuntimeException("杞崲涓烘棩鏈熺被鍨嬮敊璇細DATE锛? + date + "  FORMAT:" + format);
            }
        } else {
            return null;
        }
    }

    public static Date getDateByLongDate(Long millis) {
        if (millis == null) {
            return new Date();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return cal.getTime();

    }

    public static UUID stringToUUID(String id) {
        if (StringUtil.isNotEmpty(id)) {
            return UUID.fromString(id);
        } else {
            return null;
        }
    }

    public static Integer parseInteger(String str) {
        if (StringUtil.isNotEmpty(str)) {
            return Integer.parseInt(str);
        } else {
            return null;
        }
    }

    public static Long parseStrLong(String str) {
        if (StringUtil.isNotEmpty(str)) {
            return Long.parseLong(str);
        } else {
            return null;
        }
    }

    public static List<UUID> listToUUID(List<String> listStrs) {
        if (listStrs != null && listStrs.size() > 0) {
            List<UUID> uuidList = new ArrayList<UUID>();
            for (String str : listStrs) {
                uuidList.add(UUID.fromString(str));
            }
            return uuidList;
        } else {
            return null;
        }
    }

    public static List<UUID> arrayToUUIDList(String[] uuids) {
        if (uuids != null && uuids.length > 0) {
            List<UUID> uuidList = new ArrayList<UUID>();
            for (String str : uuids) {
                uuidList.add(UUID.fromString(str));
            }
            return uuidList;
        } else {
            return null;
        }
    }

    //鏄惁鏄疛SON
    public static boolean containsAny(String str, String... flag) {
        if (str != null) {
            if (flag == null || flag.length == 0) {
                flag = "[-{-}-]-,".split("-");
            }
            for (String s : flag) {
                if (str.contains(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getModifyOrgOperateData(UUID resourceId, UUID orgId) {
        if (resourceId != null && orgId != null) {
            Map<UUID, UUID> map = new HashMap<UUID, UUID>();
            map.put(resourceId, orgId);
            return JSON.toJSONString(map);
        }
        return "";
    }

    public static String[] listToStringArray(List<String> list) {
        if (list != null && !list.isEmpty()) {
            return list.toArray(new String[list.size()]);
        }
        return new String[0];
    }

    public static Long[] listToLongArray(List<Long> list) {
        if (list != null && !list.isEmpty()) {
            return list.toArray(new Long[list.size()]);
        }
        return new Long[0];
    }

    public static List<String> stringToListArray(String[] strings) {
        if (strings != null && strings.length > 0) {
            return Arrays.asList(strings);
        }
        return new ArrayList<String>();
    }

    public static BigDecimal getArrSum(String[] strings) {
        BigDecimal sum = BigDecimal.ZERO;
        for(int i=0;i<strings.length;i++){
            sum = sum.add(new BigDecimal(strings[i]));
        }
        return sum;
    }

    /**
     * String瀛楃涓茶浆鎴怢ist<Long>鏁版嵁鏍煎紡
     * String str = "1,2,3,4,5,6" -> List<Long> listLong [1,2,3,4,5,6];
     *
     * @param strArr
     * @return
     */
    public static List<Long> strToLongList(String strArr) {
        List<Long> idList=new ArrayList<Long>();
        String[] d=strArr.split(",");
        for (int i = 0, size = d.length; i < size; i++) {
            if(d[i]!=null) {
                idList.add(Long.parseLong(d[i]));
            }
        }
        return idList;
    }

    /**
     * String瀛楃涓茶浆鎴怢ist<BigDecimal>鏁版嵁鏍煎紡
     * String str = "1,2,3,4,5,6" -> List<BigDecimal> listBigDecimal [1,2,3,4,5,6];
     *
     * @param strArr
     * @return
     */
    public static List<BigDecimal> strToBigDecimalList(String strArr) {
        List<BigDecimal> idList=new ArrayList<>();
        String[] d=strArr.split(",");
        for (int i = 0, size = d.length; i < size; i++) {
            if(d[i]!=null) {
                idList.add(new BigDecimal(d[i]));
            }
        }
        return idList;
    }

    /**
     * String瀛楃涓茶浆鎴怢ist<String>鏁版嵁鏍煎紡
     * String str = "1,2,3,4,5,6" -> List<Long> listLong [1,2,3,4,5,6];
     *
     * @param strArr
     * @return
     */
    public static List<String> strToStringList(String strArr) {
        if(StringUtils.isEmpty(strArr)){
            return null;
        }
        List<String> idList=new ArrayList<String>();
        String[] d=strArr.split(",");
        for (int i = 0, size = d.length; i < size; i++) {
            if(d[i]!=null) {
                idList.add(d[i].toString());
            }
        }
        return idList;
    }

    public static List<String> searchCondition(String search) {
        if (isEmpty(search)) {
            return new ArrayList<String>();
        }else{
            //String[] split = search.split(" ");
			String[] split = search.split("#");
            return stringToListArray(split);
        }
    }

    public static String getInfo(String search, String key){
        String value = null;
        if(StringUtil.isNotEmpty(search)) {
            search = search.replace("{}","");
            if(StringUtil.isNotEmpty(search)) {
                JSONObject obj = JSONObject.parseObject(search);
                if (obj.get(key) != null) {
                    value = obj.getString(key).trim();
                    if (value.equals("")) {
                        value = null;
                    }
                } else {
                    value = null;
                }
            }
        }
        return value;
    }

    public static String toNull(String value) {
        if(isEmpty(value)) {
            value = null;
        } else {
            value = value.trim();
        }
        return value;
    }

    public static boolean isExist(Object value) {
        if(value!=null) {
            String str = value.toString();
            if("".equals(str.trim())) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * 鍒ゆ柇瀵硅薄鏄惁涓烘鏁存暟
     * @param value
     * @return
     */
    public static boolean isPositiveLong(Object value) {
        if(value!=null) {
            String str = value.toString();
            if(isNotEmpty(str)) {
                if((str.matches("[0-9]+"))&&(Long.parseLong(str)>0)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 鏍￠獙鏉＄爜闀垮害涓?鍒?0浣?     * @param value
     * @return
     */
    public static boolean checkBarCodeLength(Object value) {
        if(value!=null) {
            String str = value.toString();
            if(isNotEmpty(str)) {
                if(str.length()>=4 && str.length()<=40 ) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 鍒ゆ柇瀵硅薄鏄惁涓烘暟瀛楋紙鍚皬鏁帮級
     * @param str
     * @return
     */
    public static boolean isPositiveBigDecimal(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        if(str.indexOf(".")>0){//鍒ゆ柇鏄惁鏈夊皬鏁扮偣
            if(str.indexOf(".")==str.lastIndexOf(".") && str.split("\\.").length==2){ //鍒ゆ柇鏄惁鍙湁涓€涓皬鏁扮偣
                return pattern.matcher(str.replace(".","")).matches();
            }else {
                return false;
            }
        }else {
            return pattern.matcher(str).matches();
        }
    }

    /**
     * sql娉ㄥ叆杩囨护锛屼繚闅渟ql鐨勫畨鍏ㄦ墽琛?     * @param originStr
     * @return
     */
    public static String safeSqlParse(String originStr){
        return originStr.replaceAll("(?i)" + regex, "");
    }

    /**
     * 鍒ゆ柇瀛楃涓叉槸鍚︿负绾暟瀛?     * @param str 杈撳叆鐨勫瓧绗︿覆
     * @return 濡傛灉瀛楃涓蹭负绾暟瀛楋紝杩斿洖 true锛涘惁鍒欒繑鍥?false
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        // 浣跨敤姝ｅ垯琛ㄨ揪寮忓垽鏂瓧绗︿覆鏄惁涓虹函鏁板瓧
        return str.matches("\\d+");
    }

}
