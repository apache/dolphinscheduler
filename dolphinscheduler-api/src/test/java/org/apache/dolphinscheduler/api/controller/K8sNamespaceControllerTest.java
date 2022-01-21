package org.apache.dolphinscheduler.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

public class K8sNamespaceControllerTest extends AbstractControllerTest {


    private static Logger logger = LoggerFactory.getLogger(K8sNamespaceControllerTest.class);

    private static final String NAMESPACE_CREATE_STRING = "namespace1";

    @Test
    public void  queryProjectListPaging() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("searchVal","");
        paramsMap.add("pageNo","1");
        paramsMap.add("pageSize","20");

        MvcResult mvcResult = mockMvc.perform(get("/k8s-namespace")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info("query list-paging namespace return result:{}", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void  createNamespace() throws Exception {

    }

    @Test
    public void  updateNamespace() throws Exception {

    }

    @Test
    public void  verifyNamespace() throws Exception {
        // queue value exist
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("namespace", NAMESPACE_CREATE_STRING);
        paramsMap.add("k8s","default");

        // success
        paramsMap.clear();
        paramsMap.add("namespace", "notExist");
        paramsMap.add("k8s","default");

        MvcResult mvcResult = mockMvc.perform(post("/k8s-namespace/verify")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
        logger.info("verify namespace return result:{}", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void  delNamespaceById() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id","1");

        MvcResult mvcResult = mockMvc.perform(post("/k8s-namespace/delete")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.QUEUE_NOT_EXIST.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}