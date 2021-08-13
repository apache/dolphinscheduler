package org.apache.dolphinscheduler.graphql.datafetcher;

import graphql.schema.DataFetcher;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
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

}
