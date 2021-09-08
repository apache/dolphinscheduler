package org.apache.dolphinscheduler.graphql.datafetcher;

import graphql.schema.DataFetcher;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.SchedulerService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.graphql.datafetcher.service.UserArgumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ScheduleDataFetchers extends BaseDataFetchers {

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private UserArgumentService userArgumentService;

    public DataFetcher<Result> mutationTypeCreateSchedule() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            int processDefinitionId = environment.getArgument("processDefinitionId");
            String schedule = environment.getArgument("schedule");
            WarningType warningType = WarningType.valueOf(environment.getArgument("warningType"));
            int warningGroupId = environment.getArgument("warningGroupId");
            FailureStrategy failureStrategy = FailureStrategy.valueOf(environment.getArgument("failureStrategy"));
            String workerGroup = environment.getArgument("workerGroup");
            Priority processInstancePriority = Priority.valueOf(environment.getArgument("processInstancePriority"));

            Map<String, Object> result = schedulerService.insertSchedule(loginUser, projectName, processDefinitionId, schedule,
                    warningType, warningGroupId, failureStrategy, processInstancePriority, workerGroup);

            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeUpdateSchedule() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            int id = environment.getArgument("id");
            String schedule = environment.getArgument("schedule");
            WarningType warningType = WarningType.valueOf(environment.getArgument("warningType"));
            int warningGroupId = environment.getArgument("warningGroupId");
            FailureStrategy failureStrategy = FailureStrategy.valueOf(environment.getArgument("failureStrategy"));
            String workerGroup = environment.getArgument("workerGroup");
            Priority processInstancePriority = environment.getArgument("processInstancePriority") == null
                    ? null
                    : Priority.valueOf(environment.getArgument("processInstancePriority"));


            Map<String, Object> result = schedulerService.updateSchedule(loginUser, projectName, id, schedule,
                    warningType, warningGroupId, failureStrategy, null, processInstancePriority, workerGroup);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeOnline() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            int id = environment.getArgument("id");

            Map<String, Object> result = schedulerService.setScheduleState(loginUser, projectName, id, ReleaseState.ONLINE);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeOffline() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            int id = environment.getArgument("id");

            Map<String, Object> result = schedulerService.setScheduleState(loginUser, projectName, id, ReleaseState.OFFLINE);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryScheduleListPaging() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            int processDefinitionId = environment.getArgument("processDefinitionId");
            String searchVal = environment.getArgument("searchVal");
            int pageNo = environment.getArgument("pageNo");
            int pageSize = environment.getArgument("pageSize");

            Result result = checkPageParams(pageNo, pageSize);
            if (!result.checkResult()) {
                return result;
            }
            searchVal = ParameterUtils.handleEscapes(searchVal);
            result = schedulerService.querySchedule(loginUser, projectName, processDefinitionId, searchVal, pageNo, pageSize);
            return result;
        };
    }

    public DataFetcher<Result> mutationTypeDeleteScheduleById() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            int scheduleId = environment.getArgument("scheduleId");

            Map<String, Object> result = schedulerService.deleteScheduleById(loginUser, projectName, scheduleId);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryScheduleList() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");

            Map<String, Object> result = schedulerService.queryScheduleList(loginUser, projectName);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypePreviewSchedule() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            String schedule = environment.getArgument("schedule");

            Map<String, Object> result = schedulerService.previewSchedule(loginUser, projectName, schedule);
            return returnDataList(result);
        };
    }



}
