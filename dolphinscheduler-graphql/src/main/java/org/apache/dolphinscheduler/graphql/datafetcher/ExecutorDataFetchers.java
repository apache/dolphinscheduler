package org.apache.dolphinscheduler.graphql.datafetcher;

import graphql.schema.DataFetcher;
import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.*;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.graphql.datafetcher.service.UserArgumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ExecutorDataFetchers extends BaseDataFetchers {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorDataFetchers.class);

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private UserArgumentService userArgumentService;

    public DataFetcher<Result> mutationTypeStartProcessInstance() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = dataFetchingEnvironment.getArgument("projectName");
            int processDefinitionId = dataFetchingEnvironment.getArgument("processDefinitionId");
            String scheduleTime = dataFetchingEnvironment.getArgument("scheduleTime");
            FailureStrategy failureStrategy = FailureStrategy.valueOf(dataFetchingEnvironment.getArgument("failureStrategy"));
            String startNodeList = dataFetchingEnvironment.getArgument("startNodeList");

            TaskDependType taskDependType = dataFetchingEnvironment.getArgument("taskDependType") == null
                    ? null
                    : TaskDependType.valueOf(dataFetchingEnvironment.getArgument("taskDependType"));

            CommandType execType = dataFetchingEnvironment.getArgument("execType") == null
                    ? null
                    : CommandType.valueOf(dataFetchingEnvironment.getArgument("execType"));

            WarningType warningType = WarningType.valueOf(dataFetchingEnvironment.getArgument("warningType"));
            int warningGroupId = dataFetchingEnvironment.getArgument("warningGroupId");
            String receivers = dataFetchingEnvironment.getArgument("receivers");
            String receiversCc = dataFetchingEnvironment.getArgument("receiversCc");

            RunMode runMode = dataFetchingEnvironment.getArgument("runMode") == null
                    ? null
                    : RunMode.valueOf(dataFetchingEnvironment.getArgument("runMode"));

            Priority processInstancePriority = dataFetchingEnvironment.getArgument("processInstancePriority") == null
                    ? null
                    : Priority.valueOf(dataFetchingEnvironment.getArgument("processInstancePriority"));

            String workerGroup = dataFetchingEnvironment.getArgument("workerGroup");
            Integer timeout = dataFetchingEnvironment.getArgument("timeout");
            String startParams = dataFetchingEnvironment.getArgument("startParams");

            logger.info("login user {}, start process instance, project name: {}, process definition id: {}, schedule time: {}, "
                            + "failure policy: {}, node name: {}, node dep: {}, notify type: {}, "
                            + "notify group id: {},receivers:{},receiversCc:{}, run mode: {},process instance priority:{}, workerGroup: {}, timeout: {}",
                    loginUser.getUserName(), projectName, processDefinitionId, scheduleTime,
                    failureStrategy, startNodeList, taskDependType, warningType, workerGroup, receivers, receiversCc, runMode, processInstancePriority,
                    workerGroup, timeout);

            if (timeout == null) {
                timeout = Constants.MAX_TASK_TIMEOUT;
            }
            Map<String, String> startParamMap = null;
            if (startParams != null) {
                startParamMap = JSONUtils.toMap(startParams);
            }
            Map<String, Object> result = executorService.execProcessInstance(loginUser, projectName, processDefinitionId, scheduleTime, execType, failureStrategy,
                    startNodeList, taskDependType, warningType,
                    warningGroupId, runMode, processInstancePriority, workerGroup, timeout, startParamMap);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeExecute() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = dataFetchingEnvironment.getArgument("projectName");
            int processInstanceId = dataFetchingEnvironment.getArgument("processInstanceId");
            ExecuteType executeType = ExecuteType.valueOf(dataFetchingEnvironment.getArgument("executeType"));

            logger.info("execute command, login user: {}, project:{}, process instance id:{}, execute type:{}",
                    loginUser.getUserName(), projectName, processInstanceId, executeType);

            Map<String, Object> result = executorService.execute(loginUser, projectName, processInstanceId, executeType);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeStartCheckProcessDefinition() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int processDefinitionId = dataFetchingEnvironment.getArgument("processDefinitionId");

            logger.info("login user {}, check process definition {}", loginUser.getUserName(), processDefinitionId);
            Map<String, Object> result = executorService.startCheckByProcessDefinedId(processDefinitionId);
            return returnDataList(result);
        };
    }

}
