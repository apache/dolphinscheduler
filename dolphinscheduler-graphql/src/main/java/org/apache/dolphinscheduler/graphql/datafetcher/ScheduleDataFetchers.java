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

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));
            long processDefinitionCode = Long.parseLong(environment.getArgument("processDefinitionCode"));
            String schedule = environment.getArgument("schedule");
            WarningType warningType = WarningType.valueOf(environment.getArgument("warningType"));
            int warningGroupId = environment.getArgument("warningGroupId");
            FailureStrategy failureStrategy = FailureStrategy.valueOf(environment.getArgument("failureStrategy"));
            String workerGroup = environment.getArgument("workerGroup");
            long environmentCode = Long.parseLong(environment.getArgument("environmentCode"));
            Priority processInstancePriority = Priority.valueOf(environment.getArgument("processInstancePriority"));

            Map<String, Object> result = schedulerService.insertSchedule(loginUser, projectCode, processDefinitionCode, schedule,
                    warningType, warningGroupId, failureStrategy, processInstancePriority, workerGroup, environmentCode);

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

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));
            int id = environment.getArgument("id");
            String schedule = environment.getArgument("schedule");
            WarningType warningType = WarningType.valueOf(environment.getArgument("warningType"));
            int warningGroupId = environment.getArgument("warningGroupId");
            FailureStrategy failureStrategy = FailureStrategy.valueOf(environment.getArgument("failureStrategy"));
            String workerGroup = environment.getArgument("workerGroup");
            long environmentCode = Long.parseLong(environment.getArgument("environmentCode"));
            Priority processInstancePriority = environment.getArgument("processInstancePriority") == null
                    ? null
                    : Priority.valueOf(environment.getArgument("processInstancePriority"));


            Map<String, Object> result = schedulerService.updateSchedule(loginUser, projectCode, id, schedule,
                    warningType, warningGroupId, failureStrategy, processInstancePriority, workerGroup, environmentCode);
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

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));
            int id = environment.getArgument("id");

            Map<String, Object> result = schedulerService.setScheduleState(loginUser, projectCode, id, ReleaseState.ONLINE);
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

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));
            int id = environment.getArgument("id");

            Map<String, Object> result = schedulerService.setScheduleState(loginUser, projectCode, id, ReleaseState.OFFLINE);
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

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));
            long processDefinitionCode = Long.parseLong(environment.getArgument("processDefinitionCode"));
            String searchVal = environment.getArgument("searchVal");
            int pageNo = environment.getArgument("pageNo");
            int pageSize = environment.getArgument("pageSize");

            Result result = checkPageParams(pageNo, pageSize);
            if (!result.checkResult()) {
                return result;
            }
            searchVal = ParameterUtils.handleEscapes(searchVal);
            result = schedulerService.querySchedule(loginUser, projectCode, processDefinitionCode, searchVal, pageNo, pageSize);
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

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));
            int id = environment.getArgument("id");

            Map<String, Object> result = schedulerService.deleteScheduleById(loginUser, projectCode, id);
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

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));

            Map<String, Object> result = schedulerService.queryScheduleList(loginUser, projectCode);
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

            String schedule = environment.getArgument("schedule");

            Map<String, Object> result = schedulerService.previewSchedule(loginUser, schedule);
            return returnDataList(result);
        };
    }



}
