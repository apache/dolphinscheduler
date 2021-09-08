package org.apache.dolphinscheduler.graphql.datafetcher;

import graphql.schema.DataFetcher;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.TenantService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.graphql.datafetcher.service.UserArgumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class TenantDataFetchers extends BaseDataFetchers {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private UserArgumentService userArgumentService;

    public DataFetcher<Result> mutationTypeCreateTenant() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String tenantCode = environment.getArgument("tenantCode");
            int queueId = environment.getArgument("queueId");
            String description = environment.getArgument("description");

            Map<String, Object> result = tenantService.createTenant(loginUser, tenantCode, queueId, description);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryTenantlistPaging() {
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
            result = tenantService.queryTenantList(loginUser, searchVal, pageNo, pageSize);
            return result;
        };
    }

    public DataFetcher<Result> queryTypeQueryTenantlist() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            Map<String, Object> result = tenantService.queryTenantList(loginUser);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeUpdateTenant() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int id = environment.getArgument("id");
            String tenantCode = environment.getArgument("tenantCode");
            int queueId = environment.getArgument("queueId");
            String description = environment.getArgument("description");

            Map<String, Object> result = tenantService.updateTenant(loginUser, id, tenantCode, queueId, description);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeDeleteTenantById() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int id = environment.getArgument("id");

            Map<String, Object> result = tenantService.deleteTenantById(loginUser, id);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeVerifyTenantCode() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String tenantCode = environment.getArgument("tenantCode");

            return tenantService.verifyTenantCode(tenantCode);
        };
    }

}
