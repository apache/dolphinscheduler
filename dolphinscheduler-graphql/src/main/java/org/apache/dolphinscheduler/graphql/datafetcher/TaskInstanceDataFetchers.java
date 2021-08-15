package org.apache.dolphinscheduler.graphql.datafetcher;

import graphql.schema.DataFetcher;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.TaskInstanceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.graphql.datafetcher.service.UserArgumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class TaskInstanceDataFetchers extends BaseDataFetchers {

    @Autowired
    TaskInstanceService taskInstanceService;

    @Autowired
    private UserArgumentService userArgumentService;

    public DataFetcher<Result> queryTypeQueryTaskListPaging() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            int processInstanceId = environment.getArgument("processInstanceId");
            String processInstanceName = environment.getArgument("processInstanceName");
            String searchVal = environment.getArgument("searchVal");
            String taskName = environment.getArgument("taskName");
            String executorName = environment.getArgument("executorName");
            ExecutionStatus stateType = environment.getArgument("stateType") == null
                    ? null
                    : ExecutionStatus.valueOf(environment.getArgument("stateType"));
            String host = environment.getArgument("host");
            String startTime = environment.getArgument("startTime");
            String endTime = environment.getArgument("endTime");
            int pageNo = environment.getArgument("pageNo");
            int pageSize = environment.getArgument("pageSize");

            Result result = checkPageParams(pageNo, pageSize);
            if (!result.checkResult()) {
                return result;
            }
            searchVal = ParameterUtils.handleEscapes(searchVal);
            result = taskInstanceService.queryTaskListPaging(
                    loginUser, projectName, processInstanceId, processInstanceName, taskName, executorName, startTime, endTime, searchVal, stateType, host, pageNo, pageSize);
            return result;
        };
    }

    public DataFetcher<Result> mutationTypeForceTaskSuccess() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            int taskInstanceId = environment.getArgument("taskInstanceId");

            Map<String, Object> result = taskInstanceService.forceTaskSuccess(loginUser, projectName, taskInstanceId);
            return returnDataList(result);
        };
    }

}
