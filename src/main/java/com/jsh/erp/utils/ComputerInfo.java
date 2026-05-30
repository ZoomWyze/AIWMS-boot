package com.jsh.erp.utils;


/**
 * 计算机信息工具类
 * 获取服务器的硬件信息（IP 地址、MAC 地址等），用于系统注册和授权
 *
 * @author jishenghua
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * <鍙栫綉鍗＄墿鐞嗗湴鍧€--
 * 1.鍦╓indows,Linux绯荤粺涓嬪潎鍙敤锛?
 * 2.閫氳繃ipconifg,ifconfig鑾峰緱璁＄畻鏈轰俊鎭紱
 * 3.鍐嶇敤妯″紡鍖归厤鏂瑰紡鏌ユ壘MAC鍦板潃锛屼笌鎿嶄綔绯荤粺鐨勮瑷€鏃犲叧>
 *
 * //* Description: <鍙栬绠楁満鍚?-浠庣幆澧冨彉閲忎腑鍙?
 * abstract 闄愬埗缁ф壙/鍒涘缓瀹炰緥
 */
public abstract class ComputerInfo {
    private static String macAddressStr = null;
    private static String computerName = System.getenv().get("COMPUTERNAME");

    private static final String[] windowsCommand = { "ipconfig", "/all" };
    private static final String[] linuxCommand = { "/sbin/ifconfig", "-a" };
    private static final Pattern macPattern = Pattern.compile(".*((:?[0-9a-f]{2}[-:]){5}[0-9a-f]{2}).*",
            Pattern.CASE_INSENSITIVE);

    /**
     * 鑾峰彇澶氫釜缃戝崱鍦板潃
     *
     * @return
     * @throws IOException
     */
    private final static List<String> getMacAddressList() throws IOException {
        final ArrayList<String> macAddressList = new ArrayList<String>();
        final String os = System.getProperty("os.name");
        final String command[];

        if (os.startsWith("Windows")) {
            command = windowsCommand;
        } else if (os.startsWith("Linux")) {
            command = linuxCommand;
        } else {
            throw new IOException("Unknow operating system:" + os);
        }
        // 鎵ц鍛戒护
        final Process process = Runtime.getRuntime().exec(command);

        BufferedReader bufReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        for (String line = null; (line = bufReader.readLine()) != null;) {
            Matcher matcher = macPattern.matcher(line);
            if (matcher.matches()) {
                macAddressList.add(matcher.group(1));
                // macAddressList.add(matcher.group(1).replaceAll("[-:]",
                // ""));//鍘绘帀MAC涓殑鈥?鈥?
            }
        }

        process.destroy();
        bufReader.close();
        return macAddressList;
    }

    /**
     * 鑾峰彇涓€涓綉鍗″湴鍧€锛堝涓綉鍗℃椂浠庝腑鑾峰彇涓€涓級
     *
     * @return
     */
    public static String getMacAddress() {
        if (macAddressStr == null || macAddressStr.equals("")) {
            StringBuffer sb = new StringBuffer(); // 瀛樻斁澶氫釜缃戝崱鍦板潃鐢紝鐩墠鍙彇涓€涓潪0000000000E0闅ч亾鐨勫€?
            try {
                List<String> macList = getMacAddressList();
                for (Iterator<String> iter = macList.iterator(); iter.hasNext();) {
                    String amac = iter.next();
                    if (!amac.equals("0000000000E0")) {
                        sb.append(amac);
                        break;
                    }
                }
            } catch (IOException ignored) {
            }

            macAddressStr = sb.toString();

        }

        return macAddressStr;
    }

    /**
     * 鑾峰彇鐢佃剳鍚?
     *
     * @return
     */
    public static String getComputerName() {
        if (computerName == null || computerName.equals("")) {
            computerName = System.getenv().get("COMPUTERNAME");
        }
        return computerName;
    }

    /**
     * 鑾峰彇瀹㈡埛绔疘P鍦板潃
     *
     * @return
     */
    public static String getIpAddrAndName() throws IOException {
        return InetAddress.getLocalHost().toString();
    }

    /**
     * 鑾峰彇瀹㈡埛绔疘P鍦板潃
     *
     * @return
     */
    public static String getIpAddr() throws IOException {
        return InetAddress.getLocalHost().getHostAddress().toString();
    }

    /**
     * 闄愬埗鍒涘缓瀹炰緥
     */
    private ComputerInfo() {

    }

    public static void main(String[] args) throws IOException {
        System.out.println(ComputerInfo.getMacAddress());
        System.out.println(ComputerInfo.getComputerName());
        System.out.println(ComputerInfo.getIpAddr());
        System.out.println(ComputerInfo.getIpAddrAndName());
    }
}
