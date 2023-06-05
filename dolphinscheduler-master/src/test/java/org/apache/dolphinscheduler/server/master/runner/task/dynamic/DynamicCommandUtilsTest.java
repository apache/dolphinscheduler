package org.apache.dolphinscheduler.server.master.runner.task.dynamic;

import org.apache.dolphinscheduler.common.constants.CommandKeyConstants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DynamicCommandUtilsTest {

    private ProcessInstance processInstance;
    private Long subProcessDefinitionCode;
    private Integer subProcessDefinitionVersion;
    private Map<String, String> parameters;

    @BeforeEach
    void setUp() {
        processInstance = new ProcessInstance();
        subProcessDefinitionCode = 1L;
        subProcessDefinitionVersion = 1;
        parameters = new HashMap<>();

        // Populate processInstance with some dummy data
        processInstance.setCommandType(CommandType.START_PROCESS);
        processInstance.setFailureStrategy(null); // update this
        processInstance.setWarningType(null); // update this
        processInstance.setGlobalParams("{\"prop\":\"value\"}");
        processInstance.setExecutorId(1);
        processInstance.setWarningGroupId(1);
        processInstance.setProcessInstancePriority(null); // update this
        processInstance.setWorkerGroup("worker");
        processInstance.setDryRun(0);
    }

    @Test
    void testCreateCommand() {
        Command command = DynamicCommandUtils.createCommand(processInstance, subProcessDefinitionCode,
                subProcessDefinitionVersion, parameters);

        Assertions.assertEquals(CommandType.DYNAMIC_GENERATION, command.getCommandType());
        Assertions.assertEquals(subProcessDefinitionCode, command.getProcessDefinitionCode());
        Assertions.assertEquals(subProcessDefinitionVersion, command.getProcessDefinitionVersion());
        Assertions.assertEquals(TaskDependType.TASK_POST, command.getTaskDependType());
        Assertions.assertEquals(processInstance.getFailureStrategy(), command.getFailureStrategy());
        Assertions.assertEquals(processInstance.getWarningType(), command.getWarningType());
        Assertions.assertEquals(processInstance.getExecutorId(), command.getExecutorId());
        Assertions.assertEquals(processInstance.getWarningGroupId(), command.getWarningGroupId());
        Assertions.assertEquals(processInstance.getProcessInstancePriority(), command.getProcessInstancePriority());
        Assertions.assertEquals(processInstance.getWorkerGroup(), command.getWorkerGroup());
        Assertions.assertEquals(processInstance.getDryRun(), command.getDryRun());
    }

    @Test
    void testGetDataFromCommandParam() {
        Command command = new Command();
        DynamicCommandUtils.addDataToCommandParam(command, "testKey", "testData");
        String data = DynamicCommandUtils.getDataFromCommandParam(command.getCommandParam(), "testKey");

        Assertions.assertEquals("testData", data);
    }

    @Test
    void testCreateCommandCommandType() {
        // Scenario 1: CommandType is START_PROCESS
        processInstance.setCommandType(CommandType.START_PROCESS);
        Command command1 = DynamicCommandUtils.createCommand(processInstance, subProcessDefinitionCode,
                subProcessDefinitionVersion, parameters);
        Assertions.assertEquals(CommandType.DYNAMIC_GENERATION, command1.getCommandType());

        // Scenario 2: CommandType is not START_PROCESS
        processInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        Command command2 = DynamicCommandUtils.createCommand(processInstance, subProcessDefinitionCode,
                subProcessDefinitionVersion, parameters);
        Assertions.assertEquals(CommandType.START_FAILURE_TASK_PROCESS, command2.getCommandType());
    }

    @Test
    void testCreateCommandStartParams() {
        // Scenario: Add some data to parameters
        parameters.put("testKey", "testValue");
        Command command = DynamicCommandUtils.createCommand(processInstance, subProcessDefinitionCode,
                subProcessDefinitionVersion, parameters);

        String startParamsJson = DynamicCommandUtils.getDataFromCommandParam(command.getCommandParam(),
                CommandKeyConstants.CMD_PARAM_START_PARAMS);
        Map<String, String> startParams = JSONUtils.toMap(startParamsJson);

        Assertions.assertEquals("testValue", startParams.get("testKey"));
    }

    @Test
    void testCreateCommandGlobalParams() {
        // Scenario: processInstance has globalParams
        parameters.put("testKey", "testValue");
        processInstance.setGlobalParams("[{\"prop\":\"globalKey\",\"value\":\"globalValue\"}]");

        Command command = DynamicCommandUtils.createCommand(processInstance, subProcessDefinitionCode,
                subProcessDefinitionVersion, parameters);

        String startParamsJson = DynamicCommandUtils.getDataFromCommandParam(command.getCommandParam(),
                CommandKeyConstants.CMD_PARAM_START_PARAMS);
        Map<String, String> startParams = JSONUtils.toMap(startParamsJson);

        Assertions.assertEquals("testValue", startParams.get("testKey"));
        Assertions.assertEquals("globalValue", startParams.get("globalKey"));
    }

}
