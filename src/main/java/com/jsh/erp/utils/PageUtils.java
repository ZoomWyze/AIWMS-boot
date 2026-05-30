package com.jsh.erp.utils;


/**
 * 分页工具类
 * 提供分页参数的设置和获取方法，封装 PageHelper 的分页逻辑
 *
 * @author jishenghua
 */
import com.github.pagehelper.PageHelper;
import com.jsh.erp.base.PageDomain;
import com.jsh.erp.base.TableSupport;

/**
 * 鍒嗛〉宸ュ叿绫? * 
 * @author ji-sheng-hua
 */
public class PageUtils extends PageHelper
{
    /**
     * 璁剧疆璇锋眰鍒嗛〉鏁版嵁
     */
    public static void startPage()
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer currentPage = pageDomain.getCurrentPage();
        Integer pageSize = pageDomain.getPageSize();
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        Boolean reasonable = pageDomain.getReasonable();
        PageHelper.startPage(currentPage, pageSize, orderBy).setReasonable(reasonable);
    }

    /**
     * 娓呯悊鍒嗛〉鐨勭嚎绋嬪彉閲?     */
    public static void clearPage()
    {
        PageHelper.clearPage();
    }
}
