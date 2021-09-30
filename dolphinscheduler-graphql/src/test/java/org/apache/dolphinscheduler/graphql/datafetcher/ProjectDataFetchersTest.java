package org.apache.dolphinscheduler.graphql.datafetcher;

import org.apache.dolphinscheduler.api.controller.ProjectController;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProjectDataFetchersTest extends AbstractDataFetchersTest {

    private static Logger logger = LoggerFactory.getLogger(ProjectDataFetchersTest.class);

    @InjectMocks
    private ProjectController projectController;

    @Mock
    private ProjectServiceImpl projectService;

    @Mock
    private ProjectMapper projectMapper;

    protected User user;



    @Test
    public void testUpdateProject() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "mutation updateProject {\n" +
                        "    updateProject(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }\n" +
                        "        code: \"2\"\n" +
                        "        projectName: \"new_test1\"\n" +
                        "        description: \"createProject Mutation test\"\n" +
                        "        userName: \"admin\"\n" +
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
        Assert.assertEquals("{data={updateProject={code=10018, msg=project  not found , data=null, success=false, failed=true}}}",
                mvcResult.getAsyncResult().toString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryProjectByCode() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query queryProjectByCode {\n" +
                        "    queryProjectByCode(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }\n" +
                        "        code: \"2\"\n" +
                        "    ) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        data {\n" +
                        "            id\n" +
                        "            userId\n" +
                        "            userName\n" +
                        "            code\n" +
                        "            name\n" +
                        "            defCount\n" +
                        "            createTime\n" +
                        "            updateTime\n" +
                        "            perm\n" +
                        "            defCount\n" +
                        "            instRunningCount\n" +
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
        Assert.assertEquals("{data={queryProjectByCode={code=10018, msg=project  not found , data=null, success=false, failed=true}}}",
                mvcResult.getAsyncResult().toString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryProjectListPaging() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query queryProjectListPaging {\n" +
                        "    queryProjectListPaging(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }\n" +
                        "        searchVal: \"\"\n" +
                        "        pageNo: 1\n" +
                        "        pageSize: 2\n" +
                        "    ) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        data {\n" +
                        "            totalList {\n" +
                        "                id\n" +
                        "                userId\n" +
                        "                userName\n" +
                        "                code\n" +
                        "                name\n" +
                        "                defCount\n" +
                        "                createTime\n" +
                        "                updateTime\n" +
                        "                perm\n" +
                        "                defCount\n" +
                        "                instRunningCount\n" +
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
    public void testQueryUnauthorizedProject() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query queryUnauthorizedProject {\n" +
                        "    queryUnauthorizedProject(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }\n" +
                        "        userId: 2\n" +
                        "    ) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        data {\n" +
                        "            id\n" +
                        "            userId\n" +
                        "            userName\n" +
                        "            code\n" +
                        "            name\n" +
                        "            defCount\n" +
                        "            createTime\n" +
                        "            updateTime\n" +
                        "            perm\n" +
                        "            defCount\n" +
                        "            instRunningCount\n" +
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
    public void testQueryAuthorizedProject() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query queryAuthorizedProject {\n" +
                        "    queryAuthorizedProject(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }\n" +
                        "        userId: 2\n" +
                        "    ) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        data {\n" +
                        "            id\n" +
                        "            userId\n" +
                        "            userName\n" +
                        "            code\n" +
                        "            name\n" +
                        "            defCount\n" +
                        "            createTime\n" +
                        "            updateTime\n" +
                        "            perm\n" +
                        "            defCount\n" +
                        "            instRunningCount\n" +
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
    public void testQueryAllProjectList() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query queryAllProjectList {\n" +
                        "    queryAllProjectList(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }\n" +
                        "    ) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        data {\n" +
                        "            id\n" +
                        "            userId\n" +
                        "            userName\n" +
                        "            code\n" +
                        "            name\n" +
                        "            defCount\n" +
                        "            createTime\n" +
                        "            updateTime\n" +
                        "            perm\n" +
                        "            defCount\n" +
                        "            instRunningCount\n" +
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
