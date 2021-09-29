package org.apache.dolphinscheduler.graphql.datafetcher;

import graphql.schema.DataFetcher;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.DataAnalysisService;
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
public class DataAnalysisDataFetchers extends BaseDataFetchers {

    private static final Logger logger = LoggerFactory.getLogger(DataAnalysisDataFetchers.class);

    @Autowired
    private DataAnalysisService dataAnalysisService;

    @Autowired
    private UserArgumentService userArgumentService;

    public DataFetcher<Result> queryTypeCountTaskState() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String startDate = dataFetchingEnvironment.getArgument("startDate");
            String endDate = dataFetchingEnvironment.getArgument("endDate");
            long projectCode = Long.parseLong(dataFetchingEnvironment.getArgument("projectCode"));

            Map<String, Object> result = dataAnalysisService.countTaskStateByProject(loginUser, projectCode, startDate, endDate);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeCountProcessInstanceState() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String startDate = dataFetchingEnvironment.getArgument("startDate");
            String endDate = dataFetchingEnvironment.getArgument("endDate");
            long projectCode = Long.parseLong(dataFetchingEnvironment.getArgument("projectCode"));

            logger.info("count process instance state, user:{}, start date: {}, end date:{}, project id:{}",
                    loginUser.getUserName(), startDate, endDate, projectCode);
            Map<String, Object> result = dataAnalysisService.countProcessInstanceStateByProject(loginUser, projectCode, startDate, endDate);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeCountDefinitionByUser() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            long projectCode = Long.parseLong(dataFetchingEnvironment.getArgument("projectCode"));

            logger.info("count process definition , user:{}, project id:{}",
                    loginUser.getUserName(), projectCode);
            Map<String, Object> result = dataAnalysisService.countDefinitionByUser(loginUser, projectCode);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeCountCommandState() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            logger.info("count command state, user:{}",
                    loginUser.getUserName());
            Map<String, Object> result = dataAnalysisService.countCommandState(loginUser);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeCountQueueState() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            logger.info("count command state, user:{}",
                    loginUser.getUserName());
            Map<String, Object> result = dataAnalysisService.countQueueState(loginUser);
            return returnDataList(result);
        };
    }

}
