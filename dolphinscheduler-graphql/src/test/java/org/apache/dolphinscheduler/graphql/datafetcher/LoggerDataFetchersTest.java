package org.apache.dolphinscheduler.graphql.datafetcher;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LoggerDataFetchersTest extends AbstractDataFetchersTest {

    private static Logger logger = LoggerFactory.getLogger(LoggerDataFetchersTest.class);

    @Test
    public void testQueryLog() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query queryLog {\n" +
                        "    queryLog(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" },\n" +
                        "        taskInstanceId: 1,\n" +
                        "        skipNum: 0,\n" +
                        "        limit: 10\n" +
                        "    ) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        data\n" +
                        "        success\n" +
                        "        failed\n" +
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
        Assert.assertEquals("{data={queryLog={code=0, msg=success, data=[LOG-PATH]: logs/, [HOST]:  127.0.0.1\n" +
                        ", success=true, failed=false}}}",
                mvcResult.getAsyncResult().toString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testDownloadTaskLog() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query downloadTaskLog {\n" +
                        "    downloadTaskLog(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" },\n" +
                        "        taskInstanceId: 1\n" +
                        "    ) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        data\n" +
                        "        success\n" +
                        "        failed\n" +
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
        Assert.assertEquals("{data={downloadTaskLog={code=null, msg=null, data=null, success=null, failed=null}}}",
                mvcResult.getAsyncResult().toString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}
