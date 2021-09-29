package org.apache.dolphinscheduler.graphql.datafetcher;

import org.apache.dolphinscheduler.api.controller.AlertGroupController;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AlertGroupDataFetchersTest extends AbstractDataFetchersTest {

    private static final Logger logger = LoggerFactory.getLogger(AlertGroupDataFetchersTest.class);

    @Test
    public void testCreateAlertGroup() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "mutation createAlertGroup(\n" +
                        "  $id: ID!, \n" +
                        "  $sessionId: String!, \n" +
                        "  $groupName: String!, \n" +
                        "  $description: String, \n" +
                        "  $alertInstanceIds: String\n" +
                        ") {\n" +
                        "  createAlertGroup(\n" +
                        "    loginUser: {id: $id, sessionId: $sessionId},\n" +
                        "    groupName: $groupName,\n" +
                        "    description: $description,\n" +
                        "    alertInstanceIds: $alertInstanceIds\n" +
                        "  ) {\n" +
                        "    code\n" +
                        "    msg\n" +
                        "    data\n" +
                        "    success\n" +
                        "    failed\n" +
                        "  }\n" +
                        "}");
        paramsMap.put("variables",
                "{\n" +
                    "\"id\": 1,\n" +
                    "\"sessionId\": \"" + sessionId + "\",\n" +
                    "\"groupName\": \"cxc test group name\",\n" +
                    "\t\"description\": \"cxc junit 测试告警描述\",\n" +
                    "\"alertInstanceIds\": \"\"" +
                "}");
        MvcResult mvcResult = mockMvc.perform(post("/graphql")
                        .accept(MediaType.parseMediaType("*/*"))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(toJson(paramsMap)))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println(mvcResult.getAsyncResult());
        Assert.assertEquals(mvcResult.getAsyncResult().toString(),
                "{data={createAlertGroup={code=0, msg=success, data=null, success=true, failed=false}}}");
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testList() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query queryQueueList($id: ID!, $sessionId: String!) {\n" +
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
        Assert.assertEquals(mvcResult.getAsyncResult().toString(),
                "{data={queryQueueList={code=0, msg=success, data=[{id=1, queue=default, queueName=default, createTime=null, updateTime=null}, {id=2, queue=change, queueName=changeName, createTime=Sun Aug 15 00:00:25 CST 2021, updateTime=Sun Aug 15 00:15:36 CST 2021}], success=true, failed=false}}}");
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testListPaging() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query queryAlertGroupListPaging {\n" +
                        "    queryAlertGroupListPaging(loginUser: { id: \"1\",sessionId: \" " + sessionId + " \" }, pageNo: 1, pageSize: 5) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        data {\n" +
                        "            totalList {\n" +
                        "                id\n" +
                        "                groupName\n" +
                        "                description\n" +
                        "                createTime\n" +
                        "                updateTime\n" +
                        "                alertInstanceIds\n" +
                        "                createUserId\n" +
                        "            }\n" +
                        "            total\n" +
                        "            totalPage\n" +
                        "            currentPage\n" +
                        "        }\n" +
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
        System.out.println(mvcResult.getAsyncResult());
        Assert.assertEquals("{data={queryAlertGroupListPaging={code=10043, msg=user login failure, data=null, success=false, failed=true}}}",
                mvcResult.getAsyncResult().toString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testUpdateAlertGroup() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "mutation updateAlertGroup(\n" +
                        "  $uid: ID!,\n" +
                        "  $sessionId: String!,\n" +
                        "  $id: Int!,\n" +
                        "  $groupName: String!,\n" +
                        "  $description: String,\n" +
                        "  $alertInstanceIds: String\n" +
                        ") {\n" +
                        "  updateAlertGroup(\n" +
                        "    loginUser: {id: $uid, sessionId: $sessionId},\n" +
                        "    id: $id,\n" +
                        "    groupName: $groupName,\n" +
                        "    description: $description,\n" +
                        "    alertInstanceIds: $alertInstanceIds\n" +
                        "  ) {\n" +
                        "    code\n" +
                        "    msg\n" +
                        "    data\n" +
                        "    success\n" +
                        "    failed\n" +
                        "  }\n" +
                        "}");
        paramsMap.put("variables",
                "{\n" +
                "  \"uid\": 1,\n" +
                "  \"sessionId\": \"" + sessionId + "\",\n" +
                "  \"id\": 15,\n" +
                "  \"groupName\": \"test\",\n" +
                "  \"description\": \"hhh\",\n" +
                "  \"alertInstanceIds\": \"\"\n" +
                "}");

        MvcResult mvcResult = mockMvc.perform(post("/graphql")
                        .accept(MediaType.parseMediaType("*/*"))
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(toJson(paramsMap)))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println(mvcResult.getAsyncResult());
        Assert.assertEquals("{data={updateAlertGroup={code=0, msg=success, data=null, success=true, failed=false}}}",
                mvcResult.getAsyncResult().toString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testVerifyGroupName() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query testVerifyGroupName {\n" +
                        "  verifyGroupName(\n" +
                        "    loginUser: { id: \"1\", sessionId: \"" + sessionId +"\" },\n" +
                        "    groupName: \"cxc test group name\"\n" +
                        "  ) {\n" +
                        "    code\n" +
                        "    msg\n" +
                        "    data\n" +
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
        Assert.assertEquals("{data={verifyGroupName={code=10012, msg=alarm group already exists, data=null, success=false, failed=true}}}",
                mvcResult.getAsyncResult().toString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testVerifyGroupNameNotExit() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query testVerifyGroupName {\n" +
                        "  verifyGroupName(\n" +
                        "    loginUser: { id: \"1\", sessionId: \"" + sessionId +"\" },\n" +
                        "    groupName: \"dont\"\n" +
                        "  ) {\n" +
                        "    code\n" +
                        "    msg\n" +
                        "    data\n" +
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
        Assert.assertEquals("{data={verifyGroupName={code=0, msg=success, data=null, success=true, failed=false}}}",
                mvcResult.getAsyncResult().toString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testDelAlertGroupById() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "mutation testDelAlertgroupById {\n" +
                        "  delAlertGroupById(\n" +
                        "    loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" },\n" +
                        "    id: 18\n" +
                        "  ) {\n" +
                        "    code\n" +
                        "    msg\n" +
                        "    data\n" +
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
        Assert.assertEquals("{data={delAlertGroupById={code=10011, msg=alarm group not found, data=null, success=false, failed=true}}}",
                mvcResult.getAsyncResult().toString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}
