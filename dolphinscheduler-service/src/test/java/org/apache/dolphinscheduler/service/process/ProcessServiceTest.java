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
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessInstanceMap;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

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
        task.setTaskJson("{\"params\":{\"processDefinitionId\":100}}");
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

        String startString = "2020-01-01 00:00:00";
        String endString = "2020-01-10 00:00:00";
        parentInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        parentInstance.setHistoryCmd("COMPLEMENT_DATA,START_FAILURE_TASK_PROCESS");
        Map<String,String> complementMap = new HashMap<>();
        complementMap.put(Constants.CMDPARAM_COMPLEMENT_DATA_START_DATE, startString);
        complementMap.put(Constants.CMDPARAM_COMPLEMENT_DATA_END_DATE, endString);
        parentInstance.setCommandParam(JSONUtils.toJsonString(complementMap));
        command = processService.createSubProcessCommand(
                parentInstance, childInstance, instanceMap, task
        );
        Assert.assertEquals(CommandType.COMPLEMENT_DATA, command.getCommandType());

        JsonNode complementDate = JSONUtils.parseObject(command.getCommandParam());
        Date start = DateUtils.stringToDate(complementDate.get(Constants.CMDPARAM_COMPLEMENT_DATA_START_DATE).asText());
        Date end = DateUtils.stringToDate(complementDate.get(Constants.CMDPARAM_COMPLEMENT_DATA_END_DATE).asText());
        Assert.assertEquals(startString, DateUtils.dateToString(start));
        Assert.assertEquals(endString, DateUtils.dateToString(end));

        //father history: start,failure,start failure; child not null == command type: start failure
        childInstance = new ProcessInstance();
        parentInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        parentInstance.setHistoryCmd("START_PROCESS,START_FAILURE_TASK_PROCESS");
        command = processService.createSubProcessCommand(
                parentInstance, childInstance, instanceMap, task
        );
        Assert.assertEquals(CommandType.START_FAILURE_TASK_PROCESS, command.getCommandType());
    }
}
