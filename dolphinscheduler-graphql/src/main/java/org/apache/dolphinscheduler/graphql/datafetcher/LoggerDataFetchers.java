package org.apache.dolphinscheduler.graphql.datafetcher;

import graphql.schema.DataFetcher;
import org.apache.dolphinscheduler.api.controller.AlertPluginInstanceController;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.LoggerService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.graphql.datafetcher.service.UserArgumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@Component
public class LoggerDataFetchers extends BaseDataFetchers {

    private static final Logger logger = LoggerFactory.getLogger(LoggerDataFetchers.class);

    @Autowired
    private LoggerService loggerService;

    @Autowired
    private UserArgumentService userArgumentService;

    public DataFetcher<Result> queryTypeQueryLog() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int taskInstanceId = dataFetchingEnvironment.getArgument("taskInstanceId");
            int skipNum = dataFetchingEnvironment.getArgument("skipNum");
            int limit = dataFetchingEnvironment.getArgument("limit");

            return loggerService.queryLog(taskInstanceId, skipNum, limit);
        };
    }

    public DataFetcher<ResponseEntity> queryTypeDownloadTaskLog() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return ResponseEntity.notFound().build();
            }
            User loginUser = (User) selectUserResult.getData();

            int taskInstanceId = dataFetchingEnvironment.getArgument("taskInstanceId");

            byte[] logBytes = loggerService.getLogBytes(taskInstanceId);
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + System.currentTimeMillis() + ".log" + "\"")
                    .body(logBytes);
        };
    }

}
