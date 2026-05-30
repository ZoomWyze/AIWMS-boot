package com.jsh.erp.utils;

import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.datasource.entities.User;

/**
 * 权限判断工具类。
 */
public final class PermissionUtil {

    private PermissionUtil() {
    }

    /**
     * 判断是否为默认管理员账号。
     *
     * @param loginName 登录名
     * @return true-默认管理员，false-非默认管理员
     */
    public static boolean isDefaultManager(String loginName) {
        return BusinessConstants.DEFAULT_MANAGER.equals(loginName);
    }

    /**
     * 判断用户对象是否为默认管理员账号。
     *
     * @param user 当前用户
     * @return true-默认管理员，false-非默认管理员
     */
    public static boolean isDefaultManager(User user) {
        return user != null && isDefaultManager(user.getLoginName());
    }
}

