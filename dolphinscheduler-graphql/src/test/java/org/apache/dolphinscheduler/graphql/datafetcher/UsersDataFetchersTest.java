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

public class UsersDataFetchersTest extends AbstractDataFetchersTest {

    private static Logger logger = LoggerFactory.getLogger(UsersDataFetchersTest.class);

    @Test
    public void testBatchActivateUser() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query batchActivateUser {\n" +
                        "    batchActivateUser(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }\n" +
                        "        userNames: \"test, test1\"\n" +
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
