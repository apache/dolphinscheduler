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
package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BaseDAGServiceTest {

    @Test
    public void testProcessInstance2DAG(){

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setProcessInstanceJson("{\"globalParams\":[],\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-61567\"," +
                "\"name\":\"开始\",\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo '1'\"}," +
                "\"description\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\"," +
                "\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},\"taskInstancePriority\":\"MEDIUM\"," +
                "\"workerGroupId\":-1,\"preTasks\":[]},{\"type\":\"SHELL\",\"id\":\"tasks-6-3ug5ej\",\"name\":\"结束\"," +
                "\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo '1'\"},\"description\":\"\"," +
                "\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\"," +
                "\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},\"taskInstancePriority\":\"MEDIUM\"," +
                "\"workerGroupId\":-1,\"preTasks\":[\"开始\"]}],\"tenantId\":-1,\"timeout\":0}");

        DAG<String, TaskNode, TaskNodeRelation> relationDAG = BaseDAGService.processInstance2DAG(processInstance);

        Assert.assertTrue(relationDAG.containsNode("开始"));

    }
}
