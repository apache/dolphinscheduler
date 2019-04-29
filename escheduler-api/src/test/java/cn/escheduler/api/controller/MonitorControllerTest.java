package cn.escheduler.api.controller;

import cn.escheduler.api.enums.Status;
import cn.escheduler.api.utils.Result;
import cn.escheduler.common.enums.ResourceType;
import cn.escheduler.common.utils.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MonitorControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(MonitorControllerTest.class);
    public static final String SESSION_ID = "sessionId";
    public static String SESSION_ID_VALUE;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;



    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        SESSION_ID_VALUE = "bad76fc4-2eb4-4aae-b32b-d650e4beb6af";
    }

    @Test
    public void listMaster() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/monitor/master/list")
                .header(SESSION_ID, SESSION_ID_VALUE)
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
                        .header(SESSION_ID, SESSION_ID_VALUE)
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
                        .header(SESSION_ID, SESSION_ID_VALUE)
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