package com.jsh.erp.filter;

import com.jsh.erp.service.RedisService;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 登录与路径安全过滤器。
 *
 * @author refactor
 */
@WebFilter(filterName = "LogCostFilter", urlPatterns = {"/*"},
        initParams = {@WebInitParam(name = "filterPath",
                      value = "/platformConfig/getPlatform#/v2/api-docs#/webjars#" +
                              "/systemConfig/static#/api/plugin/wechat/weChat/share#" +
                              "/api/plugin/general-ledger/pdf/voucher#/api/plugin/tenant-statistics/tenantClean")})
public class LogCostFilter implements Filter {

    private static final String FILTER_PATH = "filterPath";
    private static final String LOGIN_OUT = "loginOut";
    private static final Set<String> PUBLIC_URLS = new HashSet<>(Arrays.asList(
            "/doc.html",
            "/user/login",
            "/user/register",
            "/user/weixinLogin",
            "/user/weixinBind",
            "/user/registerUser",
            "/user/randomImage"
    ));
    private static final Set<String> SKIP_LOGIN_OUT_URLS = new HashSet<>(Arrays.asList(
            "/user/logout",
            "/function/findMenuByPNumber"
    ));

    private String[] allowUrls;
    @Resource
    private RedisService redisService;

    @Override
    public void init(FilterConfig filterConfig) {
        String filterPath = filterConfig.getInitParameter(FILTER_PATH);
        if (!StringUtils.isEmpty(filterPath)) {
            allowUrls = filterPath.contains("#") ? filterPath.split("#") : new String[]{filterPath};
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;
        String requestUrl = servletRequest.getRequestURI();
        if (containsIllegalPathTraversal(requestUrl)) {
            servletResponse.setStatus(500);
            servletResponse.getWriter().write(LOGIN_OUT);
            return;
        }
        String contextPath = servletRequest.getContextPath();
        String requestPath = requestUrl.startsWith(contextPath) ? requestUrl.substring(contextPath.length()) : requestUrl;
        Object userId = redisService.getObjectFromSessionByKey(servletRequest, "userId");
        if (userId != null) {
            chain.doFilter(request, response);
            return;
        }
        if (PUBLIC_URLS.contains(requestPath)) {
            chain.doFilter(request, response);
            return;
        }
        if (allowUrls != null) {
            for (String url : allowUrls) {
                if (requestPath.startsWith(url)) {
                    chain.doFilter(request, response);
                    return;
                }
            }
        }
        servletResponse.setStatus(500);
        if (!SKIP_LOGIN_OUT_URLS.contains(requestPath)) {
            servletResponse.getWriter().write(LOGIN_OUT);
        }
    }

    private boolean containsIllegalPathTraversal(String requestUrl) {
        return requestUrl.contains("..") || requestUrl.contains("%2e") || requestUrl.contains("%2E");
    }

    @Override
    public void destroy() {

    }
}