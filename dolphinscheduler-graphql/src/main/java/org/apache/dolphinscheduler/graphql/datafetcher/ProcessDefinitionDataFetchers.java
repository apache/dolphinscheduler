package org.apache.dolphinscheduler.graphql.datafetcher;

import graphql.schema.DataFetcher;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;

import org.apache.commons.lang.StringUtils;

import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.graphql.datafetcher.service.UserArgumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.apache.dolphinscheduler.api.enums.Status.BATCH_DELETE_PROCESS_DEFINE_BY_CODES_ERROR;

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

            long projectCode = Long.parseLong(dataFetchingEnvironment.getArgument("projectCode"));
            String name = dataFetchingEnvironment.getArgument("name");
            String description = dataFetchingEnvironment.getArgument("description");
            String globalParams = dataFetchingEnvironment.getArgument("globalParams");
            String locations = dataFetchingEnvironment.getArgument("locations");
            int timeout = dataFetchingEnvironment.getArgument("connects");
            String tenantCode = dataFetchingEnvironment.getArgument("tenantCode");
            String taskRelationJson = dataFetchingEnvironment.getArgument("taskRelationJson");
            String taskDefinitionJson = dataFetchingEnvironment.getArgument("taskDefinitionJson");


            Map<String, Object> result = processDefinitionService.createProcessDefinition(loginUser, projectCode, name, description, globalParams,
                    locations, timeout, tenantCode, taskRelationJson, taskDefinitionJson);
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

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));
            String codes = environment.getArgument("codes");
            long targetProjectCode = Long.parseLong(environment.getArgument("targetProjectCode"));

            return returnDataList(processDefinitionService.batchCopyProcessDefinition(loginUser, projectCode, codes, targetProjectCode));
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

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));
            String codes = environment.getArgument("codes");
            long targetProjectCode = Long.parseLong(environment.getArgument("targetProjectCode"));

            return returnDataList(processDefinitionService.batchMoveProcessDefinition(loginUser, projectCode, codes, targetProjectCode));
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

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));
            String name = environment.getArgument("name");

            Map<String, Object> result = processDefinitionService.verifyProcessDefinitionName(loginUser, projectCode, name);
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

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));
            String name = environment.getArgument("name");
            long code = Long.parseLong(environment.getArgument("code"));
            String description = environment.getArgument("description");
            String globalParams = environment.getArgument("globalParams");
            String locations = environment.getArgument("locations");
            int timeout = environment.getArgument("timeout");
            String tenantCode = environment.getArgument("tenantCode");
            String taskRelationJson = environment.getArgument("taskRelationJson");
            String taskDefinitionJson = environment.getArgument("taskDefinitionJson");
            ReleaseState releaseState = ReleaseState.valueOf(environment.getArgument("releaseState"));

            Map<String, Object> result = processDefinitionService.updateProcessDefinition(loginUser, projectCode, name, code, description, globalParams,
                    locations, timeout, tenantCode, taskRelationJson, taskDefinitionJson);
            //  If the update fails, the result will be returned directly
            if (result.get(Constants.STATUS) != Status.SUCCESS) {
                return returnDataList(result);
            }

            //  Judge whether to go online after editing,0 means offline, 1 means online
            if (releaseState == ReleaseState.ONLINE) {
                result = processDefinitionService.releaseProcessDefinition(loginUser, projectCode, code, releaseState);
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

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));
            int pageNo = environment.getArgument("pageNo");
            int pageSize = environment.getArgument("pageSize");
            long code = Long.parseLong(environment.getArgument("code"));

            Result result = checkPageParams(pageNo, pageSize);
            if (!result.checkResult()) {
                return result;
            }
            result = processDefinitionService.queryProcessDefinitionVersions(loginUser, projectCode, pageNo, pageSize, code);

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

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));
            long code = Long.parseLong(environment.getArgument("code"));
            int version = environment.getArgument("version");

            Map<String, Object> result = processDefinitionService.switchProcessDefinitionVersion(loginUser, projectCode, code, version);
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

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));
            long code = Long.parseLong(environment.getArgument("code"));
            int version = environment.getArgument("version");

            Map<String, Object> result = processDefinitionService.deleteProcessDefinitionVersion(loginUser, projectCode, code, version);
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

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));
            long code = Long.parseLong(environment.getArgument("code"));
            ReleaseState releaseState = ReleaseState.valueOf(environment.getArgument("releaseState"));

            Map<String, Object> result = processDefinitionService.releaseProcessDefinition(loginUser, projectCode, code, releaseState);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryProcessDefinitionByCode() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));
            long code = Long.parseLong(environment.getArgument("code"));

            Map<String, Object> result = processDefinitionService.queryProcessDefinitionByCode(loginUser, projectCode, code);
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

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));
            String name = environment.getArgument("name");

            Map<String, Object> result = processDefinitionService.queryProcessDefinitionByName(loginUser, projectCode, name);
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

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));

            Map<String, Object> result = processDefinitionService.queryProcessDefinitionList(loginUser, projectCode);
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

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));
            int pageNo = environment.getArgument("pageNo");
            String searchVal = environment.getArgument("searchVal");
            int userId = environment.getArgument("userId");
            int pageSize = environment.getArgument("pageSize");

            Result result = checkPageParams(pageNo, pageSize);
            if (!result.checkResult()) {
                return result;
            }
            searchVal = ParameterUtils.handleEscapes(searchVal);
            return processDefinitionService.queryProcessDefinitionListPaging(loginUser, projectCode, searchVal, userId, pageNo, pageSize);
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

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));
            long code = Long.parseLong(environment.getArgument("code"));

            Map<String, Object> result = processDefinitionService.getTaskNodeListByDefinitionCode(loginUser, projectCode, code);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeGetNodeListMapByDefinitionCodes() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));
            String codes = environment.getArgument("codes");


            Map<String, Object> result = processDefinitionService.getNodeListMapByDefinitionCodes(loginUser, projectCode, codes);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeDeleteProcessDefinitionByCode() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));
            long code = Long.parseLong(environment.getArgument("code"));


            Map<String, Object> result = processDefinitionService.deleteProcessDefinitionByCode(loginUser, projectCode, code);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeBatchDeleteProcessDefinitionByCodes() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));
            String codes = environment.getArgument("codes");

            Map<String, Object> result = new HashMap<>();
            List<String> deleteFailedCodeList = new ArrayList<>();
            if (!StringUtils.isEmpty(codes)) {
                String[] processDefinitionCodeArray = codes.split(",");
                for (String strProcessDefinitionCode : processDefinitionCodeArray) {
                    long code = Long.parseLong(strProcessDefinitionCode);
                    try {
                        Map<String, Object> deleteResult = processDefinitionService.deleteProcessDefinitionByCode(loginUser, projectCode, code);
                        if (!Status.SUCCESS.equals(deleteResult.get(Constants.STATUS))) {
                            deleteFailedCodeList.add(strProcessDefinitionCode);
                            logger.error((String) deleteResult.get(Constants.MSG));
                        }
                    } catch (Exception e) {
                        deleteFailedCodeList.add(strProcessDefinitionCode);
                    }
                }
            }

            if (!deleteFailedCodeList.isEmpty()) {
                putMsg(result, BATCH_DELETE_PROCESS_DEFINE_BY_CODES_ERROR, String.join(",", deleteFailedCodeList));
            } else {
                putMsg(result, Status.SUCCESS);
            }
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryAllProcessDefinitionByProjectCode() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            long projectCode = Long.parseLong(environment.getArgument("projectCode"));

            Map<String, Object> result = processDefinitionService.queryAllProcessDefinitionByProjectCode(loginUser, projectCode);
            return returnDataList(result);
        };
    }

}
