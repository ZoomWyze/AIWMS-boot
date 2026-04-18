package com.jsh.erp.controller;

import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.service.DepotHeadService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.mockito.Mockito.when;

/**
 * 库存单据回归测试。
 */
@RunWith(MockitoJUnitRunner.class)
public class DepotHeadControllerBillTest {

    @InjectMocks
    private DepotHeadController depotHeadController;

    @Mock
    private DepotHeadService depotHeadService;

    @Test
    public void batchSetStatusShouldReturnSuccessWhenServiceReturnsPositive() throws Exception {
        JSONObject body = new JSONObject();
        body.put("status", "1");
        body.put("ids", "1,2");

        when(depotHeadService.batchSetStatus("1", "1,2")).thenReturn(1);

        String result = depotHeadController.batchSetStatus(body, new MockHttpServletRequest());

        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains("\"code\":200"));
    }
}
