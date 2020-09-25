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

import org.junit.Assert;
import org.junit.Test;

import com.alibaba.fastjson.JSONObject;

/**
 *  process service test
 */
public class ProcessServiceTest {



    @Test
    public void testCreateSubCommand(){
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
        Assert.assertEquals(String.valueOf(complementDate.get(Constants.CMDPARAM_COMPLEMENT_DATA_START_DATE)), "2020-01-01");
        Assert.assertEquals(String.valueOf(complementDate.get(Constants.CMDPARAM_COMPLEMENT_DATA_END_DATE)), "2020-01-10");

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
