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
package cn.escheduler.server.master;

import cn.escheduler.common.enums.ExecutionStatus;
import cn.escheduler.dao.datasource.ConnectionFactory;
import cn.escheduler.dao.mapper.ProcessDefinitionMapper;
import cn.escheduler.dao.mapper.ProcessInstanceMapper;
import cn.escheduler.dao.mapper.TaskInstanceMapper;
import cn.escheduler.dao.model.ProcessDefinition;
import cn.escheduler.dao.model.ProcessInstance;
import cn.escheduler.dao.model.TaskInstance;
import cn.escheduler.server.utils.AlertManager;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 *  alert manager test
 */
@Ignore
public class AlertManagerTest {

    private static final Logger logger = LoggerFactory.getLogger(AlertManagerTest.class);

    ProcessDefinitionMapper processDefinitionMapper;
    ProcessInstanceMapper processInstanceMapper;
    TaskInstanceMapper taskInstanceMapper;

    AlertManager alertManager;

    @Before
    public void before(){
        processDefinitionMapper = ConnectionFactory.getSqlSession().getMapper(ProcessDefinitionMapper.class);
        processInstanceMapper = ConnectionFactory.getSqlSession().getMapper(ProcessInstanceMapper.class);
        taskInstanceMapper = ConnectionFactory.getSqlSession().getMapper(TaskInstanceMapper.class);
        alertManager = new AlertManager();
    }

    /**
     * send worker alert fault tolerance
     */
    @Test
    public void sendWarnningWorkerleranceFaultTest(){
        // process instance
        ProcessInstance processInstance = processInstanceMapper.queryDetailById(13028);

        // set process definition
        ProcessDefinition processDefinition = processDefinitionMapper.queryByDefineId(47);
        processInstance.setProcessDefinition(processDefinition);


        // fault task instance
        TaskInstance toleranceTask1 = taskInstanceMapper.queryById(5038);
        TaskInstance toleranceTask2 = taskInstanceMapper.queryById(5039);

        List<TaskInstance> toleranceTaskList = new ArrayList<>(2);
        toleranceTaskList.add(toleranceTask1);
        toleranceTaskList.add(toleranceTask2);

        alertManager.sendAlertWorkerToleranceFault(processInstance, toleranceTaskList);
    }


    /**
     * send worker alert fault tolerance
     */
    @Test
    public void sendWarnningOfProcessInstanceTest(){
        // process instance
        ProcessInstance processInstance = processInstanceMapper.queryDetailById(13028);

        // set process definition
        ProcessDefinition processDefinition = processDefinitionMapper.queryByDefineId(47);
        processInstance.setProcessDefinition(processDefinition);


        // fault task instance
        TaskInstance toleranceTask1 = taskInstanceMapper.queryById(5038);
        toleranceTask1.setState(ExecutionStatus.FAILURE);
        TaskInstance toleranceTask2 = taskInstanceMapper.queryById(5039);
        toleranceTask2.setState(ExecutionStatus.FAILURE);

        List<TaskInstance> toleranceTaskList = new ArrayList<>(2);
        toleranceTaskList.add(toleranceTask1);
        toleranceTaskList.add(toleranceTask2);

        alertManager.sendAlertProcessInstance(processInstance, toleranceTaskList);
    }

}
