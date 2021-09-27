package org.apache.dolphinscheduler.graphql.datafetcher;

import org.apache.dolphinscheduler.api.controller.AlertGroupController;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AlertGroupDataFetchersTest extends AbstractDataFetchersTest {

    private static final Logger logger = LoggerFactory.getLogger(AlertGroupController.class);

    @Test
    public void testList() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query", "query queryQueueList($id: ID!, $sessionId: String!) {\n" +
                "  queryQueueList(\n" +
                "    loginUser: {id: $id, sessionId: $sessionId}\n" +
                "  ) {\n" +
                "    code\n" +
                "        msg\n" +
                "        data {\n" +
                "            id\n" +
                "            queue\n" +
                "            queueName\n" +
                "            createTime\n" +
                "            updateTime\n" +
                "        }\n" +
                "        success\n" +
                "        failed\n" +
                "  }\n" +
                "}");
        paramsMap.put("variables", "{\n" +
                "  \"id\": \"1\",\n" +
                "  \"sessionId\": \""+ sessionId +"\"\n" +
                "}");

        System.out.println(toJson(paramsMap));
        MvcResult mvcResult = mockMvc.perform(post("/graphql")
                        .accept(MediaType.parseMediaType("*/*"))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(toJson(paramsMap)))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println(mvcResult.getAsyncResult());
        MockHttpServletResponse response = mvcResult.getResponse();
        Result result = JSONUtils.parseObject(mvcResult.getAsyncResult().toString(), Result.class);
        System.out.println(result);
        //Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());

    }
}
