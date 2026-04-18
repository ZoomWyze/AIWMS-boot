package com.jsh.erp.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.datasource.entities.Function;
import com.jsh.erp.datasource.entities.SystemConfig;
import com.jsh.erp.datasource.entities.User;
import com.jsh.erp.datasource.entities.UserBusiness;
import com.jsh.erp.service.FunctionService;
import com.jsh.erp.service.SystemConfigService;
import com.jsh.erp.service.UserBusinessService;
import com.jsh.erp.service.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

/**
 * 菜单接口回归测试。
 */
@RunWith(MockitoJUnitRunner.class)
public class FunctionControllerMenuTest {

	@InjectMocks
	private FunctionController functionController;

	@Mock
	private FunctionService functionService;

	@Mock
	private UserService userService;

	@Mock
	private UserBusinessService userBusinessService;

	@Mock
	private SystemConfigService systemConfigService;

	@Test
	public void findMenuByPNumberShouldContainHomeMenu() throws Exception {
		JSONObject reqBody = new JSONObject();
		reqBody.put("pNumber", "0");
		reqBody.put("userId", "2");

		UserBusiness roleUb = new UserBusiness();
		roleUb.setValue("[1]");
		UserBusiness funUb = new UserBusiness();
		funUb.setValue("[1]");
		when(userBusinessService.getBasicData("2", "UserRole")).thenReturn(Collections.singletonList(roleUb));
		when(userBusinessService.getBasicData("1", "RoleFunctions")).thenReturn(Collections.singletonList(funUb));

		SystemConfig cfg = new SystemConfig();
		cfg.setMultiLevelApprovalFlag("1");
		when(systemConfigService.getSystemConfig()).thenReturn(Collections.singletonList(cfg));

		Function menu = new Function();
		menu.setId(1L);
		menu.setNumber("100");
		menu.setName("采购管理");
		menu.setIcon("appstore");
		menu.setUrl("/buy");
		menu.setComponent("/layouts/RouteView");
		when(functionService.getRoleFunction("0")).thenReturn(Collections.singletonList(menu));
		when(functionService.getRoleFunction("100")).thenReturn(Collections.emptyList());

		Map<Long, Long> funIdMap = new HashMap<>();
		funIdMap.put(1L, 1L);
		when(functionService.getCurrentTenantFunIdMap()).thenReturn(funIdMap);

		User currentUser = new User();
		currentUser.setId(2L);
		currentUser.setTenantId(2L);
		currentUser.setLoginName("student");
		when(userService.getCurrentUser()).thenReturn(currentUser);

		JSONArray result = functionController.findMenuByPNumber(reqBody, new MockHttpServletRequest());

		Assert.assertNotNull(result);
		Assert.assertTrue(result.size() >= 2);
		Assert.assertEquals("首页", result.getJSONObject(0).getString("text"));
	}
}

