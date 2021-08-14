package org.apache.dolphinscheduler.graphql.datafetcher;

import graphql.schema.DataFetcher;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.QueueService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.graphql.datafetcher.service.UserArgumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class QueueDataFetchers extends BaseDataFetchers {

    @Autowired
    private QueueService queueService;

    @Autowired
    private UserArgumentService userArgumentService;

    public DataFetcher<Result> mutationTypeCreateProject() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String queue = environment.getArgument("queue");
            String queueName = environment.getArgument("queueName");

            Map<String, Object> result = queueService.createQueue(loginUser, queue, queueName);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryQueueList() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            Map<String, Object> result = queueService.queryList(loginUser);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryQueueListPaging() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int pageNo = environment.getArgument("pageNo");
            String searchVal = environment.getArgument("searchVal");
            int pageSize = environment.getArgument("pageSize");

            Result result = checkPageParams(pageNo, pageSize);
            if (!result.checkResult()) {
                return result;
            }

            searchVal = ParameterUtils.handleEscapes(searchVal);
            result = queueService.queryList(loginUser, searchVal, pageNo, pageSize);
            return result;
        };
    }

    public DataFetcher<Result> mutationTypeUpdateQueue() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int id = environment.getArgument("id");
            String queue = environment.getArgument("queue");
            String queueName = environment.getArgument("queueName");

            Map<String, Object> result = queueService.updateQueue(loginUser, id, queue, queueName);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeVerifyQueue() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String queue = environment.getArgument("queue");
            String queueName = environment.getArgument("queueName");

            return queueService.verifyQueue(queue, queueName);
        };
    }
}
