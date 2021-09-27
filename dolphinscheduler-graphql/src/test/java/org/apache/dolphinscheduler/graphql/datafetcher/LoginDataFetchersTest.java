package org.apache.dolphinscheduler.graphql.datafetcher;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LoginDataFetchersTest extends AbstractDataFetchersTest {

    private static Logger logger = LoggerFactory.getLogger(LoginDataFetchersTest.class);

    @Test
    public void testLogin() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query","query login {\n" +
                "    login(\n" +
                "        userName: \"admin\"\n" +
                "        userPassword: \"dolphinscheduler123\"\n" +
                "        ip: \"127.0.0.1\"\n" +
                "    ) {\n" +
                "        code\n" +
                "        msg\n" +
                "        data\n" +
                "        success\n" +
                "        failed\n" +
                "    }\n" +
                "}");
        paramsMap.put("variables", "{}");

        MvcResult mvcResult = mockMvc.perform(post("/graphql")
                        .accept(MediaType.parseMediaType("*/*"))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(toJson(paramsMap)))
                .andExpect(status().isOk())
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
//        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}
