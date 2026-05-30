package com.jsh.erp.utils;


/**
 * 类型转换工具类
 * 提供常用的数据类型转换方法（String 转 Integer/Long 等）
 *
 * @author jishenghua
 */
import com.jsh.erp.utils.StringUtil;

import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 * 绫诲瀷杞崲鍣? *
 * @author ji-sheng-hua
 */
public class Convert
{
    /**
     * 杞崲涓哄瓧绗︿覆<br>
     * 濡傛灉缁欏畾鐨勫€间负null锛屾垨鑰呰浆鎹㈠け璐ワ紝杩斿洖榛樿鍊?br>
     * 杞崲澶辫触涓嶄細鎶ラ敊
     *
     * @param value 琚浆鎹㈢殑鍊?     * @param defaultValue 杞崲閿欒鏃剁殑榛樿鍊?     * @return 缁撴灉
     */
    public static String toStr(Object value, String defaultValue)
    {
        if (null == value)
        {
            return defaultValue;
        }
        if (value instanceof String)
        {
            return (String) value;
        }
        return value.toString();
    }

    /**
     * 杞崲涓哄瓧绗︿覆<br>
     * 濡傛灉缁欏畾鐨勫€间负<code>null</code>锛屾垨鑰呰浆鎹㈠け璐ワ紝杩斿洖榛樿鍊?code>null</code><br>
     * 杞崲澶辫触涓嶄細鎶ラ敊
     *
     * @param value 琚浆鎹㈢殑鍊?     * @return 缁撴灉
     */
    public static String toStr(Object value)
    {
        return toStr(value, null);
    }

    /**
     * 杞崲涓哄瓧绗?br>
     * 濡傛灉缁欏畾鐨勫€间负null锛屾垨鑰呰浆鎹㈠け璐ワ紝杩斿洖榛樿鍊?br>
     * 杞崲澶辫触涓嶄細鎶ラ敊
     *
     * @param value 琚浆鎹㈢殑鍊?     * @param defaultValue 杞崲閿欒鏃剁殑榛樿鍊?     * @return 缁撴灉
     */
    public static Character toChar(Object value, Character defaultValue)
    {
        if (null == value)
        {
            return defaultValue;
        }
        if (value instanceof Character)
        {
            return (Character) value;
        }

        final String valueStr = toStr(value, null);
        return StringUtil.isEmpty(valueStr) ? defaultValue : valueStr.charAt(0);
    }

    /**
     * 杞崲涓哄瓧绗?br>
     * 濡傛灉缁欏畾鐨勫€间负<code>null</code>锛屾垨鑰呰浆鎹㈠け璐ワ紝杩斿洖榛樿鍊?code>null</code><br>
     * 杞崲澶辫触涓嶄細鎶ラ敊
     *
     * @param value 琚浆鎹㈢殑鍊?     * @return 缁撴灉
     */
    public static Character toChar(Object value)
    {
        return toChar(value, null);
    }

