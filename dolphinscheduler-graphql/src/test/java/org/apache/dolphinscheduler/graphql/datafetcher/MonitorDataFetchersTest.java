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

public class MonitorDataFetchersTest extends AbstractDataFetchersTest {

    private static Logger logger = LoggerFactory.getLogger(MonitorDataFetchersTest.class);

    @Test
    public void testListMaster() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query listMaster {\n" +
                        "    listMaster(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }\n" +
                        "    ) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        data {\n" +
                        "            id\n" +
                        "            host\n" +
                        "            port\n" +
                        "            zkDirectory\n" +
                        "            resInfo\n" +
                        "            createTime\n" +
                        "            lastHeartbeatTime\n" +
                        "        }\n" +
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
        Assert.assertTrue(mvcResult.getAsyncResult().toString().contains("success=true"));
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testListWorker() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query listWorker {\n" +
                        "    listWorker(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }\n" +
                        "    ) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        data {\n" +
                        "            hostname\n" +
                        "            connections\n" +
                        "            watches\n" +
                        "            sent\n" +
                        "            received\n" +
                        "            mode\n" +
                        "            minLatency\n" +
                        "            avgLatency\n" +
                        "            maxLatency\n" +
                        "            nodeCount\n" +
                        "            date\n" +
                        "            state\n" +
                        "        }\n" +
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
        Assert.assertTrue(mvcResult.getAsyncResult().toString().contains("success=true"));
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryDatabaseState() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query queryDatabaseState {\n" +
                        "    queryDatabaseState(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }\n" +
                        "    ) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        data {\n" +
                        "            dbType\n" +
                        "            state\n" +
                        "            maxConnections\n" +
                        "            maxUsedConnections\n" +
                        "            threadsConnections\n" +
                        "            threadsRunningConnections\n" +
                        "            date\n" +
                        "        }\n" +
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
        Assert.assertTrue(mvcResult.getAsyncResult().toString().contains("success=true"));
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryZookeeperState() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query queryZookeeperState {\n" +
                        "    queryZookeeperState(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }\n" +
                        "    ) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        data {\n" +
                        "            hostname\n" +
                        "            connections\n" +
                        "            watches\n" +
                        "            sent\n" +
                        "            received\n" +
                        "            mode\n" +
                        "            minLatency\n" +
                        "            avgLatency\n" +
                        "            maxLatency\n" +
                        "            nodeCount\n" +
                        "            date\n" +
                        "            state\n" +
                        "        }\n" +
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
        Assert.assertTrue(mvcResult.getAsyncResult().toString().contains("success=true"));
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}
