package com.jsh.erp.exception;

/**
 * JshException 工具类 —— 异常处理辅助方法
 *
 * 提供统一的异常包装和抛出方法，简化 Service 层的异常处理代码。
 * 在 Service 方法中调用 JshException.xxx() 即可抛出标准化的业务异常。
 *
 * @author jishenghua
 */

import com.jsh.erp.constants.ExceptionConstants;
import org.slf4j.Logger;

/**
 * 封装日志打印，收集日志
 * author: ji shenghua, qq 752718 920
 */
public class JshException {

    public static void readFail(Logger logger, Exception e) {
        logger.error("异常码[{}],异常提示[{}],异常[{}]",
                ExceptionConstants.DATA_READ_FAIL_CODE, ExceptionConstants.DATA_READ_FAIL_MSG,e);
        throw new BusinessRunTimeException(ExceptionConstants.DATA_READ_FAIL_CODE,
                ExceptionConstants.DATA_READ_FAIL_MSG);
    }

    public static void writeFail(Logger logger, Exception e) {
        logger.error("异常码[{}],异常提示[{}],异常[{}]",
                ExceptionConstants.DATA_WRITE_FAIL_CODE,ExceptionConstants.DATA_WRITE_FAIL_MSG,e);
        throw new BusinessRunTimeException(ExceptionConstants.DATA_WRITE_FAIL_CODE,
                ExceptionConstants.DATA_WRITE_FAIL_MSG);
    }


}
