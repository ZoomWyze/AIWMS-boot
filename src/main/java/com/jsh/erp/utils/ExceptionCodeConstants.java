package com.jsh.erp.utils;


/**
 * 异常码常量类
 * 定义系统中所有业务异常的错误码常量
 *
 * @author jishenghua
 */
public interface ExceptionCodeConstants {
    /**
     * 鐢ㄦ埛閿欒鐮佸畾涔?     */
    public class UserExceptionCode {
        /**
         * 鐢ㄦ埛涓嶅瓨鍦?         */
        public static final int USER_NOT_EXIST = 1;

        /**
         * 鐢ㄦ埛瀵嗙爜閿欒
         */
        public static final int USER_PASSWORD_ERROR = 2;

        /**
         * 鐢ㄦ埛琚姞鍏ラ粦鍚嶅崟
         */
        public static final int BLACK_USER = 3;

        /**
         * 鍙互鐧诲綍
         */
        public static final int USER_CONDITION_FIT = 4;

        /**
         * 璁块棶鏁版嵁搴撳紓甯?         */
        public static final int USER_ACCESS_EXCEPTION = 5;

        /**
         * 绉熸埛琚姞鍏ラ粦鍚嶅崟
         */
        public static final int BLACK_TENANT = 6;

        /**
         * 绉熸埛宸茬粡杩囨湡
         */
        public static final int EXPIRE_TENANT = 7;
    }
}
