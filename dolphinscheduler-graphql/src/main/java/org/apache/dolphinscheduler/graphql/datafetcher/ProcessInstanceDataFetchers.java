package org.apache.dolphinscheduler.graphql.datafetcher;

import graphql.schema.DataFetcher;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ProcessInstanceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.graphql.datafetcher.service.UserArgumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProcessInstanceDataFetchers extends BaseDataFetchers {

    private static final Logger logger = LoggerFactory.getLogger(ProcessInstanceDataFetchers.class);

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    private UserArgumentService userArgumentService;

    public DataFetcher<Result> queryTypeQueryProcessInstanceList() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            int processDefinitionId = environment.getArgument("processDefinitionId");
            String searchVal = environment.getArgument("searchVal");
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
            result = processInstanceService.queryProcessInstanceList(
                    loginUser, projectName, processDefinitionId, startTime, endTime, searchVal, executorName, stateType, host, pageNo, pageSize);
            return result;
        };
    }

    public DataFetcher<Result> queryTypeQueryTaskListByProcessId() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            int processInstanceId = environment.getArgument("processInstanceId");

            Map<String, Object> result = processInstanceService.queryTaskListByProcessId(loginUser, projectName, processInstanceId);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeUpdateProcessInstance() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            String processInstanceJson = environment.getArgument("processInstanceJson");
            int processInstanceId = environment.getArgument("processInstanceId");
            String scheduleTime = environment.getArgument("scheduleTime");
            boolean syncDefine = environment.getArgument("syncDefine");
            String locations = environment.getArgument("locations");
            String connects = environment.getArgument("connects");

            Flag flag = environment.getArgument("flag") == null
                    ? null
                    : Flag.valueOf(environment.getArgument("flag"));

            Map<String, Object> result = processInstanceService.updateProcessInstance(loginUser, projectName,
                    processInstanceId, processInstanceJson, scheduleTime, syncDefine, flag, locations, connects);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryProcessInstanceById() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            int processInstanceId = environment.getArgument("processInstanceId");

            Map<String, Object> result = processInstanceService.queryProcessInstanceById(loginUser, projectName, processInstanceId);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryTopNLongestRunningProcessInstance() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            int size = environment.getArgument("size");
            String startTime = environment.getArgument("startTime");
            String endTime = environment.getArgument("endTime");

            projectName = ParameterUtils.handleEscapes(projectName);
            Map<String,Object> result = processInstanceService.queryTopNLongestRunningProcessInstance(loginUser, projectName, size, startTime, endTime);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeDeleteProcessInstanceById() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            int processInstanceId = environment.getArgument("processInstanceId");

            Map<String, Object> result = processInstanceService.deleteProcessInstanceById(loginUser, projectName, processInstanceId);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQuerySubProcessInstanceByTaskId() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            int taskId = environment.getArgument("taskId");

            Map<String, Object> result = processInstanceService.querySubProcessInstanceByTaskId(loginUser, projectName, taskId);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryParentInstanceBySubId() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            int subId = environment.getArgument("subId");

            Map<String, Object> result = processInstanceService.queryParentInstanceBySubId(loginUser, projectName, subId);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeViewVariables() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int processInstanceId = environment.getArgument("processInstanceId");

            Map<String, Object> result = processInstanceService.viewVariables(processInstanceId);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeProcessInstanceViewTree() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int processInstanceId = environment.getArgument("processInstanceId");

            Map<String, Object> result = processInstanceService.viewGantt(processInstanceId);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeBatchDeleteProcessInstanceByIds() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            String processInstanceIds = environment.getArgument("processInstanceIds");

            // task queue
            Map<String, Object> result = new HashMap<>();
            List<String> deleteFailedIdList = new ArrayList<>();
            if (StringUtils.isNotEmpty(processInstanceIds)) {
                String[] processInstanceIdArray = processInstanceIds.split(",");

                for (String strProcessInstanceId : processInstanceIdArray) {
                    int processInstanceId = Integer.parseInt(strProcessInstanceId);
                    try {
                        Map<String, Object> deleteResult = processInstanceService.deleteProcessInstanceById(loginUser, projectName, processInstanceId);
                        if (!Status.SUCCESS.equals(deleteResult.get(Constants.STATUS))) {
                            deleteFailedIdList.add(strProcessInstanceId);
                            logger.error((String) deleteResult.get(Constants.MSG));
                        }
                    } catch (Exception e) {
                        deleteFailedIdList.add(strProcessInstanceId);
                    }
                }
            }
            if (!deleteFailedIdList.isEmpty()) {
                putMsg(result, Status.BATCH_DELETE_PROCESS_INSTANCE_BY_IDS_ERROR, String.join(",", deleteFailedIdList));
            } else {
                putMsg(result, Status.SUCCESS);
            }

            return returnDataList(result);
        };
    }

}
