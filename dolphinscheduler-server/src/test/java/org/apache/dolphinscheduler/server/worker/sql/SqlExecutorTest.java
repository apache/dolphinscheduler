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
package org.apache.dolphinscheduler.server.worker.sql;

import com.alibaba.fastjson.JSONObject;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.utils.SpringApplicationContext;
import org.apache.dolphinscheduler.dao.ProcessDao;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.apache.dolphinscheduler.server.worker.task.TaskManager;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 *  python shell command executor test
 */
@Ignore
public class SqlExecutorTest {

    private static final Logger logger = LoggerFactory.getLogger(SqlExecutorTest.class);

    private ProcessDao processDao = null;

    @Before
    public void before(){
        processDao = SpringApplicationContext.getBean(ProcessDao.class);
    }

    @Test
    public void test() throws Exception {
        String nodeName = "mysql sql test";
        String taskAppId = "51_11282_263978";
        String tenantCode = "hdfs";
        int taskInstId = 7;
        sharedTestSqlTask(nodeName, taskAppId, tenantCode, taskInstId);
    }

    @Test
    public void testClickhouse() throws Exception {
        String nodeName = "ClickHouse sql test";
        String taskAppId = "1_11_20";
        String tenantCode = "default";
        int taskInstId = 20;
        sharedTestSqlTask(nodeName, taskAppId, tenantCode, taskInstId);
    }

    @Test
    public void testOracle() throws Exception {
        String nodeName = "oracle sql test";
        String taskAppId = "2_13_25";
        String tenantCode = "demo";
        int taskInstId = 25;
        sharedTestSqlTask(nodeName, taskAppId, tenantCode, taskInstId);
    }

    @Test
    public void testSQLServer() throws Exception {
        String nodeName = "SQL Server sql test";
        String taskAppId = "3_14_27";
        String tenantCode = "demo";
        int taskInstId = 27;
        sharedTestSqlTask(nodeName, taskAppId, tenantCode, taskInstId);
    }

    /**
     * Basic test template for SQLTasks, mainly test different types of DBMS types
     * @param nodeName node name for selected task
     * @param taskAppId task app id
     * @param tenantCode tenant code
     * @param taskInstId task instance id
     * @throws Exception
     */
    private void sharedTestSqlTask(String nodeName, String taskAppId, String tenantCode, int taskInstId) throws Exception {
        TaskProps taskProps = new TaskProps();
        taskProps.setTaskDir("");
        // processDefineId_processInstanceId_taskInstanceId
        taskProps.setTaskAppId(taskAppId);
        // set tenant -> task execute linux user
        taskProps.setTenantCode(tenantCode);
        taskProps.setTaskStartTime(new Date());
        taskProps.setTaskTimeout(360000);
        taskProps.setTaskInstId(taskInstId);
        taskProps.setNodeName(nodeName);
        taskProps.setCmdTypeIfComplement(CommandType.START_PROCESS);


        TaskInstance taskInstance = processDao.findTaskInstanceById(taskInstId);

        String taskJson = taskInstance.getTaskJson();
        TaskNode taskNode = JSONObject.parseObject(taskJson, TaskNode.class);
        taskProps.setTaskParams(taskNode.getParams());


        // custom logger
        Logger taskLogger = LoggerFactory.getLogger(LoggerUtils.buildTaskId(LoggerUtils.TASK_LOGGER_INFO_PREFIX,
                taskInstance.getProcessDefinitionId(),
                taskInstance.getProcessInstanceId(),
                taskInstance.getId()));


        AbstractTask task = TaskManager.newTask(taskInstance.getTaskType(), taskProps, taskLogger);

        logger.info("task info : {}", task);

        // job init
        task.init();

        // job handle
        task.handle();
        ExecutionStatus status = ExecutionStatus.SUCCESS;

        if (task.getExitStatusCode() == Constants.EXIT_CODE_SUCCESS){
            status = ExecutionStatus.SUCCESS;
        }else if (task.getExitStatusCode() == Constants.EXIT_CODE_KILL){
            status = ExecutionStatus.KILL;
        }else {
            status = ExecutionStatus.FAILURE;
        }

        logger.info(status.toString());
    }
}