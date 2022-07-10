package org.apache.dolphinscheduler.api.test.cases;

import io.restassured.response.Response;

import org.apache.dolphinscheduler.api.test.base.AbstractAPITest;
import org.apache.dolphinscheduler.api.test.core.common.Constants;
import org.apache.dolphinscheduler.api.test.core.extensions.DolphinScheduler;
import org.apache.dolphinscheduler.api.test.pages.projects.project.ProjectPageAPI;
import org.apache.dolphinscheduler.api.test.pages.projects.project.entity.ProjectRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.project.entity.ProjectResponseEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.WorkFlowPageAPI;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.TaskDefinitionEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.TaskLocationsEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.TaskParamsEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.TaskRelationEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.WorkFlowDefinitionRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.WorkFlowDefinitionResponseEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.WorkFlowReleaseRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity.WorkFlowRunRequestEntity;
import org.apache.dolphinscheduler.api.test.utils.JSONUtils;
import org.apache.dolphinscheduler.api.test.utils.RestResponse;
import org.apache.dolphinscheduler.api.test.utils.Result;
import org.apache.dolphinscheduler.api.test.utils.enums.ComplementDependentMode;
import org.apache.dolphinscheduler.api.test.utils.enums.ExecCommandType;
import org.apache.dolphinscheduler.api.test.utils.enums.FailureStrategy;
import org.apache.dolphinscheduler.api.test.utils.enums.Flag;
import org.apache.dolphinscheduler.api.test.utils.enums.Priority;
import org.apache.dolphinscheduler.api.test.utils.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.api.test.utils.enums.ReleaseState;
import org.apache.dolphinscheduler.api.test.utils.enums.RunMode;
import org.apache.dolphinscheduler.api.test.utils.enums.TaskDependType;
import org.apache.dolphinscheduler.api.test.utils.enums.TimeoutFlag;
import org.apache.dolphinscheduler.api.test.utils.enums.WarningType;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import com.devskiller.jfairy.Fairy;

@DolphinScheduler(composeFiles = "docker/basic/docker-compose.yaml")
@DisplayName("WorkFlow Page API test")
public class WorkFlowAPITest extends AbstractAPITest {
    private final Fairy fairy = Fairy.create();
    private ProjectPageAPI projectPageAPI;
    private WorkFlowPageAPI workFlowPageAPI;
    private ProjectRequestEntity projectRequestEntity = null;
    private ProjectResponseEntity projectResponseEntity = null;
    private WorkFlowDefinitionResponseEntity workFlowDefinitionResponseEntity = null;

    @BeforeAll
    public void initWorkFlowAPIFactory() {
        projectPageAPI = pageAPIFactory.createProjectPageAPI();
        workFlowPageAPI = pageAPIFactory.createWorkFlowPageAPI();
        projectRequestEntity = new ProjectRequestEntity();
        projectRequestEntity.setProjectName(fairy.person().getCompany().getName() + "1");
        projectRequestEntity.setDescription(fairy.person().getFullName());
        RestResponse<Result> response = projectPageAPI.createProject(projectRequestEntity);
        response.isResponseSuccessful();
        projectResponseEntity = response.getResponse().jsonPath().getObject(Constants.DATA_KEY, ProjectResponseEntity.class);
    }

