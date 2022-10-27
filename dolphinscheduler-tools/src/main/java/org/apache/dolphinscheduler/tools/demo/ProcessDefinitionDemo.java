package org.apache.dolphinscheduler.tools.demo;

import static org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum.PARALLEL;

import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.EncryptionUtils;
import org.apache.dolphinscheduler.dao.entity.AccessToken;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AccessTokenMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class ProcessDefinitionDemo {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionDemo.class);

    @Value("${demo.tenant-code}")
    private String tenantCode;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AccessTokenMapper accessTokenMapper;

    @Autowired
    private ProxyProcessDefinitionController proxyProcessDefinitionController;

    public void createProcessDefinitionDemo() throws Exception {
        //get user
        User loginUser = userMapper.selectById("1");
        Date now = new Date();

        //create demo tenantCode
        CreateDemoTenant createDemoTenant = new CreateDemoTenant();
        createDemoTenant.createTenantCode(tenantCode);

        //create and get demo projectCode
        Project project = projectMapper.queryByName("demo");
        if (project != null) {
            logger.warn("Project {} already exists.", project.getName());
        }
        try {
            project = Project
                .builder()
                .name("demo")
                .code(CodeGenerateUtils.getInstance().genCode())
                .description("")
                .userId(loginUser.getId())
                .userName(loginUser.getUserName())
                .createTime(now)
                .updateTime(now)
                .build();
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            logger.error("create project error", e);
        }
        if (projectMapper.insert(project) > 0) {
            logger.info("create project success");
        } else {
            throw new Exception("create project error");
        }
        Long projectCode = project.getCode();

        // generate access token
        String expireTime = "2050-09-30 15:59:23";
        String token = EncryptionUtils.getMd5(1 + expireTime + System.currentTimeMillis());
        AccessToken accessToken = new AccessToken();
        accessToken.setUserId(1);
        accessToken.setExpireTime(DateUtils.stringToDate(expireTime));
        accessToken.setToken(token);
        accessToken.setCreateTime(new Date());
        accessToken.setUpdateTime(new Date());

        int insert = accessTokenMapper.insert(accessToken);

        if (insert > 0) {
            logger.info("create access token success");
        } else {
            logger.info("create access token error");
        }

        //creat process definition demo
        //shell demo
        ProxyResult shellResult = shellDemo(token, projectCode, tenantCode);
        logger.info("create shell demo " + shellResult.getMsg());

        //subprocess demo
        LinkedHashMap<String, Object> subProcess = (LinkedHashMap<String, Object>) shellResult.getData();
        String subProcessCode = String.valueOf(subProcess.get("code")) ;
        ProxyResult subProcessResult = subProcessDemo (token, projectCode, tenantCode, subProcessCode);
        logger.info("create subprocess demo " + subProcessResult.getMsg());

        //switch demo
        ProxyResult switchResult = swicthDemo (token, projectCode, tenantCode);
        logger.info("create switch demo " + switchResult.getMsg());

        //condition demo
        ProxyResult conditionResult = conditionDemo (token, projectCode, tenantCode);
        logger.info("create condition demo " + conditionResult.getMsg());

        //dependent demo
        LinkedHashMap<String, Object> switchProcess = (LinkedHashMap<String, Object>) switchResult.getData();
        String switchProcessCode = String.valueOf(switchProcess.get("code")) ;
        ProxyResult dependentResult = dependentProxyResultDemo (token, projectCode, tenantCode, subProcessCode, switchProcessCode);
        logger.info("create dependent demo " + dependentResult.getMsg());

        //parameter context demo
        ProxyResult parameterContextResult = parameterContextDemo (token, projectCode, tenantCode);
        logger.info("create parameter context demo " + parameterContextResult.getMsg());

        //clear log demo
        ProxyResult clearLogResult = clearLogDemo (token, projectCode, tenantCode);
        logger.info("create clear log demo " + clearLogResult.getMsg());

    }

    public ProxyResult clearLogDemo(String token, long projectCode, String tenantCode){

        //get demo taskcode
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < 1; i++) {
                taskCodes.add(CodeGenerateUtils.getInstance().genCode());
            }
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            logger.error("task code get error, ", e);
        }
        String taskCodeFirst = String.valueOf(taskCodes.get(0)).replaceAll("\\[|\\]", "");
        String absolutePath = System.getProperty("user.dir");

        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setName("demo_clear_log");
        taskDefinition.setDescription("Clear the DS log files from 30 days ago");
        taskDefinition.setTimeout(0);
        taskDefinition.setTaskParams("[]");

        ProxyResult ProxyResult = proxyProcessDefinitionController.createProcessDefinition(token, projectCode,
            taskDefinition.getName(),
            taskDefinition.getDescription(),
            taskDefinition.getTaskParams(),
            DemoContants.CLEAR_LOG_locations[0] + taskCodeFirst + DemoContants.CLEAR_LOG_locations[1],
            taskDefinition.getTimeout(),
            tenantCode,
            DemoContants.CLEAR_LOG_taskRelationJson[0] + taskCodeFirst + DemoContants.CLEAR_LOG_taskRelationJson[1],
            DemoContants.CLEAR_LOG_taskDefinitionJson[0] + taskCodeFirst + DemoContants.CLEAR_LOG_taskDefinitionJson[1] + absolutePath + DemoContants.CLEAR_LOG_taskDefinitionJson[2],
            PARALLEL);
        return ProxyResult;
    }
    public ProxyResult dependentProxyResultDemo(String token, long projectCode, String tenantCode, String shellProcessCode, String switchProcessCode){

        //get demo taskcode
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < 2; i++) {
                taskCodes.add(CodeGenerateUtils.getInstance().genCode());
            }
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            logger.error("task code get error, ", e);
        }
        String taskCodeFirst = String.valueOf(taskCodes.get(0)).replaceAll("\\[|\\]", "");
        String taskCodeSecond = String.valueOf(taskCodes.get(1)).replaceAll("\\[|\\]", "");

        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setName("demo_dependent");
        taskDefinition.setDescription("Check the completion of daily tasks");
        taskDefinition.setTimeout(0);
        taskDefinition.setTaskParams("[]");

        ProxyResult ProxyResult = proxyProcessDefinitionController.createProcessDefinition(token, projectCode,
            taskDefinition.getName(),
            taskDefinition.getDescription(),
            taskDefinition.getTaskParams(),
            DemoContants.DEPENDENT_locations[0] + taskCodeFirst + DemoContants.DEPENDENT_locations[1] + taskCodeSecond + DemoContants.DEPENDENT_locations[2],
            taskDefinition.getTimeout(),
            tenantCode,
            DemoContants.DEPENDENT_taskRelationJson[0] + taskCodeFirst + DemoContants.DEPENDENT_taskRelationJson[1] + taskCodeFirst + DemoContants.DEPENDENT_taskRelationJson[2] + taskCodeSecond + DemoContants.DEPENDENT_taskRelationJson[3],
            DemoContants.DEPENDENT_taskDefinitionJson[0] + taskCodeFirst + DemoContants.DEPENDENT_taskDefinitionJson[1] + projectCode + DemoContants.DEPENDENT_taskDefinitionJson[2] + shellProcessCode + DemoContants.DEPENDENT_taskDefinitionJson[3] + projectCode + DemoContants.DEPENDENT_taskDefinitionJson[4] + switchProcessCode + DemoContants.DEPENDENT_taskDefinitionJson[5] + taskCodeSecond + DemoContants.DEPENDENT_taskDefinitionJson[6],
            PARALLEL);
        return ProxyResult;
    }
    public ProxyResult parameterContextDemo(String token, long projectCode, String tenantCode){

        //get demo taskcode
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < 2; i++) {
                taskCodes.add(CodeGenerateUtils.getInstance().genCode());
            }
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            logger.error("task code get error, ", e);
        }
        String taskCodeFirst = String.valueOf(taskCodes.get(0)).replaceAll("\\[|\\]", "");
        String taskCodeSecond = String.valueOf(taskCodes.get(1)).replaceAll("\\[|\\]", "");

        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setName("demo_parameter_context");
        taskDefinition.setDescription("Upstream and downstream task node parameter transfer");
        taskDefinition.setTimeout(0);
        taskDefinition.setTaskParams(DemoContants.PARAMETER_CONTEXT_PARAMS);

        ProxyResult ProxyResult = proxyProcessDefinitionController.createProcessDefinition(token, projectCode,
            taskDefinition.getName(),
            taskDefinition.getDescription(),
            taskDefinition.getTaskParams(),
            DemoContants.PARAMETER_CONTEXT_locations[0] + taskCodeFirst + DemoContants.PARAMETER_CONTEXT_locations[1] + taskCodeSecond + DemoContants.PARAMETER_CONTEXT_locations[2],
            taskDefinition.getTimeout(),
            tenantCode,
            DemoContants.PARAMETER_CONTEXT_taskRelationJson[0] + taskCodeFirst + DemoContants.PARAMETER_CONTEXT_taskRelationJson[1] + taskCodeFirst + DemoContants.PARAMETER_CONTEXT_taskRelationJson[2] + taskCodeSecond + DemoContants.PARAMETER_CONTEXT_taskRelationJson[3],
            DemoContants.PARAMETER_CONTEXT_taskDefinitionJson[0] + taskCodeFirst + DemoContants.PARAMETER_CONTEXT_taskDefinitionJson[1] + taskCodeSecond + DemoContants.PARAMETER_CONTEXT_taskDefinitionJson[2],
            PARALLEL);
        return ProxyResult;
    }
    public ProxyResult conditionDemo(String token, long projectCode, String tenantCode){

        //get demo taskcode
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < 4; i++) {
                taskCodes.add(CodeGenerateUtils.getInstance().genCode());
            }
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            logger.error("task code get error, ", e);
        }
        String taskCodeFirst = String.valueOf(taskCodes.get(0)).replaceAll("\\[|\\]", "");
        String taskCodeSecond = String.valueOf(taskCodes.get(1)).replaceAll("\\[|\\]", "");
        String taskCodeThird = String.valueOf(taskCodes.get(2)).replaceAll("\\[|\\]", "");
        String taskCodeFourth = String.valueOf(taskCodes.get(3)).replaceAll("\\[|\\]", "");

        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setName("demo_condition");
        taskDefinition.setDescription("Coin Toss");
        taskDefinition.setTimeout(0);
        taskDefinition.setTaskParams("[]");

        ProxyResult ProxyResult = proxyProcessDefinitionController.createProcessDefinition(token, projectCode,
            taskDefinition.getName(),
            taskDefinition.getDescription(),
            taskDefinition.getTaskParams(),
            DemoContants.CONDITION_locations[0] + taskCodeFirst + DemoContants.CONDITION_locations[1] + taskCodeSecond + DemoContants.CONDITION_locations[2] + taskCodeThird + DemoContants.CONDITION_locations[3] + taskCodeFourth + DemoContants.CONDITION_locations[4],
            taskDefinition.getTimeout(),
            tenantCode,
            DemoContants.CONDITION_taskRelationJson[0] + taskCodeSecond + DemoContants.CONDITION_taskRelationJson[1] + taskCodeFirst + DemoContants.CONDITION_taskRelationJson[2] + taskCodeThird + DemoContants.CONDITION_taskRelationJson[3] + taskCodeFirst + DemoContants.CONDITION_taskRelationJson[4] + taskCodeFourth + DemoContants.CONDITION_taskRelationJson[5] + taskCodeSecond + DemoContants.CONDITION_taskRelationJson[6] +taskCodeFirst+
                DemoContants.CONDITION_taskRelationJson[7],
            DemoContants.CONDITION_taskDefinitionJson[0] + taskCodeFirst + DemoContants.CONDITION_taskDefinitionJson[1] + taskCodeThird + DemoContants.CONDITION_taskDefinitionJson[2] + taskCodeFourth + DemoContants.CONDITION_taskDefinitionJson[3] + taskCodeSecond + DemoContants.CONDITION_taskDefinitionJson[4] + taskCodeThird +
                DemoContants.CONDITION_taskDefinitionJson[5]+taskCodeFourth+ DemoContants.CONDITION_taskDefinitionJson[6],
            PARALLEL);
        return ProxyResult;
    }
    public ProxyResult swicthDemo(String token, long projectCode, String tenantCode){

        //get demo taskcode
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < 4; i++) {
                taskCodes.add(CodeGenerateUtils.getInstance().genCode());
            }
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            logger.error("task code get error, ", e);
        }
        String taskCodeFirst = String.valueOf(taskCodes.get(0)).replaceAll("\\[|\\]", "");
        String taskCodeSecond = String.valueOf(taskCodes.get(1)).replaceAll("\\[|\\]", "");
        String taskCodeThird = String.valueOf(taskCodes.get(2)).replaceAll("\\[|\\]", "");
        String taskCodeFourth = String.valueOf(taskCodes.get(3)).replaceAll("\\[|\\]", "");

        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setName("demo_switch");
        taskDefinition.setDescription("Determine which task to perform based on conditions");
        taskDefinition.setTimeout(0);
        taskDefinition.setTaskParams(DemoContants.SWITCH_GLOBAL_PARAMS);

        ProxyResult ProxyResult = proxyProcessDefinitionController.createProcessDefinition(token, projectCode,
            taskDefinition.getName(),
            taskDefinition.getDescription(),
            taskDefinition.getTaskParams(),
            DemoContants.SWITCH_locations[0] + taskCodeFirst + DemoContants.SWITCH_locations[1] + taskCodeSecond + DemoContants.SWITCH_locations[2] + taskCodeThird + DemoContants.SWITCH_locations[3] + taskCodeFourth + DemoContants.SWITCH_locations[4],
            taskDefinition.getTimeout(),
            tenantCode,
            DemoContants.SWITCH_taskRelationJson[0] + taskCodeFirst + DemoContants.SWITCH_taskRelationJson[1] + taskCodeFirst + DemoContants.SWITCH_taskRelationJson[2] + taskCodeSecond + DemoContants.SWITCH_taskRelationJson[3] + taskCodeFirst + DemoContants.SWITCH_taskRelationJson[4] + taskCodeThird + DemoContants.SWITCH_taskRelationJson[5] + taskCodeFirst + DemoContants.SWITCH_taskRelationJson[6] + taskCodeFourth + DemoContants.SWITCH_taskRelationJson[7],
            DemoContants.SWITCH_taskDefinitionJson[0] + taskCodeFirst + DemoContants.SWITCH_taskDefinitionJson[1] + taskCodeThird + DemoContants.SWITCH_taskDefinitionJson[2] + taskCodeFourth + DemoContants.SWITCH_taskDefinitionJson[3] + taskCodeSecond + DemoContants.SWITCH_taskDefinitionJson[4] + taskCodeSecond +
                DemoContants.SWITCH_taskDefinitionJson[5]+taskCodeThird+ DemoContants.SWITCH_taskDefinitionJson[6]+taskCodeFourth+
                DemoContants.SWITCH_taskDefinitionJson[7],
            PARALLEL);
        return ProxyResult;
    }
    public ProxyResult shellDemo(String token, long projectCode, String tenantCode){

        //get demo taskcode
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < 3; i++) {
                taskCodes.add(CodeGenerateUtils.getInstance().genCode());
            }
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            logger.error("task code get error, ", e);
        }
        String taskCodeFirst = String.valueOf(taskCodes.get(0)).replaceAll("\\[|\\]", "");
        String taskCodeSecond = String.valueOf(taskCodes.get(1)).replaceAll("\\[|\\]", "");
        String taskCodeThird = String.valueOf(taskCodes.get(2)).replaceAll("\\[|\\]", "");

        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setName("demo_shell");
        taskDefinition.setDescription("Production, processing and sales of a series of processes");
        taskDefinition.setTimeout(0);
        taskDefinition.setTaskParams(DemoContants.SHELL_GLOBAL_PARAMS);

        ProxyResult ProxyResult = proxyProcessDefinitionController.createProcessDefinition(token, projectCode,
            taskDefinition.getName(),
            taskDefinition.getDescription(),
            taskDefinition.getTaskParams(),
            DemoContants.SHELL_locations[0] + taskCodeFirst + DemoContants.SHELL_locations[1] + taskCodeSecond + DemoContants.SHELL_locations[2] + taskCodeThird + DemoContants.SHELL_locations[3],
            taskDefinition.getTimeout(),
            tenantCode,
            DemoContants.SHELL_taskRelationJson[0] + taskCodeFirst + DemoContants.SHELL_taskRelationJson[1] + taskCodeFirst + DemoContants.SHELL_taskRelationJson[2] + taskCodeSecond + DemoContants.SHELL_taskRelationJson[3] + taskCodeSecond + DemoContants.SHELL_taskRelationJson[4] + taskCodeThird + DemoContants.SHELL_taskRelationJson[5],
            DemoContants.SHELL_taskDefinitionJson[0] + taskCodeFirst + DemoContants.SHELL_taskDefinitionJson[1] + taskCodeSecond + DemoContants.SHELL_taskDefinitionJson[2] + taskCodeThird + DemoContants.SHELL_taskDefinitionJson[3],
            PARALLEL);
        return ProxyResult;
    }
    public ProxyResult subProcessDemo(String token, long projectCode, String tenantCode, String subProcessCode){

        //get demo taskcode
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < 1; i++) {
                taskCodes.add(CodeGenerateUtils.getInstance().genCode());
            }
        } catch (CodeGenerateUtils.CodeGenerateException e) {
            logger.error("task code get error, ", e);
        }
        String taskCode = String.valueOf(taskCodes.get(0)).replaceAll("\\[|\\]", "");

        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setName("demo_sub_process");
        taskDefinition.setDescription("Start the production line");
        taskDefinition.setTimeout(0);
        taskDefinition.setTaskParams("[]");

        ProxyResult ProxyResult = proxyProcessDefinitionController.createProcessDefinition(token, projectCode,
            taskDefinition.getName(),
            taskDefinition.getDescription(),
            taskDefinition.getTaskParams(),
            DemoContants.SUB_PROCESS_locations[0] + taskCode+ DemoContants.SUB_PROCESS_locations[1],
            taskDefinition.getTimeout(),
            tenantCode,
            DemoContants.SUB_PROCESS_taskRelationJson[0] + taskCode + DemoContants.SUB_PROCESS_taskRelationJson[1],
            DemoContants.SUB_PROCESS_taskDefinitionJson[0] + taskCode + DemoContants.SUB_PROCESS_taskDefinitionJson[1] + subProcessCode + DemoContants.SUB_PROCESS_taskDefinitionJson[2],
            PARALLEL);
        return ProxyResult;
    }


}
