package com.jsh.erp.controller;

import com.jsh.erp.datasource.entities.UserEx;
import com.jsh.erp.service.UserService;
import com.jsh.erp.utils.BaseResponseInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * 用户登录回归测试。
 */
@RunWith(MockitoJUnitRunner.class)
public class UserControllerLoginTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Test
    public void loginShouldReturnSuccessWhenCredentialsAreValid() throws Exception {
        UserEx userParam = new UserEx();
        userParam.setLoginName("jsh");
        userParam.setPassword("123456");
        userParam.setCode("abcd");
        userParam.setUuid("uuid-1");

        Map<String, Object> loginData = new HashMap<>();
        loginData.put("token", "token-demo");
        loginData.put("msgTip", "user can login");

        doNothing().when(userService).validateCaptcha("abcd", "uuid-1");
        when(userService.login("jsh", "123456", org.mockito.ArgumentMatchers.any())).thenReturn(loginData);

        BaseResponseInfo response = userController.login(userParam, new MockHttpServletRequest());

        Assert.assertEquals(200, response.code);
        Assert.assertTrue(response.data instanceof Map);
        Assert.assertEquals("token-demo", ((Map<?, ?>) response.data).get("token"));
    }
}
