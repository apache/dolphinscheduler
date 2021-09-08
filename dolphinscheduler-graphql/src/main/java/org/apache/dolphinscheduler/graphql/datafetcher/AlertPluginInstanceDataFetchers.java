package org.apache.dolphinscheduler.graphql.datafetcher;

import graphql.schema.DataFetcher;
import org.apache.dolphinscheduler.api.controller.AlertPluginInstanceController;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.AlertPluginInstanceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.graphql.datafetcher.service.UserArgumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AlertPluginInstanceDataFetchers extends BaseDataFetchers {

    private static final Logger logger = LoggerFactory.getLogger(AlertPluginInstanceController.class);

    @Autowired
    private AlertPluginInstanceService alertPluginInstanceService;

    @Autowired
    private UserArgumentService userArgumentService;

    public DataFetcher<Result> mutationTypeCreateAlertPluginInstance() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int pluginDefineId = dataFetchingEnvironment.getArgument("pluginDefineId");
            String instanceName = dataFetchingEnvironment.getArgument("instanceName");
            String pluginInstanceParams = dataFetchingEnvironment.getArgument("pluginInstanceParams");

            Map<String, Object> result = alertPluginInstanceService.create(loginUser, pluginDefineId, instanceName, pluginInstanceParams);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeUpdateAlertPluginInstance() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int pluginDefineId = dataFetchingEnvironment.getArgument("pluginDefineId");
            String instanceName = dataFetchingEnvironment.getArgument("instanceName");
            String pluginInstanceParams = dataFetchingEnvironment.getArgument("pluginInstanceParams");

            Map<String, Object> result = alertPluginInstanceService.update(loginUser, pluginDefineId, instanceName, pluginInstanceParams);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeDeleteAlertPluginInstance() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int id = dataFetchingEnvironment.getArgument("id");

            Map<String, Object> result = alertPluginInstanceService.delete(loginUser, id);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeGetAlertPluginInstance() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int id = dataFetchingEnvironment.getArgument("id");

            Map<String, Object> result = alertPluginInstanceService.get(loginUser, id);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeGetAllAlertPluginInstance() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            Map<String, Object> result = alertPluginInstanceService.queryAll();
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeVerifyAlertInstanceName() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String alertInstanceName = dataFetchingEnvironment.getArgument("alertInstanceName");

            boolean exist = alertPluginInstanceService.checkExistPluginInstanceName(alertInstanceName);
            Result result = new Result();
            if (exist) {
                logger.error("alert plugin instance {} has exist, can't create again.", alertInstanceName);
                result.setCode(Status.PLUGIN_INSTANCE_ALREADY_EXIT.getCode());
                result.setMsg(Status.PLUGIN_INSTANCE_ALREADY_EXIT.getMsg());
            } else {
                result.setCode(Status.SUCCESS.getCode());
                result.setMsg(Status.SUCCESS.getMsg());
            }
            return result;
        };
    }

    public DataFetcher<Result> queryTypeQueryAlertPluginInstanceListPaging() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int pageNo = dataFetchingEnvironment.getArgument("pageNo");
            int pageSize = dataFetchingEnvironment.getArgument("pageSize");

            Result result = checkPageParams(pageNo, pageSize);
            if (!result.checkResult()) {
                return result;
            }
            return alertPluginInstanceService.queryPluginPage(pageNo, pageSize);
        };
    }

}
