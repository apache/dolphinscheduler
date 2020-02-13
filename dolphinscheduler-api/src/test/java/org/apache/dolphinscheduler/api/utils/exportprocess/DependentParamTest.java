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
 * DependentParamTest
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplicationServer.class)
public class DependentParamTest {


    @Test
    public void testAddExportDependentSpecialParam() throws JSONException {
        String dependentJson = "{\"type\":\"DEPENDENT\",\"id\":\"tasks-33787\"," +
                "\"name\":\"dependent\",\"params\":{},\"description\":\"\",\"runFlag\":\"NORMAL\"," +
                "\"dependence\":{\"relation\":\"AND\",\"dependTaskList\":[{\"relation\":\"AND\"," +
                "\"dependItemList\":[{\"projectId\":2,\"definitionId\":46,\"depTasks\":\"ALL\"," +
                "\"cycle\":\"day\",\"dateValue\":\"today\"}]}]}}";

        JSONObject taskNode = JSONUtils.parseObject(dependentJson);
        if (StringUtils.isNotEmpty(taskNode.getString("type"))) {
            String taskType = taskNode.getString("type");

            ProcessAddTaskParam addTaskParam = TaskNodeParamFactory.getByTaskType(taskType);

            JSONObject dependent = addTaskParam.addExportSpecialParam(taskNode);

            JSONAssert.assertEquals(taskNode.toString(), dependent.toString(), false);
        }

        String dependentEmpty = "{\"type\":\"DEPENDENT\",\"id\":\"tasks-33787\"," +
                "\"name\":\"dependent\",\"params\":{},\"description\":\"\",\"runFlag\":\"NORMAL\"}";

        JSONObject taskEmpty = JSONUtils.parseObject(dependentEmpty);
        if (StringUtils.isNotEmpty(taskEmpty.getString("type"))) {
            String taskType = taskEmpty.getString("type");

            ProcessAddTaskParam addTaskParam = TaskNodeParamFactory.getByTaskType(taskType);

            JSONObject dependent = addTaskParam.addImportSpecialParam(taskEmpty);

            JSONAssert.assertEquals(taskEmpty.toString(), dependent.toString(), false);
        }

    }

    @Test
    public void testAddImportDependentSpecialParam() throws JSONException {
        String dependentJson = "{\"workerGroupId\":-1,\"description\":\"\",\"runFlag\":\"NORMAL\"" +
                ",\"type\":\"DEPENDENT\",\"params\":{},\"timeout\":{\"enable\":false," +
                "\"strategy\":\"\"},\"maxRetryTimes\":\"0\",\"taskInstancePriority\":\"MEDIUM\"" +
                ",\"name\":\"dependent\"," +
                "\"dependence\":{\"dependTaskList\":[{\"dependItemList\":[{\"dateValue\":\"today\"," +
                "\"definitionName\":\"shell-1\",\"depTasks\":\"shell-1\",\"projectName\":\"test\"," +
                "\"projectId\":1,\"cycle\":\"day\",\"definitionId\":7}],\"relation\":\"AND\"}]," +
                "\"relation\":\"AND\"},\"retryInterval\":\"1\",\"preTasks\":[],\"id\":\"tasks-55485\"}";

        JSONObject taskNode = JSONUtils.parseObject(dependentJson);
        if (StringUtils.isNotEmpty(taskNode.getString("type"))) {
            String taskType = taskNode.getString("type");

            ProcessAddTaskParam addTaskParam = TaskNodeParamFactory.getByTaskType(taskType);

            JSONObject dependent = addTaskParam.addImportSpecialParam(taskNode);

            JSONAssert.assertEquals(taskNode.toString(), dependent.toString(), false);
        }

        String dependentEmpty = "{\"workerGroupId\":-1,\"description\":\"\",\"runFlag\":\"NORMAL\"" +
                ",\"type\":\"DEPENDENT\",\"params\":{},\"timeout\":{\"enable\":false," +
                "\"strategy\":\"\"},\"maxRetryTimes\":\"0\",\"taskInstancePriority\":\"MEDIUM\"" +
                ",\"name\":\"dependent\",\"retryInterval\":\"1\",\"preTasks\":[],\"id\":\"tasks-55485\"}";

        JSONObject taskNodeEmpty = JSONUtils.parseObject(dependentEmpty);
        if (StringUtils.isNotEmpty(taskNodeEmpty.getString("type"))) {
            String taskType = taskNodeEmpty.getString("type");

            ProcessAddTaskParam addTaskParam = TaskNodeParamFactory.getByTaskType(taskType);

            JSONObject dependent = addTaskParam.addImportSpecialParam(taskNode);

            JSONAssert.assertEquals(taskNodeEmpty.toString(), dependent.toString(), false);
        }

    }
}
