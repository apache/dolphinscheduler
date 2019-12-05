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
package cn.escheduler.server.worker.shell;

import cn.escheduler.common.Constants;
import cn.escheduler.common.enums.ExecutionStatus;
import cn.escheduler.common.model.TaskNode;
import cn.escheduler.dao.DaoFactory;
import cn.escheduler.dao.ProcessDao;
import cn.escheduler.dao.model.TaskInstance;
import cn.escheduler.server.utils.LoggerUtils;
import cn.escheduler.server.worker.task.AbstractTask;
import cn.escheduler.server.worker.task.TaskManager;
import cn.escheduler.server.worker.task.TaskProps;
import com.alibaba.fastjson.JSONObject;
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
public class ShellCommandExecutorTest {

    private static final Logger logger = LoggerFactory.getLogger(ShellCommandExecutorTest.class);

    private ProcessDao processDao = null;

    @Before
    public void before(){
        processDao = DaoFactory.getDaoInstance(ProcessDao.class);
    }

    @Test
    public void test() throws Exception {

        TaskProps taskProps = new TaskProps();
        // processDefineId_processInstanceId_taskInstanceId
        taskProps.setTaskDir("/opt/soft/program/tmp/escheduler/exec/flow/5/36/2864/7657");
        taskProps.setTaskAppId("36_2864_7657");
        // set tenant -> task execute linux user
        taskProps.setTenantCode("hdfs");
        taskProps.setTaskStartTime(new Date());
        taskProps.setTaskTimeout(360000);
        taskProps.setTaskInstId(7657);



        TaskInstance taskInstance = processDao.findTaskInstanceById(7657);

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