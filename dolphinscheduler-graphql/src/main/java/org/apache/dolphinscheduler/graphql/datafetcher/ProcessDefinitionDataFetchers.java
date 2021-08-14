package org.apache.dolphinscheduler.graphql.datafetcher;

import graphql.schema.DataFetcher;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
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
public class ProcessDefinitionDataFetchers extends BaseDataFetchers {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionDataFetchers.class);

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Autowired
    private UserArgumentService userArgumentService;

    public DataFetcher<Result> mutationTypeCreateProcessDefinition() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = dataFetchingEnvironment.getArgument("projectName");
            String name = dataFetchingEnvironment.getArgument("name");
            String json = dataFetchingEnvironment.getArgument("json");
            String locations = dataFetchingEnvironment.getArgument("locations");
            String connects = dataFetchingEnvironment.getArgument("connects");
            String description = dataFetchingEnvironment.getArgument("description");

            Map<String, Object> result = processDefinitionService.createProcessDefinition(loginUser, projectName, name, json,
                    description, locations, connects);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeCopyProcessDefinition() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            String processDefinitionIds = environment.getArgument("processDefinitionIds");
            int targetProjectId = environment.getArgument("targetProjectId");

            return returnDataList(
                    processDefinitionService.batchCopyProcessDefinition(loginUser, projectName, processDefinitionIds, targetProjectId));
        };
    }

    public DataFetcher<Result> queryTypeMoveProcessDefinition() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            String processDefinitionIds = environment.getArgument("processDefinitionIds");
            int targetProjectId = environment.getArgument("targetProjectId");

            return returnDataList(
                    processDefinitionService.batchMoveProcessDefinition(loginUser, projectName, processDefinitionIds, targetProjectId));
        };
    }

    public DataFetcher<Result> queryTypeVerifyProcessDefinitionName() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            String name = environment.getArgument("name");

            Map<String, Object> result = processDefinitionService.verifyProcessDefinitionName(loginUser, projectName, name);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeUpdateProcessDefinition() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            String name = environment.getArgument("name");
            int id = environment.getArgument("id");
            String processDefinitionJson = environment.getArgument("processDefinitionJson");
            String locations = environment.getArgument("locations");
            String connects = environment.getArgument("connects");
            String description = environment.getArgument("description");
            ReleaseState releaseState = ReleaseState.valueOf(environment.getArgument("releaseState"));

            Map<String, Object> result = processDefinitionService.updateProcessDefinition(loginUser, projectName, id, name,
                    processDefinitionJson, description, locations, connects);
            //  If the update fails, the result will be returned directly
            if (result.get(Constants.STATUS) != Status.SUCCESS) {
                return returnDataList(result);
            }

            //  Judge whether to go online after editing,0 means offline, 1 means online
            if (releaseState == ReleaseState.ONLINE) {
                result = processDefinitionService.releaseProcessDefinition(loginUser, projectName, id, releaseState);
            }
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryProcessDefinitionVersions() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            int pageNo = environment.getArgument("pageNo");
            int pageSize = environment.getArgument("pageSize");
            long processDefinitionCode = Long.parseLong(environment.getArgument("processDefinitionCode"));

            Result result = checkPageParams(pageNo, pageSize);
            if (!result.checkResult()) {
                return result;
            }
            result = processDefinitionService.queryProcessDefinitionVersions(loginUser
                    , projectName, pageNo, pageSize, processDefinitionCode);

            return result;
        };
    }

    public DataFetcher<Result> mutationTypeSwitchProcessDefinitionVersion() {
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
            long version = Long.parseLong(environment.getArgument("version"));

            Map<String, Object> result = processDefinitionService.switchProcessDefinitionVersion(loginUser, projectName
                    , processDefinitionId, version);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeDeleteProcessDefinitionVersion() {
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
            long version = Long.parseLong(environment.getArgument("version"));

            Map<String, Object> result = processDefinitionService.deleteByProcessDefinitionIdAndVersion(loginUser, projectName, processDefinitionId, version);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeReleaseProcessDefinition() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            int processId = environment.getArgument("processId");
            ReleaseState releaseState = ReleaseState.valueOf(environment.getArgument("releaseState"));

            Map<String, Object> result = processDefinitionService.releaseProcessDefinition(loginUser, projectName, processId, releaseState);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryProcessDefinitionById() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            int processId = environment.getArgument("processId");

            Map<String, Object> result = processDefinitionService.queryProcessDefinitionById(loginUser, projectName, processId);
            return returnDataList(result);
        };
    }


    public DataFetcher<Result> queryTypeQueryProcessDefinitionByName() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            String processDefinitionName = environment.getArgument("processDefinitionName");

            Map<String, Object> result = processDefinitionService.queryProcessDefinitionByName(loginUser, projectName, processDefinitionName);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryProcessDefinitionList() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");

            Map<String, Object> result = processDefinitionService.queryProcessDefinitionList(loginUser, projectName);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryProcessDefinitionListPaging() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            int pageNo = environment.getArgument("pageNo");
            String searchVal = environment.getArgument("searchVal");
            int userId = environment.getArgument("userId");
            int pageSize = environment.getArgument("pageSize");

            Result result = checkPageParams(pageNo, pageSize);
            if (!result.checkResult()) {
                return result;
            }
            searchVal = ParameterUtils.handleEscapes(searchVal);
            return processDefinitionService.queryProcessDefinitionListPaging(loginUser, projectName, searchVal, pageNo, pageSize, userId);
        };
    }

    public DataFetcher<Result> queryTypeViewTree() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int id = environment.getArgument("id");
            int limit = environment.getArgument("limit");

            Map<String, Object> result = processDefinitionService.viewTree(id, limit);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeGetNodeListByDefinitionCode() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            long processDefinitionCode = Long.parseLong(environment.getArgument("processDefinitionCode"));

            logger.info("query task node name list by definitionCode, login user:{}, project name:{}, code : {}",
                    loginUser.getUserName(), projectName, processDefinitionCode);
            Map<String, Object> result = processDefinitionService.getTaskNodeListByDefinitionCode(processDefinitionCode);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeGetNodeListByDefinitionCodeList() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String processDefinitionCodeList = environment.getArgument("processDefinitionCodeList");

            Map<String, Object> result = processDefinitionService.getTaskNodeListByDefinitionCodeList(processDefinitionCodeList);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeDeleteProcessDefinitionById() {
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


            Map<String, Object> result = processDefinitionService.deleteProcessDefinitionById(loginUser, projectName, processDefinitionId);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeBatchDeleteProcessDefinitionByIds() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            String processDefinitionIds = environment.getArgument("processDefinitionIds");

            Map<String, Object> result = new HashMap<>();
            List<String> deleteFailedIdList = new ArrayList<>();
            if (StringUtils.isNotEmpty(processDefinitionIds)) {
                String[] processDefinitionIdArray = processDefinitionIds.split(",");
                for (String strProcessDefinitionId : processDefinitionIdArray) {
                    int processDefinitionId = Integer.parseInt(strProcessDefinitionId);
                    try {
                        Map<String, Object> deleteResult = processDefinitionService.deleteProcessDefinitionById(loginUser, projectName, processDefinitionId);
                        if (!Status.SUCCESS.equals(deleteResult.get(Constants.STATUS))) {
                            deleteFailedIdList.add(strProcessDefinitionId);
                            logger.error((String) deleteResult.get(Constants.MSG));
                        }
                    } catch (Exception e) {
                        deleteFailedIdList.add(strProcessDefinitionId);
                    }
                }
            }

            if (!deleteFailedIdList.isEmpty()) {
                putMsg(result, Status.BATCH_DELETE_PROCESS_DEFINE_BY_IDS_ERROR, String.join(",", deleteFailedIdList));
            } else {
                putMsg(result, Status.SUCCESS);
            }

            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryProcessDefinitionAllByProjectId() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int projectId = environment.getArgument("projectId");

            Map<String, Object> result = processDefinitionService.queryProcessDefinitionAllByProjectId(projectId);
            return returnDataList(result);
        };
    }

}