    /**
     * 杞崲涓篵yte<br>
     * 濡傛灉缁欏畾鐨勫€间负<code>null</code>锛屾垨鑰呰浆鎹㈠け璐ワ紝杩斿洖榛樿鍊?br>
     * 杞崲澶辫触涓嶄細鎶ラ敊
     *
     * @param value 琚浆鎹㈢殑鍊?     * @param defaultValue 杞崲閿欒鏃剁殑榛樿鍊?     * @return 缁撴灉
     */
    public static Byte toByte(Object value, Byte defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof Byte)
        {
            return (Byte) value;
        }
        if (value instanceof Number)
        {
            return ((Number) value).byteValue();
        }
        final String valueStr = toStr(value, null);
        if (StringUtil.isEmpty(valueStr))
        {
            return defaultValue;
        }
        try
        {
            return Byte.parseByte(valueStr);
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * 杞崲涓篵yte<br>
     * 濡傛灉缁欏畾鐨勫€间负<code>null</code>锛屾垨鑰呰浆鎹㈠け璐ワ紝杩斿洖榛樿鍊?code>null</code><br>
     * 杞崲澶辫触涓嶄細鎶ラ敊
     *
     * @param value 琚浆鎹㈢殑鍊?     * @return 缁撴灉
     */
    public static Byte toByte(Object value)
    {
        return toByte(value, null);
    }

    /**
     * 杞崲涓篠hort<br>
     * 濡傛灉缁欏畾鐨勫€间负<code>null</code>锛屾垨鑰呰浆鎹㈠け璐ワ紝杩斿洖榛樿鍊?br>
     * 杞崲澶辫触涓嶄細鎶ラ敊
     *
     * @param value 琚浆鎹㈢殑鍊?     * @param defaultValue 杞崲閿欒鏃剁殑榛樿鍊?     * @return 缁撴灉
     */
    public static Short toShort(Object value, Short defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof Short)
        {
            return (Short) value;
        }
        if (value instanceof Number)
        {
            return ((Number) value).shortValue();
        }
        final String valueStr = toStr(value, null);
        if (StringUtil.isEmpty(valueStr))
        {
            return defaultValue;
        }
        try
        {
            return Short.parseShort(valueStr.trim());
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * 杞崲涓篠hort<br>
     * 濡傛灉缁欏畾鐨勫€间负<code>null</code>锛屾垨鑰呰浆鎹㈠け璐ワ紝杩斿洖榛樿鍊?code>null</code><br>
     * 杞崲澶辫触涓嶄細鎶ラ敊
     *
     * @param value 琚浆鎹㈢殑鍊?     * @return 缁撴灉
     */
    public static Short toShort(Object value)
    {
        return toShort(value, null);
    }

    /**
     * 杞崲涓篘umber<br>
     * 濡傛灉缁欏畾鐨勫€间负绌猴紝鎴栬€呰浆鎹㈠け璐ワ紝杩斿洖榛樿鍊?br>
     * 杞崲澶辫触涓嶄細鎶ラ敊
     *
     * @param value 琚浆鎹㈢殑鍊?     * @param defaultValue 杞崲閿欒鏃剁殑榛樿鍊?     * @return 缁撴灉
     */
    public static Number toNumber(Object value, Number defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof Number)
        {
            return (Number) value;
        }
        final String valueStr = toStr(value, null);
        if (StringUtil.isEmpty(valueStr))
        {
            return defaultValue;
        }
        try
        {
            return NumberFormat.getInstance().parse(valueStr);
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * 杞崲涓篘umber<br>
     * 濡傛灉缁欏畾鐨勫€间负绌猴紝鎴栬€呰浆鎹㈠け璐ワ紝杩斿洖榛樿鍊?code>null</code><br>
     * 杞崲澶辫触涓嶄細鎶ラ敊
     *
     * @param value 琚浆鎹㈢殑鍊?     * @return 缁撴灉
     */
    public static Number toNumber(Object value)
    {
        return toNumber(value, null);
    }

    /**
     * 杞崲涓篿nt<br>
     * 濡傛灉缁欏畾鐨勫€间负绌猴紝鎴栬€呰浆鎹㈠け璐ワ紝杩斿洖榛樿鍊?br>
     * 杞崲澶辫触涓嶄細鎶ラ敊
     *
     * @param value 琚浆鎹㈢殑鍊?     * @param defaultValue 杞崲閿欒鏃剁殑榛樿鍊?     * @return 缁撴灉
     */
    public static Integer toInt(Object value, Integer defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof Integer)
        {
            return (Integer) value;
        }
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        final String valueStr = toStr(value, null);
        if (StringUtil.isEmpty(valueStr))
        {
            return defaultValue;
        }
        try
        {
            return Integer.parseInt(valueStr.trim());
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * 杞崲涓篿nt<br>
     * 濡傛灉缁欏畾鐨勫€间负<code>null</code>锛屾垨鑰呰浆鎹㈠け璐ワ紝杩斿洖榛樿鍊?code>null</code><br>
     * 杞崲澶辫触涓嶄細鎶ラ敊
     *
     * @param value 琚浆鎹㈢殑鍊?     * @return 缁撴灉
     */
    public static Integer toInt(Object value)
    {
        return toInt(value, null);
    }

    /**
     * 杞崲涓篒nteger鏁扮粍<br>
     *
     * @param str 琚浆鎹㈢殑鍊?     * @return 缁撴灉
     */
    public static Integer[] toIntArray(String str)
    {
        return toIntArray(",", str);
    }

    /**
     * 杞崲涓篖ong鏁扮粍<br>
     *
     * @param str 琚浆鎹㈢殑鍊?     * @return 缁撴灉
     */
    public static Long[] toLongArray(String str)
    {
        return toLongArray(",", str);
    }

    /**
     * 杞崲涓篒nteger鏁扮粍<br>
     *
     * @param split 鍒嗛殧绗?     * @param split 琚浆鎹㈢殑鍊?     * @return 缁撴灉
     */
    public static Integer[] toIntArray(String split, String str)
    {
        if (StringUtil.isEmpty(str))
        {
            return new Integer[] {};
        }
        String[] arr = str.split(split);
        final Integer[] ints = new Integer[arr.length];
        for (int i = 0; i < arr.length; i++)
        {
            final Integer v = toInt(arr[i], 0);
            ints[i] = v;
        }
        return ints;
    }

    /**
     * 杞崲涓篖ong鏁扮粍<br>
     *
     * @param split 鍒嗛殧绗?     * @param str 琚浆鎹㈢殑鍊?     * @return 缁撴灉
     */
    public static Long[] toLongArray(String split, String str)
    {
        if (StringUtil.isEmpty(str))
        {
            return new Long[] {};
        }
        String[] arr = str.split(split);
        final Long[] longs = new Long[arr.length];
        for (int i = 0; i < arr.length; i++)
        {
            final Long v = toLong(arr[i], null);
            longs[i] = v;
        }
        return longs;
    }

    /**
     * 杞崲涓篠tring鏁扮粍<br>
     *
     * @param str 琚浆鎹㈢殑鍊?     * @return 缁撴灉
     */
    public static String[] toStrArray(String str)
    {
        return toStrArray(",", str);
    }

    /**
     * 杞崲涓篠tring鏁扮粍<br>
     *
     * @param split 鍒嗛殧绗?     * @param split 琚浆鎹㈢殑鍊?     * @return 缁撴灉
     */
    public static String[] toStrArray(String split, String str)
    {
        return str.split(split);
    }

    /**
     * 杞崲涓簂ong<br>
     * 濡傛灉缁欏畾鐨勫€间负绌猴紝鎴栬€呰浆鎹㈠け璐ワ紝杩斿洖榛樿鍊?br>
     * 杞崲澶辫触涓嶄細鎶ラ敊
     *
     * @param value 琚浆鎹㈢殑鍊?     * @param defaultValue 杞崲閿欒鏃剁殑榛樿鍊?     * @return 缁撴灉
     */
    public static Long toLong(Object value, Long defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof Long)
        {
            return (Long) value;
        }
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        final String valueStr = toStr(value, null);
        if (StringUtil.isEmpty(valueStr))
        {
            return defaultValue;
        }
        try
        {
            // 鏀寔绉戝璁℃暟娉?            return new BigDecimal(valueStr.trim()).longValue();
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * 杞崲涓簂ong<br>
     * 濡傛灉缁欏畾鐨勫€间负<code>null</code>锛屾垨鑰呰浆鎹㈠け璐ワ紝杩斿洖榛樿鍊?code>null</code><br>
     * 杞崲澶辫触涓嶄細鎶ラ敊
     *
     * @param value 琚浆鎹㈢殑鍊?     * @return 缁撴灉
     */
    public static Long toLong(Object value)
    {
        return toLong(value, null);
    }

    /**
     * 杞崲涓篸ouble<br>
     * 濡傛灉缁欏畾鐨勫€间负绌猴紝鎴栬€呰浆鎹㈠け璐ワ紝杩斿洖榛樿鍊?br>
     * 杞崲澶辫触涓嶄細鎶ラ敊
     *
     * @param value 琚浆鎹㈢殑鍊?     * @param defaultValue 杞崲閿欒鏃剁殑榛樿鍊?     * @return 缁撴灉
     */
    public static Double toDouble(Object value, Double defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof Double)
        {
            return (Double) value;
        }
        if (value instanceof Number)
        {
            return ((Number) value).doubleValue();
        }
        final String valueStr = toStr(value, null);
        if (StringUtil.isEmpty(valueStr))
        {
            return defaultValue;
        }
        try
        {
            // 鏀寔绉戝璁℃暟娉?            return new BigDecimal(valueStr.trim()).doubleValue();
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * 杞崲涓篸ouble<br>
     * 濡傛灉缁欏畾鐨勫€间负绌猴紝鎴栬€呰浆鎹㈠け璐ワ紝杩斿洖榛樿鍊?code>null</code><br>
     * 杞崲澶辫触涓嶄細鎶ラ敊
     *
     * @param value 琚浆鎹㈢殑鍊?     * @return 缁撴灉
     */
    public static Double toDouble(Object value)
    {
        return toDouble(value, null);
    }

    /**
     * 杞崲涓篵oolean<br>
     * String鏀寔鐨勫€间负锛歵rue銆乫alse銆亂es銆乷k銆乶o锛?,0 濡傛灉缁欏畾鐨勫€间负绌猴紝鎴栬€呰浆鎹㈠け璐ワ紝杩斿洖榛樿鍊?br>
     * 杞崲澶辫触涓嶄細鎶ラ敊
     *
     * @param value 琚浆鎹㈢殑鍊?     * @param defaultValue 杞崲閿欒鏃剁殑榛樿鍊?     * @return 缁撴灉
     */
    public static Boolean toBool(Object value, Boolean defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof Boolean)
        {
            return (Boolean) value;
        }
        String valueStr = toStr(value, null);
        if (StringUtil.isEmpty(valueStr))
        {
            return defaultValue;
        }
        valueStr = valueStr.trim().toLowerCase();
        switch (valueStr)
        {
            case "true":
            case "yes":
            case "ok":
            case "1":
                return true;
            case "false":
            case "no":
            case "0":
                return false;
            default:
                return defaultValue;
        }
    }

    /**
     * 杞崲涓篵oolean<br>
     * 濡傛灉缁欏畾鐨勫€间负绌猴紝鎴栬€呰浆鎹㈠け璐ワ紝杩斿洖榛樿鍊?code>null</code><br>
     * 杞崲澶辫触涓嶄細鎶ラ敊
     *
     * @param value 琚浆鎹㈢殑鍊?     * @return 缁撴灉
     */
    public static Boolean toBool(Object value)
    {
        return toBool(value, null);
    }

}
