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

            long projectCode = Long.parseLong(dataFetchingEnvironment.getArgument("projectCode"));
            long processDefinitionCode = Long.parseLong(dataFetchingEnvironment.getArgument("processDefinitionId"));
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

            RunMode runMode = dataFetchingEnvironment.getArgument("runMode") == null
                    ? null
                    : RunMode.valueOf(dataFetchingEnvironment.getArgument("runMode"));

            Priority processInstancePriority = dataFetchingEnvironment.getArgument("processInstancePriority") == null
                    ? null
                    : Priority.valueOf(dataFetchingEnvironment.getArgument("processInstancePriority"));

            String workerGroup = dataFetchingEnvironment.getArgument("workerGroup");
            long environmentCode = Long.parseLong(dataFetchingEnvironment.getArgument("environmentCode"));
            Integer timeout = dataFetchingEnvironment.getArgument("timeout");
            String startParams = dataFetchingEnvironment.getArgument("startParams");
            int expectedParallelismNumber = dataFetchingEnvironment.getArgument("expectedParallelismNumber");
            int dryRun = dataFetchingEnvironment.getArgument("dryRun");

            if (timeout == null) {
                timeout = Constants.MAX_TASK_TIMEOUT;
            }
            Map<String, String> startParamMap = null;
            if (startParams != null) {
                startParamMap = JSONUtils.toMap(startParams);
            }
            Map<String, Object> result = executorService.execProcessInstance(loginUser, projectCode, processDefinitionCode, scheduleTime, execType, failureStrategy,
                    startNodeList, taskDependType, warningType, warningGroupId, runMode, processInstancePriority, workerGroup, environmentCode, timeout, startParamMap, expectedParallelismNumber, dryRun);
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

            long projectCode = Long.parseLong(dataFetchingEnvironment.getArgument("projectCode"));
            int processInstanceId = dataFetchingEnvironment.getArgument("processInstanceId");
            ExecuteType executeType = ExecuteType.valueOf(dataFetchingEnvironment.getArgument("executeType"));

            logger.info("execute command, login user: {}, project:{}, process instance id:{}, execute type:{}",
                    loginUser.getUserName(), projectCode, processInstanceId, executeType);

            Map<String, Object> result = executorService.execute(loginUser, projectCode, processInstanceId, executeType);
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

            long processDefinitionCode = Long.parseLong(dataFetchingEnvironment.getArgument("processDefinitionCode"));

            logger.info("login user {}, check process definition {}", loginUser.getUserName(), processDefinitionCode);
            Map<String, Object> result = executorService.startCheckByProcessDefinedCode(processDefinitionCode);
            return returnDataList(result);
        };
    }

}
