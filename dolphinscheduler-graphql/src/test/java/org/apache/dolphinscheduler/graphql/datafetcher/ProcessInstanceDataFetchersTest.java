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

public class ProcessInstanceDataFetchersTest extends AbstractDataFetchersTest {

    private static Logger logger = LoggerFactory.getLogger(ProcessInstanceDataFetchersTest.class);

    @Test
    public void testQueryProcessInstanceList() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query queryProcessInstanceList {\n" +
                        "    queryProcessInstanceList(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }\n" +
                        "        projectCode: \"1\"\n" +
                        "        pageNo: 1\n" +
                        "        pageSize: 3\n" +
                        "    ) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        data {\n" +
                        "            totalList {\n" +
                        "                id\n" +
                        "                code\n" +
                        "                name\n" +
                        "                version\n" +
                        "                releaseState\n" +
                        "                projectId\n" +
                        "                processDefinitionJson\n" +
                        "                description\n" +
                        "                globalParams\n" +
                        "                globalParamList {\n" +
                        "                    prop\n" +
                        "                    direct\n" +
                        "                    type\n" +
                        "                    value\n" +
                        "                }\n" +
                        "                globalParamMap\n" +
                        "                createTime\n" +
                        "                updateTime\n" +
                        "                flag\n" +
                        "                userId\n" +
                        "                userName\n" +
                        "                projectName\n" +
                        "                locations\n" +
                        "                connects\n" +
                        "                scheduleReleaseState\n" +
                        "                timeout\n" +
                        "                tenantId\n" +
                        "                modifyBy\n" +
                        "                resourceIds\n" +
                        "                warningGroupId\n" +
                        "            }\n" +
                        "            total\n" +
                        "            totalPage\n" +
                        "            currentPage\n" +
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
    public void testQueryTaskListByProcessId() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query queryTaskListByProcessId {\n" +
                        "    queryTaskListByProcessId(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }\n" +
                        "        projectCode: \"1\"\n" +
                        "        id: 2\n" +
                        "    ) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        data {\n" +
                        "            processInstanceState\n" +
                        "            taskList {\n" +
                        "                id\n" +
                        "                name\n" +
                        "                taskType\n" +
                        "                processInstanceId\n" +
                        "                taskCode\n" +
                        "                taskDefinitionVersion\n" +
                        "                processInstanceName\n" +
                        "                state\n" +
                        "                firstSubmitTime\n" +
                        "                submitTime\n" +
                        "                startTime\n" +
                        "                endTime\n" +
                        "                host\n" +
                        "                executePath\n" +
                        "                logPath\n" +
                        "                retryTimes\n" +
                        "                alertFlag\n" +
                        "                processInstance {\n" +
                        "                    id\n" +
                        "                    processDefinitionCode\n" +
                        "                    processDefinitionVersion\n" +
                        "                    state\n" +
                        "                    recovery\n" +
                        "                    startTime\n" +
                        "                    endTime\n" +
                        "                    runTimes\n" +
                        "                    name\n" +
                        "                    host\n" +
                        "                    processDefinition {\n" +
                        "                        id\n" +
                        "                        code\n" +
                        "                        name\n" +
                        "                        version\n" +
                        "                        releaseState\n" +
                        "                        projectId\n" +
                        "                        projectCode\n" +
                        "                        processDefinitionJson\n" +
                        "                        description\n" +
                        "                        globalParams\n" +
                        "                        globalParamList {\n" +
                        "                            prop\n" +
                        "                            direct\n" +
                        "                            type\n" +
                        "                            value\n" +
                        "                        }\n" +
                        "                        globalParamMap\n" +
                        "                        createTime\n" +
                        "                        updateTime\n" +
                        "                        flag\n" +
                        "                        userId\n" +
                        "                        projectName\n" +
                        "                        locations\n" +
                        "                        connects\n" +
                        "                        scheduleReleaseState\n" +
                        "                        timeout\n" +
                        "                        tenantId\n" +
                        "                        modifyBy\n" +
                        "                        resourceIds\n" +
                        "                        warningGroupId\n" +
                        "                    }\n" +
                        "                    commandType\n" +
                        "                    taskDependType\n" +
                        "                    maxTryTimes\n" +
                        "                    failureStrategy\n" +
                        "                    warningType\n" +
                        "                    scheduleTime\n" +
                        "                    commandStartTime\n" +
                        "                    globalParams\n" +
                        "                    processInstanceJson\n" +
                        "                    executorId\n" +
                        "                    tenantCode\n" +
                        "                    queue\n" +
                        "                    isSubProcess\n" +
                        "                    locations\n" +
                        "                    connects\n" +
                        "                    historyCmd\n" +
                        "                    dependenceScheduleTimes\n" +
                        "                    duration\n" +
                        "                    processInstancePriority\n" +
                        "                    workerGroup\n" +
                        "                    timeout\n" +
                        "                    tenantId\n" +
                        "                    varPool\n" +
                        "                }\n" +
                        "                processDefine {\n" +
                        "                    id\n" +
                        "                    code\n" +
                        "                    name\n" +
                        "                    version\n" +
                        "                    releaseState\n" +
                        "                    projectId\n" +
                        "                    projectCode\n" +
                        "                    processDefinitionJson\n" +
                        "                    description\n" +
                        "                    globalParams\n" +
                        "                    globalParamList {\n" +
                        "                        prop\n" +
                        "                        direct\n" +
                        "                        type\n" +
                        "                        value\n" +
                        "                    }\n" +
                        "                    globalParamMap\n" +
                        "                    createTime\n" +
                        "                    updateTime\n" +
                        "                    flag\n" +
                        "                    userId\n" +
                        "                    projectName\n" +
                        "                    locations\n" +
                        "                    connects\n" +
                        "                    scheduleReleaseState\n" +
                        "                    timeout\n" +
                        "                    tenantId\n" +
                        "                    modifyBy\n" +
                        "                    resourceIds\n" +
                        "                    warningGroupId\n" +
                        "                }\n" +
                        "                taskDefine {\n" +
                        "                    id\n" +
                        "                    code\n" +
                        "                    name\n" +
                        "                    version\n" +
                        "                    description\n" +
                        "                    projectCode\n" +
                        "                    userId\n" +
                        "                    taskType\n" +
                        "                    taskParams\n" +
                        "                    taskParamMap\n" +
                        "                    flag\n" +
                        "                    taskPriority\n" +
                        "                    userName\n" +
                        "                    projectName\n" +
                        "                    workerGroup\n" +
                        "                    failRetryTimes\n" +
                        "                    failRetryInterval\n" +
                        "                    timeoutFlag\n" +
                        "                    timeoutNotifyStrategy\n" +
                        "                    timeout\n" +
                        "                    delayTime\n" +
                        "                    resourceIds\n" +
                        "                    createTime\n" +
                        "                    updateTime\n" +
                        "                }\n" +
                        "                pid\n" +
                        "                appLink\n" +
                        "                flag\n" +
                        "                dependency {\n" +
                        "                    dependTaskList {\n" +
                        "                        dependItemList {\n" +
                        "                            definitionCode\n" +
                        "                            depTask\n" +
                        "                            cycle\n" +
                        "                            dataValue\n" +
                        "                            dependResult\n" +
                        "                            status\n" +
                        "                        }\n" +
                        "                        relation\n" +
                        "                    }\n" +
                        "                }\n" +
                        "                duration\n" +
                        "                maxRetryTimes\n" +
                        "                retryInterval\n" +
                        "                taskInstancePriority\n" +
                        "                processInstancePriority\n" +
                        "                dependentResult\n" +
                        "                workerGroup\n" +
                        "                executorId\n" +
                        "                varPool\n" +
                        "                executorName\n" +
                        "                resources\n" +
                        "                delayTime\n" +
                        "                taskParams\n" +
                        "            }\n" +
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
    public void testQueryProcessInstanceById() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query queryProcessInstanceById {\n" +
                        "    queryProcessInstanceById(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"8216d4b8-3fb4-4a20-8191-d6df5b41d2d9\" }\n" +
                        "        projectCode: \"1\"\n" +
                        "        id: 1\n" +
                        "    ) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        data {\n" +
                        "            id\n" +
                        "            code\n" +
                        "            name\n" +
                        "            version\n" +
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
        Assert.assertEquals("{data={queryProcessInstanceById={code=10043, msg=user login failure, data=null, success=false, failed=true}}}",
                mvcResult.getAsyncResult().toString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQuerySubProcessInstanceByTaskId() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query querySubProcessInstanceByTaskId {\n" +
                        "    querySubProcessInstanceByTaskId(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }\n" +
                        "        projectCode: \"1\"\n" +
                        "        taskId: 1\n" +
                        "    ) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        data {\n" +
                        "            subProcessInstanceId\n" +
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
