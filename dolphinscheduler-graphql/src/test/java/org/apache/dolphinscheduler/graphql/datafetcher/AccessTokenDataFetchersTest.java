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

public class AccessTokenDataFetchersTest extends AbstractDataFetchersTest {

    private static final Logger logger = LoggerFactory.getLogger(AccessTokenDataFetchersTest.class);

    @Test
    public void testCreateToken() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "mutation CreateToken {\n" +
                        "    createToken(\n" +
                        "        loginUser: { id: \"1\", userType: GENERAL_USER, sessionId: \"" + sessionId + "\" }\n" +
                        "        userId: 123\n" +
                        "        expireTime: \"2019-12-18 00:00:00\"\n" +
                        "        token: \"56b11bb49a883434b4fab3ce55956945\"\n" +
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
        Assert.assertEquals("{data={createToken={code=0, msg=success, data=null, success=true, failed=false}}}",
                mvcResult.getAsyncResult().toString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testGenerateToken() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query GenerateToken {\n" +
                        "    generateToken(\n" +
                        "        loginUser: { id: \"1\", userType: GENERAL_USER, sessionId: \"" + sessionId + "\" }\n" +
                        "        userId: 123\n" +
                        "        expireTime: \"2019-12-28 00:00:00\"\n" +
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
        Assert.assertTrue(mvcResult.getAsyncResult().toString().contains("success=true"));
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryAccessTokenList() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query QueryAccessTokenList {\n" +
                        "    queryAccessTokenList(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }\n" +
                        "        pageNo: 1\n" +
                        "        pageSize: 10\n" +
                        "        searchVal: \"mktb\"\n" +
                        "    ) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        success\n" +
                        "        failed\n" +
                        "        data {\n" +
                        "            totalList {\n" +
                        "                id\n" +
                        "                userId\n" +
                        "                token\n" +
                        "                expireTime\n" +
                        "                createTime\n" +
                        "                updateTime\n" +
                        "            }\n" +
                        "            totalCount\n" +
                        "            totalPage\n" +
                        "            currentPage\n" +
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
        Assert.assertTrue(mvcResult.getAsyncResult().toString().contains("success=true"));
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testDelAccessTokenById() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "mutation DelAccessTokenById {\n" +
                        "    delAccessTokenById(loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }, id: 10) {\n" +
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
        Assert.assertTrue(mvcResult.getAsyncResult().toString().contains("success=false"));
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testUpdateToken() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "mutation UpdateToken {\n" +
                        "    updateToken(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }\n" +
                        "        id: 4\n" +
                        "        userId: 2\n" +
                        "        expireTime: \"\"\n" +
                        "        token: \"hhh1\"\n" +
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
        Assert.assertTrue(mvcResult.getAsyncResult().toString().contains("success=true"));
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}
