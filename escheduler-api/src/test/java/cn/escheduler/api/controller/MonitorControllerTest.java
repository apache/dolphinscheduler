package cn.escheduler.api.controller;

import cn.escheduler.api.enums.Status;
import cn.escheduler.api.utils.Result;
import cn.escheduler.common.utils.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * monitor controller test
 */
public class MonitorControllerTest extends AbstractControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(MonitorControllerTest.class);


    @Test
    public void listMaster() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/monitor/master/list")
                .header(SESSION_ID, sessionId)
               /* .param("type", ResourceType.FILE.name())*/   )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        result.getCode().equals(Status.SUCCESS.getCode());


        JSONObject object = (JSONObject) JSONObject.parse(mvcResult.getResponse().getContentAsString());

        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void queryDatabaseState() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/monitor/database")
                        .header(SESSION_ID, sessionId)
                /* .param("type", ResourceType.FILE.name())*/   )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        result.getCode().equals(Status.SUCCESS.getCode());


        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void queryZookeeperState() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/monitor/zookeeper/list")
                        .header(SESSION_ID, sessionId)
                /* .param("type", ResourceType.FILE.name())*/   )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        result.getCode().equals(Status.SUCCESS.getCode());



        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}