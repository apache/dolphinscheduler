package org.apache.dolphinscheduler.graphql.datafetcher;

import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DataAnalysisDataFetchersTest extends AbstractDataFetchersTest {

    private static final Logger logger = LoggerFactory.getLogger(DataAnalysisDataFetchersTest.class);

    @MockBean
    ProjectMapper projectMapper;

    @Test
    public void testCountTaskState() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query testCountTaskState {\n" +
                        "  countTaskState(\n" +
                        "    loginUser: {id: 1, sessionId: \"" + sessionId + "\"},\n" +
                        "    startDate: \"2019-12-01 00:00:00\",\n" +
                        "    endDate: \"2019-12-28 00:00:00\",\n" +
                        "    projectCode: \"333\"\n" +
                        "  ) {\n" +
                        "    code\n" +
                        "    msg\n" +
                        "    data {\n" +
                        "      totalCount\n" +
                        "      taskCountDtos {\n" +
                        "        count\n" +
                        "        taskStateType\n" +
                        "      }\n" +
                        "    }\n" +
                        "    success\n" +
                        "    failed\n" +
                        "  }\n" +
                        "}");
        paramsMap.put("variables",
                "{}");

        MvcResult mvcResult = mockMvc.perform(post("/graphql")
                        .accept(MediaType.parseMediaType("*/*"))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(toJson(paramsMap)))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println(mvcResult.getAsyncResult());
        Assert.assertEquals("{data={countTaskState={code=10018, msg=project 333 not found , data=null, success=false, failed=true}}}",
                mvcResult.getAsyncResult().toString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testCountProcessInstanceState() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query testCountProcessInstanceState {\n" +
                        "  countProcessInstanceState(\n" +
                        "    loginUser: {id: 1, sessionId: \"" + sessionId + "\"},\n" +
                        "    startDate: \"2019-12-01 00:00:00\",\n" +
                        "    endDate: \"2019-12-28 00:00:00\",\n" +
                        "    projectCode: \"16\"\n" +
                        "  ) {\n" +
                        "    code\n" +
                        "    msg\n" +
                        "    data {\n" +
                        "      totalCount\n" +
                        "      taskCountDtos {\n" +
                        "        count\n" +
                        "        taskStateType\n" +
                        "      }\n" +
                        "    }\n" +
                        "    success\n" +
                        "    failed\n" +
                        "  }\n" +
                        "}");
        paramsMap.put("variables",
                "{}");

        MvcResult mvcResult = mockMvc.perform(post("/graphql")
                        .accept(MediaType.parseMediaType("*/*"))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(toJson(paramsMap)))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println(mvcResult.getAsyncResult());
        Assert.assertEquals("{data={countProcessInstanceState={code=10018, msg=project 16 not found , data=null, success=false, failed=true}}}",
                mvcResult.getAsyncResult().toString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testCountDefinitionByUser() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query testCountProcessInstanceState {\n" +
                        "  countProcessInstanceState(\n" +
                        "    loginUser: {id: 1, sessionId: \"" + sessionId + "\"},\n" +
                        "    projectCode: \"16\"\n" +
                        "  ) {\n" +
                        "    code\n" +
                        "    msg\n" +
                        "    data {\n" +
                        "      totalCount\n" +
                        "      taskCountDtos {\n" +
                        "        count\n" +
                        "        taskStateType\n" +
                        "      }\n" +
                        "    }\n" +
                        "    success\n" +
                        "    failed\n" +
                        "  }\n" +
                        "}");
        paramsMap.put("variables",
                "{}");

        MvcResult mvcResult = mockMvc.perform(post("/graphql")
                        .accept(MediaType.parseMediaType("*/*"))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(toJson(paramsMap)))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println(mvcResult.getAsyncResult());
        Assert.assertEquals("{data={countProcessInstanceState={code=10018, msg=project 16 not found , data=null, success=false, failed=true}}}",
                mvcResult.getAsyncResult().toString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testCountCommandState() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query testCountCommandState {\n" +
                        "  countCommandState(\n" +
                        "    loginUser: {id: 1, sessionId: \"" + sessionId + "\"}\n" +
                        "  ) {\n" +
                        "    code\n" +
                        "    msg\n" +
                        "    data {\n" +
                        "      errorCount\n" +
                        "      normalCount\n" +
                        "      commandState\n" +
                        "    }\n" +
                        "    success\n" +
                        "    failed\n" +
                        "  }\n" +
                        "}");
        paramsMap.put("variables",
                "{}");

        MvcResult mvcResult = mockMvc.perform(post("/graphql")
                        .accept(MediaType.parseMediaType("*/*"))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(toJson(paramsMap)))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println(mvcResult.getAsyncResult());
        Assert.assertEquals("{data={countProcessInstanceState={code=10018, msg=project 16 not found , data=null, success=false, failed=true}}}",
                mvcResult.getAsyncResult().toString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testCountQueueState() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query CountQueueState {\n" +
                        "    countQueueState(loginUser: {id: \"1\", sessionId: \"" + sessionId + "\"}) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        data {\n" +
                        "            taskKill\n" +
                        "            taskQueue\n" +
                        "        }\n" +
                        "    }\n" +
                        "}");
        paramsMap.put("variables",
                "{}");

        MvcResult mvcResult = mockMvc.perform(post("/graphql")
                        .accept(MediaType.parseMediaType("*/*"))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(toJson(paramsMap)))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println(mvcResult.getAsyncResult());
        Assert.assertEquals("{data={countQueueState={code=0, msg=success, data={taskKill=0, taskQueue=0}}}}",
                mvcResult.getAsyncResult().toString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}
