package org.apache.dolphinscheduler.tools.datasource;


import static org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum.PARALLEL;


import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class ProcessDefinitionDemo {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionDemo.class);

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private UserMapper userMapper;

    public void createProcessDefinitionDemo(){

        User loginUser = userMapper.selectById("1");
        Map<String, Object> result = new HashMap<>();

        //get demo tenantCode
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
            result.put(Constants.DATA_LIST, project);
            logger.info("create project success");
        } else {
            logger.info("create project error");
        }
        Long projectCode = project.getCode();

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

        //creat process definition demo
        result = shellDemo(loginUser, projectCode, taskCodeFirst, taskCodeSecond, taskCodeThird, tenantCode);
        logger.info(String.valueOf(result));
    }

    public Map<String, Object>  shellDemo(User loginUser, long projectCode, String taskCodeFirst, String taskCodeSecond, String taskCodeThird, String tenantCode){
        Map<String, Object> result = processDefinitionService.createProcessDefinition(loginUser,
            projectCode,
            DemoContants.name,
            DemoContants.description,
            DemoContants.globalParams,
            DemoContants.locations[0]+taskCodeFirst+DemoContants.locations[1]+taskCodeSecond+DemoContants.locations[2]+taskCodeThird+DemoContants.locations[3],
            DemoContants.timeout,
            tenantCode,
            DemoContants.taskRelationJson[0]+taskCodeFirst+DemoContants.taskRelationJson[1]+taskCodeFirst+DemoContants.taskRelationJson[2]+taskCodeSecond+DemoContants.taskRelationJson[3]+taskCodeSecond+DemoContants.taskRelationJson[4]+taskCodeThird+DemoContants.taskRelationJson[5],
            DemoContants.taskDefinitionJson[0]+taskCodeFirst+DemoContants.taskDefinitionJson[1]+taskCodeSecond+DemoContants.taskDefinitionJson[2]+taskCodeThird+DemoContants.taskDefinitionJson[3],
            PARALLEL);
        return result;
    }


}
