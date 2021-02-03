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
package org.apache.dolphinscheduler.service.process;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.task.conditions.ConditionsParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessData;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessInstanceMap;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.alibaba.fastjson.JSONObject;

/**
 * process service test
 */
public class ProcessServiceTest {


    @Test
    public void testCreateSubCommand() {

        ProcessService processService = new ProcessService();
        ProcessInstance parentInstance = new ProcessInstance();
        parentInstance.setProcessDefinitionId(1);
        parentInstance.setWarningType(WarningType.SUCCESS);
        parentInstance.setWarningGroupId(0);

        TaskInstance task = new TaskInstance();
        task.setTaskJson("{params:{processDefinitionId:100}}");
        task.setId(10);

        ProcessInstance childInstance = null;
        ProcessInstanceMap instanceMap = new ProcessInstanceMap();
        instanceMap.setParentProcessInstanceId(1);
        instanceMap.setParentTaskInstanceId(10);
        Command command = null;

        //father history: start; child null == command type: start
        parentInstance.setHistoryCmd("START_PROCESS");
        parentInstance.setCommandType(CommandType.START_PROCESS);
        command = processService.createSubProcessCommand(
                parentInstance, childInstance, instanceMap, task
        );
        Assert.assertEquals(CommandType.START_PROCESS, command.getCommandType());

        //father history: start,start failure; child null == command type: start
        parentInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        parentInstance.setHistoryCmd("START_PROCESS,START_FAILURE_TASK_PROCESS");
        command = processService.createSubProcessCommand(
                parentInstance, childInstance, instanceMap, task
        );
        Assert.assertEquals(CommandType.START_PROCESS, command.getCommandType());


        //father history: scheduler,start failure; child null == command type: scheduler
        parentInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        parentInstance.setHistoryCmd("SCHEDULER,START_FAILURE_TASK_PROCESS");
        command = processService.createSubProcessCommand(
                parentInstance, childInstance, instanceMap, task
        );
        Assert.assertEquals(CommandType.SCHEDULER, command.getCommandType());

        //father history: complement,start failure; child null == command type: complement
        parentInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        parentInstance.setHistoryCmd("COMPLEMENT_DATA,START_FAILURE_TASK_PROCESS");
        parentInstance.setCommandParam("{complementStartDate:'2020-01-01',complementEndDate:'2020-01-10'}");
        command = processService.createSubProcessCommand(
                parentInstance, childInstance, instanceMap, task
        );
        Assert.assertEquals(CommandType.COMPLEMENT_DATA, command.getCommandType());

        JSONObject complementDate = JSONUtils.parseObject(command.getCommandParam());
        Assert.assertEquals("2020-01-01", String.valueOf(complementDate.get(Constants.CMDPARAM_COMPLEMENT_DATA_START_DATE)));
        Assert.assertEquals("2020-01-10", String.valueOf(complementDate.get(Constants.CMDPARAM_COMPLEMENT_DATA_END_DATE)));

        //father history: start,failure,start failure; child not null == command type: start failure
        childInstance = new ProcessInstance();
        parentInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        parentInstance.setHistoryCmd("START_PROCESS,START_FAILURE_TASK_PROCESS");
        command = processService.createSubProcessCommand(
                parentInstance, childInstance, instanceMap, task
        );
        Assert.assertEquals(CommandType.START_FAILURE_TASK_PROCESS, command.getCommandType());

    }


    @Test
    public void testChangeJson() {

        ProcessData oldProcessData = new ProcessData();
        ConditionsParameters conditionsParameters = new ConditionsParameters();
        ArrayList<TaskNode> tasks = new ArrayList<>();
        TaskNode taskNode = new TaskNode();
        TaskNode taskNode11 = new TaskNode();
        TaskNode taskNode111 = new TaskNode();
        ArrayList<String> successNode = new ArrayList<>();
        ArrayList<String> faildNode = new ArrayList<>();

        taskNode.setName("bbb");
        taskNode.setType("SHELL");
        taskNode.setId("222");

        taskNode11.setName("vvv");
        taskNode11.setType("CONDITIONS");
        taskNode11.setId("444");
        successNode.add("bbb");
        faildNode.add("ccc");

        taskNode111.setName("ccc");
        taskNode111.setType("SHELL");
        taskNode111.setId("333");

        conditionsParameters.setSuccessNode(successNode);
        conditionsParameters.setFailedNode(faildNode);
        taskNode11.setConditionResult(conditionsParameters.getConditionResult());
        tasks.add(taskNode);
        tasks.add(taskNode11);
        tasks.add(taskNode111);
        oldProcessData.setTasks(tasks);

        ProcessData newProcessData = new ProcessData();
        ConditionsParameters conditionsParameters2 = new ConditionsParameters();
        TaskNode taskNode2 = new TaskNode();
        TaskNode taskNode22 = new TaskNode();
        TaskNode taskNode222 = new TaskNode();
        ArrayList<TaskNode> tasks2 = new ArrayList<>();
        ArrayList<String> successNode2 = new ArrayList<>();
        ArrayList<String> faildNode2 = new ArrayList<>();

        taskNode2.setName("bbbchange");
        taskNode2.setType("SHELL");
        taskNode2.setId("222");

        taskNode22.setName("vv");
        taskNode22.setType("CONDITIONS");
        taskNode22.setId("444");
        successNode2.add("bbb");
        faildNode2.add("ccc");

        taskNode222.setName("ccc");
        taskNode222.setType("SHELL");
        taskNode222.setId("333");

        conditionsParameters2.setSuccessNode(successNode2);
        conditionsParameters2.setFailedNode(faildNode2);
        taskNode22.setConditionResult(conditionsParameters2.getConditionResult());
        tasks2.add(taskNode2);
        tasks2.add(taskNode22);
        tasks2.add(taskNode222);

        newProcessData.setTasks(tasks2);

        ProcessData exceptProcessData = new ProcessData();
        ConditionsParameters conditionsParameters3 = new ConditionsParameters();
        TaskNode taskNode3 = new TaskNode();
        TaskNode taskNode33 = new TaskNode();
        TaskNode taskNode333 = new TaskNode();
        ArrayList<TaskNode> tasks3 = new ArrayList<>();
        ArrayList<String> successNode3 = new ArrayList<>();
        ArrayList<String> faildNode3 = new ArrayList<>();

        taskNode3.setName("bbbchange");
        taskNode3.setType("SHELL");
        taskNode3.setId("222");

        taskNode33.setName("vv");
        taskNode33.setType("CONDITIONS");
        taskNode33.setId("444");
        successNode3.add("bbbchange");
        faildNode3.add("ccc");

        taskNode333.setName("ccc");
        taskNode333.setType("SHELL");
        taskNode333.setId("333");

        conditionsParameters3.setSuccessNode(successNode3);
        conditionsParameters3.setFailedNode(faildNode3);
        taskNode33.setConditionResult(conditionsParameters3.getConditionResult());
        tasks3.add(taskNode3);
        tasks3.add(taskNode33);
        tasks3.add(taskNode333);
        exceptProcessData.setTasks(tasks3);

        String expect = JSONUtils.toJsonString(exceptProcessData);
        String oldJson = JSONUtils.toJsonString(oldProcessData);

        Assert.assertEquals(expect, new ProcessService().changeJson(newProcessData,oldJson));

    }
}
