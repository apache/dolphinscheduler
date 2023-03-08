package org.apache.dolphinscheduler.api.controller;


import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Dynamic Task controller test
 */
class DynamicTaskTypeControllerTest extends AbstractControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceControllerTest.class);

    @BeforeEach
    public void initSetUp() {
        setUp();
    }

    @AfterEach
    public void afterEach() throws Exception {
        after();
    }

    @Disabled("Query TaskCategories")
    @Test
    void testListDynamicTaskCategories() throws Exception {
        HashMap<String, Object> paramsMap = new HashMap<>();
        MvcResult mvcResult = mockMvc.perform(get("/dynamic/taskCategories")
                        .header("sessionId", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSONUtils.toJsonString(paramsMap)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        Assertions.assertTrue(result.getData() instanceof List);
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Disabled("Query TaskTypes")
    @Test
    void testListDynamicTaskTypes() throws Exception {
        HashMap<String, Object> paramsMap = new HashMap<>();
        MvcResult mvcResult = mockMvc.perform(get("/dynamic/{taskCategory}/taskTypes","Universal")
                        .header("sessionId", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSONUtils.toJsonString(paramsMap)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        Assertions.assertTrue(result.getData() instanceof List);
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}
