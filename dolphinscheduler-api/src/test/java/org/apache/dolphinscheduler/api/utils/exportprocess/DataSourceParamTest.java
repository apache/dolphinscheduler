/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dolphinscheduler.api.utils.exportprocess;

import com.alibaba.fastjson.JSONObject;
import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * DataSourceParamTest
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplicationServer.class)
public class DataSourceParamTest {

    @Test
    public void testAddExportDependentSpecialParam() throws JSONException {

        String sqlJson = "{\"type\":\"SQL\",\"id\":\"tasks-27297\",\"name\":\"sql\"," +
                "\"params\":{\"type\":\"MYSQL\",\"datasource\":1,\"sql\":\"select * from test\"," +
                "\"udfs\":\"\",\"sqlType\":\"1\",\"title\":\"\",\"receivers\":\"\",\"receiversCc\":\"\",\"showType\":\"TABLE\"" +
                ",\"localParams\":[],\"connParams\":\"\"," +
                "\"preStatements\":[],\"postStatements\":[]}," +
                "\"description\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\"," +
                "\"retryInterval\":\"1\",\"timeout\":{\"strategy\":\"\"," +
                "\"enable\":false},\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1," +
                "\"preTasks\":[\"dependent\"]}";


        JSONObject taskNode = JSONUtils.parseObject(sqlJson);
        if (StringUtils.isNotEmpty(taskNode.getString("type"))) {
            String taskType = taskNode.getString("type");

            ProcessAddTaskParam addTaskParam = TaskNodeParamFactory.getByTaskType(taskType);

            JSONObject sql = addTaskParam.addExportSpecialParam(taskNode);

            JSONAssert.assertEquals(taskNode.toString(), sql.toString(), false);
        }
    }

    @Test
    public void testAddImportDependentSpecialParam() throws JSONException {
        String sqlJson = "{\"workerGroupId\":-1,\"description\":\"\",\"runFlag\":\"NORMAL\"," +
                "\"type\":\"SQL\",\"params\":{\"postStatements\":[]," +
                "\"connParams\":\"\",\"receiversCc\":\"\",\"udfs\":\"\"," +
                "\"type\":\"MYSQL\",\"title\":\"\",\"sql\":\"show tables\",\"" +
                "preStatements\":[],\"sqlType\":\"1\",\"receivers\":\"\",\"datasource\":1," +
                "\"showType\":\"TABLE\",\"localParams\":[],\"datasourceName\":\"dsmetadata\"},\"timeout\"" +
                ":{\"enable\":false,\"strategy\":\"\"},\"maxRetryTimes\":\"0\"," +
                "\"taskInstancePriority\":\"MEDIUM\",\"name\":\"mysql\",\"dependence\":{}," +
                "\"retryInterval\":\"1\",\"preTasks\":[\"dependent\"],\"id\":\"tasks-8745\"}";

        JSONObject taskNode = JSONUtils.parseObject(sqlJson);
        if (StringUtils.isNotEmpty(taskNode.getString("type"))) {
            String taskType = taskNode.getString("type");

            ProcessAddTaskParam addTaskParam = TaskNodeParamFactory.getByTaskType(taskType);

            JSONObject sql = addTaskParam.addImportSpecialParam(taskNode);

            JSONAssert.assertEquals(taskNode.toString(), sql.toString(), false);
        }
    }
}