    @Test
    @Order(1)
    public void testCreateWorkFlow() {
        WorkFlowDefinitionRequestEntity workFlowDefinitionRequestEntity = new WorkFlowDefinitionRequestEntity();
        workFlowDefinitionRequestEntity.setName(fairy.person().getFirstName());
        workFlowDefinitionRequestEntity.setTenantCode("default");
        workFlowDefinitionRequestEntity.setExecutionType(ProcessExecutionTypeEnum.PARALLEL);
        workFlowDefinitionRequestEntity.setDescription("");
        workFlowDefinitionRequestEntity.setTimeout(0);

        TaskParamsEntity taskParamsEntity = new TaskParamsEntity();
        taskParamsEntity.setLocalParams(new ArrayList<>());
        taskParamsEntity.setRawScript("echo shimin.an");
        taskParamsEntity.setResourceList(new ArrayList<>());

        TaskRelationEntity taskRelationEntity = new TaskRelationEntity();
        taskRelationEntity.setName("");
        taskRelationEntity.setPreTaskCode(0);
        taskRelationEntity.setPostTaskVersion(0);
        taskRelationEntity.setPostTaskCode(projectResponseEntity.getCode());
        taskRelationEntity.setPostTaskVersion(0);
        taskRelationEntity.setConditionType("NONE");
        taskRelationEntity.setConditionParams(new HashMap<>());

        TaskLocationsEntity taskLocationsEntity = new TaskLocationsEntity();
        taskLocationsEntity.setTaskCode(projectResponseEntity.getCode());
        taskLocationsEntity.setX("44");
        taskLocationsEntity.setY("59");

        TaskDefinitionEntity taskDefinitionEntity = new TaskDefinitionEntity();
        taskDefinitionEntity.setDescription("");
        taskDefinitionEntity.setCode(projectResponseEntity.getCode());
        taskDefinitionEntity.setDelayTime("0");
        taskDefinitionEntity.setEnvironmentCode(-1);
        taskDefinitionEntity.setFailRetryInterval("1");
        taskDefinitionEntity.setFailRetryTimes("0");
        taskDefinitionEntity.setFlag(Flag.YES);
        taskDefinitionEntity.setName("shell");
        taskDefinitionEntity.setTaskParams(taskParamsEntity);
        taskDefinitionEntity.setTaskPriority(Priority.MEDIUM);
        taskDefinitionEntity.setTaskType("SHELL");
        taskDefinitionEntity.setTimeout(0);
        taskDefinitionEntity.setTimeoutFlag(TimeoutFlag.CLOSE);
        taskDefinitionEntity.setTimeoutNotifyStrategy("");
        taskDefinitionEntity.setWorkerGroup("default");
        taskDefinitionEntity.setCpuQuota(-1);
        taskDefinitionEntity.setMemoryMax(-1);

        ArrayList<TaskDefinitionEntity> taskDefinitionEntities = new ArrayList<>();
        taskDefinitionEntities.add(taskDefinitionEntity);

        workFlowDefinitionRequestEntity.setTaskDefinitionJson(JSONUtils.toJsonString(taskDefinitionEntities));
        ArrayList<TaskRelationEntity> taskRelationEntities = new ArrayList<>();
        taskRelationEntities.add(taskRelationEntity);

        workFlowDefinitionRequestEntity.setTaskRelationJson(JSONUtils.toJsonString(taskRelationEntities));
        ArrayList<TaskLocationsEntity> taskLocationsEntities = new ArrayList<>();
        taskLocationsEntities.add(taskLocationsEntity);

        workFlowDefinitionRequestEntity.setLocations(JSONUtils.toJsonString(taskLocationsEntities));

        RestResponse<Result> workFlowDefinition = workFlowPageAPI.createWorkFlowDefinition(workFlowDefinitionRequestEntity, projectResponseEntity.getCode());
        Response response = workFlowDefinition.getResponse();
        response.prettyPrint();

        workFlowDefinitionResponseEntity = response.jsonPath().getObject(Constants.DATA_KEY, WorkFlowDefinitionResponseEntity.class);

    }

    @Test
    @Order(2)
    public void testReleaseWorkFlow() {
        WorkFlowReleaseRequestEntity workFlowReleaseRequestEntity = new WorkFlowReleaseRequestEntity();
        workFlowReleaseRequestEntity.setName(workFlowDefinitionResponseEntity.getName());

        workFlowReleaseRequestEntity.setReleaseState(ReleaseState.ONLINE);

        workFlowPageAPI.releaseWorkFlowDefinition(workFlowReleaseRequestEntity, projectResponseEntity.getCode(), String.valueOf(workFlowDefinitionResponseEntity.getCode())).isResponseSuccessful();
    }

    @Test
    @Order(3)
    public void testRunWorkFlow() {
        WorkFlowRunRequestEntity workFlowRunRequestEntity = new WorkFlowRunRequestEntity();
        workFlowRunRequestEntity.setProcessDefinitionCode(String.valueOf(workFlowDefinitionResponseEntity.getCode()));
        workFlowRunRequestEntity.setFailureStrategy(FailureStrategy.CONTINUE);
        workFlowRunRequestEntity.setWarningType(WarningType.NONE);
        workFlowRunRequestEntity.setWarningGroupId("");
        workFlowRunRequestEntity.setExecType(ExecCommandType.START_PROCESS);
        workFlowRunRequestEntity.setStartNodeList("");
        workFlowRunRequestEntity.setTaskDependType(TaskDependType.TASK_POST);
        workFlowRunRequestEntity.setComplementDependentMode(ComplementDependentMode.OFF_MODE);
        workFlowRunRequestEntity.setRunMode(RunMode.RUN_MODE_SERIAL);
        workFlowRunRequestEntity.setProcessInstancePriority(Priority.MEDIUM);
        workFlowRunRequestEntity.setWorkerGroup("default");
        workFlowRunRequestEntity.setEnvironmentCode("");
        workFlowRunRequestEntity.setExpectedParallelismNumber("");
        workFlowRunRequestEntity.setDryRun(0);
        workFlowRunRequestEntity.setScheduleTime("{\"complementStartDate\":\"2022-07-10 00:00:00\",\"complementEndDate\":\"2022-07-10 00:00:00\"}");
        RestResponse<Result> resultRestResponse = workFlowPageAPI.runWorkFlowDefinition(workFlowRunRequestEntity, projectResponseEntity.getCode());
        resultRestResponse.isResponseSuccessful();
        resultRestResponse.getResponse().prettyPrint();
    }

}
