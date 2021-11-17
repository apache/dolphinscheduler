package org.apache.dolphinscheduler.api.controller;

import com.google.gson.Gson;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ProcessTaskRelationService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * process task relation controller test
 */
public class ProcessTaskRelationControllerTest extends AbstractControllerTest {

    @MockBean
    private ProcessTaskRelationService processTaskRelationService;

    @Test
    public void testQueryDownstreamRelation() throws Exception {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put(Constants.STATUS, Status.SUCCESS);
        PowerMockito.when(processTaskRelationService.queryDownstreamRelation(Mockito.any(), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(mockResult);

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectCode}/process-task-relation/{taskCode}/downstream", "1113", "123")
                .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        System.out.println(new Gson().toJson(result));
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void testQueryUpstreamRelation() throws Exception  {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put(Constants.STATUS, Status.SUCCESS);
        PowerMockito.when(processTaskRelationService.queryUpstreamRelation(Mockito.any(), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(mockResult);

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectCode}/process-task-relation/{taskCode}/upstream", "1113", "123")
                .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        System.out.println(new Gson().toJson(result));
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }
}
