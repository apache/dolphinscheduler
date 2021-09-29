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

public class DataSourceDataFetchersTest extends AbstractDataFetchersTest {

    private static Logger logger = LoggerFactory.getLogger(DataSourceDataFetchersTest.class);

    @Test
    public void testQueryDataSourceList() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query QueryDataSourceList {\n" +
                        "    queryDataSourceList(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }, \n" +
                        "        dbType: MYSQL\n" +
                        "    ) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        data {\n" +
                        "            id\n" +
                        "            userId\n" +
                        "            userName\n" +
                        "            name\n" +
                        "            note\n" +
                        "            dbType\n" +
                        "            connectionParams\n" +
                        "            createTime\n" +
                        "            updateTime\n" +
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
        Assert.assertEquals("{data={queryDataSourceList={code=0, msg=success, data=[{id=2, userId=1, userName=null, name=testData, note=test, dbType=MYSQL, connectionParams={\"address\":\"jdbc:mysql://127.0.0.1:3306\",\"database\":\"mktb_blog\",\"jdbcUrl\":\"jdbc:mysql://127.0.0.1:3306/mktb_blog\",\"user\":\"root\",\"password\":\"123456\"}, createTime=Wed Aug 11 23:55:44 CST 2021, updateTime=Wed Aug 11 23:55:44 CST 2021}, {id=3, userId=2, userName=null, name=3test, note=test, dbType=MYSQL, connectionParams={\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:mysql://127.0.0.1:3306\",\"database\":\"mktb_blog\",\"jdbcUrl\":\"jdbc:mysql://127.0.0.1:3306/mktb_blog\",\"other\":\"first=1&\"}, createTime=Thu Aug 12 00:14:55 CST 2021, updateTime=Thu Aug 12 18:10:15 CST 2021}]}}}",
                mvcResult.getAsyncResult().toString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryDataSourceListPaging() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query QueryDataSourceListPaging {\n" +
                        "    queryDataSourceListPaging(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }, \n" +
                        "        pageNo: 1, \n" +
                        "        pageSize: 2\n" +
                        "    ) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        data {\n" +
                        "            totalList {\n" +
                        "                id\n" +
                        "                userId\n" +
                        "                userName\n" +
                        "                name\n" +
                        "                note\n" +
                        "                dbType\n" +
                        "                connectionParams\n" +
                        "                createTime\n" +
                        "                updateTime\n" +
                        "            }\n" +
                        "            total\n" +
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
        Assert.assertEquals("{data={queryDataSourceListPaging={code=0, msg=success, data={totalList=[{id=3, userId=2, userName=mktb, name=3test, note=test, dbType=MYSQL, connectionParams={\"user\":\"root\",\"password\":\"******\",\"address\":\"jdbc:mysql://127.0.0.1:3306\",\"database\":\"mktb_blog\",\"jdbcUrl\":\"jdbc:mysql://127.0.0.1:3306/mktb_blog\",\"other\":\"first=1&\"}, createTime=Thu Aug 12 00:14:55 CST 2021, updateTime=Thu Aug 12 18:10:15 CST 2021}, {id=2, userId=1, userName=admin, name=testData, note=test, dbType=MYSQL, connectionParams={\"address\":\"jdbc:mysql://127.0.0.1:3306\",\"database\":\"mktb_blog\",\"jdbcUrl\":\"jdbc:mysql://127.0.0.1:3306/mktb_blog\",\"user\":\"root\",\"password\":\"******\"}, createTime=Wed Aug 11 23:55:44 CST 2021, updateTime=Wed Aug 11 23:55:44 CST 2021}], total=2, totalPage=1, currentPage=1}}}}",
                mvcResult.getAsyncResult().toString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testVerifyDataSourceName() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query verifyDataSourceName {\n" +
                        "    verifyDataSourceName(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }, \n" +
                        "        name: \"testData\"\n" +
                        "    ) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        data\n" +
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
        Assert.assertEquals("{data={verifyDataSourceName={code=10015, msg=data source name already exists, data=null}}}",
                mvcResult.getAsyncResult().toString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testAuthedDatasource() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query authedDatasource {\n" +
                        "    authedDatasource(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }, \n" +
                        "        userId: 1\n" +
                        "    ) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        data {\n" +
                        "            id\n" +
                        "            userId\n" +
                        "            userName\n" +
                        "            name\n" +
                        "            note\n" +
                        "            dbType\n" +
                        "            connectionParams\n" +
                        "            createTime\n" +
                        "            updateTime\n" +
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
        Assert.assertEquals("{data={authedDatasource={code=0, msg=success, data=[{id=2, userId=1, userName=null, name=testData, note=test, dbType=MYSQL, connectionParams={\"address\":\"jdbc:mysql://127.0.0.1:3306\",\"database\":\"mktb_blog\",\"jdbcUrl\":\"jdbc:mysql://127.0.0.1:3306/mktb_blog\",\"user\":\"root\",\"password\":\"123456\"}, createTime=Wed Aug 11 23:55:44 CST 2021, updateTime=Wed Aug 11 23:55:44 CST 2021}], success=true, failed=false}}}",
                mvcResult.getAsyncResult().toString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testUnauthDatasource() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query unauthDatasource {\n" +
                        "    unauthDatasource(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }, \n" +
                        "        userId: 1\n" +
                        "    ) {\n" +
                        "        code\n" +
                        "        msg\n" +
                        "        data {\n" +
                        "            id\n" +
                        "            userId\n" +
                        "            userName\n" +
                        "            name\n" +
                        "            note\n" +
                        "            dbType\n" +
                        "            connectionParams\n" +
                        "            createTime\n" +
                        "            updateTime\n" +
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
        Assert.assertEquals("{data={unauthDatasource={code=0, msg=success, data=[{id=3, userId=2, userName=null, name=3test, note=test, dbType=MYSQL, connectionParams={\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:mysql://127.0.0.1:3306\",\"database\":\"mktb_blog\",\"jdbcUrl\":\"jdbc:mysql://127.0.0.1:3306/mktb_blog\",\"other\":\"first=1&\"}, createTime=Thu Aug 12 00:14:55 CST 2021, updateTime=Thu Aug 12 18:10:15 CST 2021}], success=true, failed=false}}}",
                mvcResult.getAsyncResult().toString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testGetKerberosStartupState() throws Exception {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("query",
                "query getKerberosStartupState {\n" +
                        "    getKerberosStartupState(\n" +
                        "        loginUser: { id: \"1\", sessionId: \"" + sessionId + "\" }\n" +
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
        Assert.assertEquals("{data={getKerberosStartupState={code=0, msg=success, data=false, success=true, failed=false}}}",
                mvcResult.getAsyncResult().toString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}


