package com.jsh.erp.utils;


/**
 * 权限工具类
 * 提供权限校验的工具方法，用于判断用户是否拥有某项操作权限
 *
 * @author jishenghua
 */
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.datasource.entities.User;

/**
 * 鏉冮檺鍒ゆ柇宸ュ叿绫汇€?
 */
public final class PermissionUtil {

    private PermissionUtil() {
    }

    /**
     * 鍒ゆ柇鏄惁涓洪粯璁ょ鐞嗗憳璐﹀彿銆?
     *
     * @param loginName 鐧诲綍鍚?
     * @return true-榛樿绠＄悊鍛橈紝false-闈為粯璁ょ鐞嗗憳
     */
    public static boolean isDefaultManager(String loginName) {
        return BusinessConstants.DEFAULT_MANAGER.equals(loginName);
    }

    /**
     * 鍒ゆ柇鐢ㄦ埛瀵硅薄鏄惁涓洪粯璁ょ鐞嗗憳璐﹀彿銆?
     *
     * @param user 褰撳墠鐢ㄦ埛
     * @return true-榛樿绠＄悊鍛橈紝false-闈為粯璁ょ鐞嗗憳
     */
    public static boolean isDefaultManager(User user) {
        return user != null && isDefaultManager(user.getLoginName());
    }
}

