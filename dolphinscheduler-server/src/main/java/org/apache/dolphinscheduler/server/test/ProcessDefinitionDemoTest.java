package org.apache.dolphinscheduler.server.test;


import static org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum.PARALLEL;

import org.apache.dolphinscheduler.api.controller.BaseController;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("projects/po")
public class ProcessDefinitionDemoTest extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionDemoTest.class);

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private UserMapper userMapper;

    @PostMapping(value = "/demo1")
    public Result createProcessDefinitionDemo(){
        //get user
        User loginUser = userMapper.selectById("1");

        //get default demo tenantCode
        String tenantCode = "default";

        //create and get demo projectCode
        Project project = projectMapper.queryByName("demo");
        if (project != null) {
            logger.info("project already exists");
        }
        Date now = new Date();
        try {
            project = Project
                .newBuilder()
                .name("demo")
                .code(CodeGenerateUtils.getInstance().genCode())
                .description("")
                .userId(loginUser.getId())
                .userName(loginUser.getUserName())
                .createTime(now)
                .updateTime(now)
                .build();
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            logger.info("create project error");
        }
        if (projectMapper.insert(project) > 0) {
            logger.info("create project success");
        } else {
            logger.info("create project error");
        }
        Long projectCode = project.getCode();

        //creat process definition demo
        //shell demo
        Map<String, Object> shellResult = shellDemo(loginUser, projectCode, tenantCode);
        logger.info("create shell demo " + shellResult.get("msg"));

        //subprocess demo
        ProcessDefinition subProcess = (ProcessDefinition) shellResult.get("data");
        String subProcessCode = String.valueOf(subProcess.getCode()) ;
        Map<String, Object> subProcessResult = subProcessDemo (loginUser, projectCode, tenantCode, subProcessCode);
        logger.info("create subprocess demo " + subProcessResult.get("msg"));

        //switch demo
        Map<String, Object> switchResult = swicthDemo (loginUser, projectCode, tenantCode);
        logger.info("create switch demo " + switchResult.get("msg"));

        //condition demo
        Map<String, Object> conditionResult = conditionDemo (loginUser, projectCode, tenantCode);
        logger.info("create condition demo " + conditionResult.get("msg"));

        //dependent demo
        ProcessDefinition switchProcess = (ProcessDefinition) switchResult.get("data");
        String switchProcessCode = String.valueOf(switchProcess.getCode()) ;
        Map<String, Object> dependentResult = dependentResultDemo (loginUser, projectCode, tenantCode, subProcessCode, switchProcessCode);
        logger.info("create condition demo " + dependentResult.get("msg"));

        //parameter context demo
        Map<String, Object> parameterContextResult = parameterContextDemo (loginUser, projectCode, tenantCode);
        logger.info("create condition demo " + parameterContextResult.get("msg"));

        //clear log demo

        return returnDataList( parameterContextResult );
    }
    public Map<String, Object> dependentResultDemo(User loginUser, long projectCode, String tenantCode, String shellProcessCode, String switchProcessCode){

        //get demo taskcode
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < 2; i++) {
                taskCodes.add(CodeGenerateUtils.getInstance().genCode());
            }
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            logger.error("Task code get error, ", e);
        }
        String taskCodeFirst = String.valueOf(taskCodes.get(0)).replaceAll("\\[|\\]", "");
        String taskCodeSecond = String.valueOf(taskCodes.get(1)).replaceAll("\\[|\\]", "");
        Map<String, Object> result = processDefinitionService.createProcessDefinition(loginUser,
            projectCode,
            DemoContants.DEPENDENT_NAME,
            DemoContants.DEPENDENT_DESCRIPTION,
            DemoContants.GLOBAL_PARAMS,
            DemoContants.DEPENDENT_locations[0] + taskCodeFirst + DemoContants.DEPENDENT_locations[1] + taskCodeSecond + DemoContants.DEPENDENT_locations[2],
            DemoContants.TIMEOUT,
            tenantCode,
            DemoContants.DEPENDENT_taskRelationJson[0] + taskCodeFirst + DemoContants.DEPENDENT_taskRelationJson[1] + taskCodeFirst + DemoContants.DEPENDENT_taskRelationJson[2] + taskCodeSecond + DemoContants.DEPENDENT_taskRelationJson[3],
            DemoContants.DEPENDENT_taskDefinitionJson[0] + taskCodeFirst + DemoContants.DEPENDENT_taskDefinitionJson[1] + projectCode + DemoContants.DEPENDENT_taskDefinitionJson[2] + shellProcessCode + DemoContants.DEPENDENT_taskDefinitionJson[3] + projectCode + DemoContants.DEPENDENT_taskDefinitionJson[4] + switchProcessCode + DemoContants.DEPENDENT_taskDefinitionJson[5] + taskCodeSecond + DemoContants.DEPENDENT_taskDefinitionJson[6],
            PARALLEL);
        return result;
    }
    public Map<String, Object> parameterContextDemo(User loginUser, long projectCode, String tenantCode){

        //get demo taskcode
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < 2; i++) {
                taskCodes.add(CodeGenerateUtils.getInstance().genCode());
            }
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            logger.error("Task code get error, ", e);
        }
        String taskCodeFirst = String.valueOf(taskCodes.get(0)).replaceAll("\\[|\\]", "");
        String taskCodeSecond = String.valueOf(taskCodes.get(1)).replaceAll("\\[|\\]", "");
        Map<String, Object> result = processDefinitionService.createProcessDefinition(loginUser,
            projectCode,
            DemoContants.PARAMETER_CONTEXT_NAME,
            DemoContants.PARAMETER_CONTEXT_DESCRIPTION,
            DemoContants.PARAMETER_CONTEXT_PARAMS,
            DemoContants.PARAMETER_CONTEXT_locations[0] + taskCodeFirst + DemoContants.PARAMETER_CONTEXT_locations[1] + taskCodeSecond + DemoContants.PARAMETER_CONTEXT_locations[2],
            DemoContants.TIMEOUT,
            tenantCode,
            DemoContants.PARAMETER_CONTEXT_taskRelationJson[0] + taskCodeFirst + DemoContants.PARAMETER_CONTEXT_taskRelationJson[1] + taskCodeFirst + DemoContants.PARAMETER_CONTEXT_taskRelationJson[2] + taskCodeSecond + DemoContants.PARAMETER_CONTEXT_taskRelationJson[3],
            DemoContants.PARAMETER_CONTEXT_taskDefinitionJson[0] + taskCodeFirst + DemoContants.PARAMETER_CONTEXT_taskDefinitionJson[1] + taskCodeSecond + DemoContants.PARAMETER_CONTEXT_taskDefinitionJson[2],
            PARALLEL);
        return result;
    }
    public Map<String, Object> conditionDemo(User loginUser, long projectCode, String tenantCode){

        //get demo taskcode
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < 4; i++) {
                taskCodes.add(CodeGenerateUtils.getInstance().genCode());
            }
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            logger.error("Task code get error, ", e);
        }
        String taskCodeFirst = String.valueOf(taskCodes.get(0)).replaceAll("\\[|\\]", "");
        String taskCodeSecond = String.valueOf(taskCodes.get(1)).replaceAll("\\[|\\]", "");
        String taskCodeThird = String.valueOf(taskCodes.get(2)).replaceAll("\\[|\\]", "");
        String taskCodeFourth = String.valueOf(taskCodes.get(3)).replaceAll("\\[|\\]", "");
        Map<String, Object> result = processDefinitionService.createProcessDefinition(loginUser,
            projectCode,
            DemoContants.CONDITION_NAME,
            DemoContants.CONDITION_DESCRIPTION,
            DemoContants.GLOBAL_PARAMS,
            DemoContants.CONDITION_locations[0] + taskCodeFirst + DemoContants.CONDITION_locations[1] + taskCodeSecond + DemoContants.CONDITION_locations[2] + taskCodeThird + DemoContants.CONDITION_locations[3] + taskCodeFourth + DemoContants.CONDITION_locations[4],
            DemoContants.TIMEOUT,
            tenantCode,
            DemoContants.CONDITION_taskRelationJson[0] + taskCodeSecond + DemoContants.CONDITION_taskRelationJson[1] + taskCodeFirst + DemoContants.CONDITION_taskRelationJson[2] + taskCodeThird + DemoContants.CONDITION_taskRelationJson[3] + taskCodeFirst + DemoContants.CONDITION_taskRelationJson[4] + taskCodeFourth + DemoContants.CONDITION_taskRelationJson[5] + taskCodeSecond + DemoContants.CONDITION_taskRelationJson[6] +taskCodeFirst+
                DemoContants.CONDITION_taskRelationJson[7],
            DemoContants.CONDITION_taskDefinitionJson[0] + taskCodeFirst + DemoContants.CONDITION_taskDefinitionJson[1] + taskCodeThird + DemoContants.CONDITION_taskDefinitionJson[2] + taskCodeFourth + DemoContants.CONDITION_taskDefinitionJson[3] + taskCodeSecond + DemoContants.CONDITION_taskDefinitionJson[4] + taskCodeThird +
                DemoContants.CONDITION_taskDefinitionJson[5]+taskCodeFourth+ DemoContants.CONDITION_taskDefinitionJson[6],
            PARALLEL);
        return result;
    }
    public Map<String, Object> swicthDemo(User loginUser, long projectCode, String tenantCode){

        //get demo taskcode
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < 4; i++) {
                taskCodes.add(CodeGenerateUtils.getInstance().genCode());
            }
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            logger.error("Task code get error, ", e);
        }
        String taskCodeFirst = String.valueOf(taskCodes.get(0)).replaceAll("\\[|\\]", "");
        String taskCodeSecond = String.valueOf(taskCodes.get(1)).replaceAll("\\[|\\]", "");
        String taskCodeThird = String.valueOf(taskCodes.get(2)).replaceAll("\\[|\\]", "");
        String taskCodeFourth = String.valueOf(taskCodes.get(3)).replaceAll("\\[|\\]", "");
        Map<String, Object> result = processDefinitionService.createProcessDefinition(loginUser,
            projectCode,
            DemoContants.SWITCH_NAME,
            DemoContants.SWITCH_DESCRIPTION,
            DemoContants.SWITCH_GLOBAL_PARAMS,
            DemoContants.SWITCH_locations[0] + taskCodeFirst + DemoContants.SWITCH_locations[1] + taskCodeSecond + DemoContants.SWITCH_locations[2] + taskCodeThird + DemoContants.SWITCH_locations[3] + taskCodeFourth + DemoContants.SWITCH_locations[4],
            DemoContants.TIMEOUT,
            tenantCode,
            DemoContants.SWITCH_taskRelationJson[0] + taskCodeFirst + DemoContants.SWITCH_taskRelationJson[1] + taskCodeFirst + DemoContants.SWITCH_taskRelationJson[2] + taskCodeSecond + DemoContants.SWITCH_taskRelationJson[3] + taskCodeFirst + DemoContants.SWITCH_taskRelationJson[4] + taskCodeThird + DemoContants.SWITCH_taskRelationJson[5] + taskCodeFirst + DemoContants.SWITCH_taskRelationJson[6] + taskCodeFourth + DemoContants.SWITCH_taskRelationJson[7],
            DemoContants.SWITCH_taskDefinitionJson[0] + taskCodeFirst + DemoContants.SWITCH_taskDefinitionJson[1] + taskCodeThird + DemoContants.SWITCH_taskDefinitionJson[2] + taskCodeFourth + DemoContants.SWITCH_taskDefinitionJson[3] + taskCodeSecond + DemoContants.SWITCH_taskDefinitionJson[4] + taskCodeSecond +
                DemoContants.SWITCH_taskDefinitionJson[5]+taskCodeThird+ DemoContants.SWITCH_taskDefinitionJson[6]+taskCodeFourth+
                DemoContants.SWITCH_taskDefinitionJson[7],
            PARALLEL);
        return result;
    }
    public Map<String, Object> shellDemo(User loginUser, long projectCode, String tenantCode){

        //get demo taskcode
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < 3; i++) {
                taskCodes.add(CodeGenerateUtils.getInstance().genCode());
            }
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            logger.error("Task code get error, ", e);
        }
        String taskCodeFirst = String.valueOf(taskCodes.get(0)).replaceAll("\\[|\\]", "");
        String taskCodeSecond = String.valueOf(taskCodes.get(1)).replaceAll("\\[|\\]", "");
        String taskCodeThird = String.valueOf(taskCodes.get(2)).replaceAll("\\[|\\]", "");
        Map<String, Object> result = processDefinitionService.createProcessDefinition(loginUser,
            projectCode,
            DemoContants.SHELL_NAME,
            DemoContants.SHELL_DESCRIPTION,
            DemoContants.SHELL_GLOBAL_PARAMS,
            DemoContants.SHELL_locations[0] + taskCodeFirst + DemoContants.SHELL_locations[1] + taskCodeSecond + DemoContants.SHELL_locations[2] + taskCodeThird + DemoContants.SHELL_locations[3],
            DemoContants.TIMEOUT,
            tenantCode,
            DemoContants.SHELL_taskRelationJson[0] + taskCodeFirst + DemoContants.SHELL_taskRelationJson[1] + taskCodeFirst + DemoContants.SHELL_taskRelationJson[2] + taskCodeSecond + DemoContants.SHELL_taskRelationJson[3] + taskCodeSecond + DemoContants.SHELL_taskRelationJson[4] + taskCodeThird + DemoContants.SHELL_taskRelationJson[5],
            DemoContants.SHELL_taskDefinitionJson[0] + taskCodeFirst + DemoContants.SHELL_taskDefinitionJson[1] + taskCodeSecond + DemoContants.SHELL_taskDefinitionJson[2] + taskCodeThird + DemoContants.SHELL_taskDefinitionJson[3],
            PARALLEL);
        return result;
    }
    public Map<String, Object> subProcessDemo(User loginUser, long projectCode, String tenantCode, String subProcessCode){

        //get demo taskcode
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < 1; i++) {
                taskCodes.add(CodeGenerateUtils.getInstance().genCode());
            }
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            logger.error("Task code get error, ", e);
        }
        String taskCode = String.valueOf(taskCodes.get(0)).replaceAll("\\[|\\]", "");
        Map<String, Object> result = processDefinitionService.createProcessDefinition(loginUser,
            projectCode,
            DemoContants.SUB_PROCESS_NAME,
            DemoContants.SUB_PROCESS_DESCRIPTION,
            DemoContants.GLOBAL_PARAMS,
            DemoContants.SUB_PROCESS_locations[0] + taskCode+ DemoContants.SUB_PROCESS_locations[1],
            DemoContants.TIMEOUT,
            tenantCode,
            DemoContants.SUB_PROCESS_taskRelationJson[0] + taskCode + DemoContants.SUB_PROCESS_taskRelationJson[1],
            DemoContants.SUB_PROCESS_taskDefinitionJson[0] + taskCode + DemoContants.SUB_PROCESS_taskDefinitionJson[1] + subProcessCode + DemoContants.SUB_PROCESS_taskDefinitionJson[2],
            PARALLEL);
        return result;
    }



}
