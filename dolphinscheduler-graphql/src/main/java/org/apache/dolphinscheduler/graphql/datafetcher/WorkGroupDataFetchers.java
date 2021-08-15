package org.apache.dolphinscheduler.graphql.datafetcher;

import graphql.schema.DataFetcher;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.WorkerGroupService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.graphql.datafetcher.service.UserArgumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class WorkGroupDataFetchers extends BaseDataFetchers {

    @Autowired
    private WorkerGroupService workerGroupService;

    @Autowired
    private UserArgumentService userArgumentService;

    public DataFetcher<Result> mutationTypeSaveWorkerGroup() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int id = environment.getArgument("id");
            String name = environment.getArgument("name");
            String addrList = environment.getArgument("addrList");

            Map<String, Object> result = workerGroupService.saveWorkerGroup(loginUser, id, name, addrList);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryAllWorkerGroupsPaging() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String searchVal = environment.getArgument("searchVal");
            int pageNo = environment.getArgument("pageNo");
            int pageSize = environment.getArgument("pageSize");

            Result result = checkPageParams(pageNo, pageSize);
            if (!result.checkResult()) {
                return result;

            }
            searchVal = ParameterUtils.handleEscapes(searchVal);
            result = workerGroupService.queryAllGroupPaging(loginUser, pageNo, pageSize, searchVal);
            return result;
        };
    }

    public DataFetcher<Result> queryTypeQueryAllWorkerGroups() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            Map<String, Object> result = workerGroupService.queryAllGroup();
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeDeleteWorkGroupById() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int id = environment.getArgument("id");

            Map<String, Object> result = workerGroupService.deleteWorkerGroupById(loginUser, id);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryWorkerAddressList() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            Map<String, Object> result = workerGroupService.getWorkerAddressList();
            return returnDataList(result);
        };
    }

}
