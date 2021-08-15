package org.apache.dolphinscheduler.graphql.datafetcher;

import graphql.schema.DataFetcher;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.graphql.datafetcher.service.UserArgumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserDataFetchers extends BaseDataFetchers {

    private static final Logger logger = LoggerFactory.getLogger(UserDataFetchers.class);

    @Autowired
    private UsersService usersService;

    @Autowired
    private UserArgumentService userArgumentService;

    public DataFetcher<Result> mutationTypeQueryCreateUser() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String userName = environment.getArgument("userName");
            String userPassword = environment.getArgument("userPassword");
            int tenantId = environment.getArgument("tenantId");
            String queue = environment.getArgument("queue");
            String email = environment.getArgument("email");
            String phone = environment.getArgument("phone");
            int state = environment.getArgument("state");

            Map<String, Object> result = usersService.createUser(loginUser, userName, userPassword, email, tenantId, phone, queue, state);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryUserList() {
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
            result = usersService.queryUserList(loginUser, searchVal, pageNo, pageSize);
            return result;
        };
    }

    public DataFetcher<Result> mutationTypeUpdateUser() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int id = environment.getArgument("id");
            String userName = environment.getArgument("userName");
            String userPassword = environment.getArgument("userPassword");
            int tenantId = environment.getArgument("tenantId");
            String queue = environment.getArgument("queue");
            String email = environment.getArgument("email");
            String phone = environment.getArgument("phone");
            int state = environment.getArgument("state");

            Map<String, Object> result = usersService.updateUser(loginUser, id, userName, userPassword, email, tenantId, phone, queue, state);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeDelUserById() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int id = environment.getArgument("id");

            Map<String, Object> result = usersService.deleteUserById(loginUser, id);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeGrantProject() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int userId = environment.getArgument("userId");
            String projectIds = environment.getArgument("projectIds");

            Map<String, Object> result = usersService.grantProject(loginUser, userId, projectIds);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeGrantResource() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int userId = environment.getArgument("userId");
            String resourceIds = environment.getArgument("resourceIds");

            Map<String, Object> result = usersService.grantResources(loginUser, userId, resourceIds);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeGrantUDFFunc() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int userId = environment.getArgument("userId");
            String udfIds = environment.getArgument("udfIds");

            Map<String, Object> result = usersService.grantUDFFunction(loginUser, userId, udfIds);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeGrantDataSource() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int userId = environment.getArgument("userId");
            String datasourceIds = environment.getArgument("datasourceIds");

            Map<String, Object> result = usersService.grantDataSource(loginUser, userId, datasourceIds);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeGetUserInfo() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            Map<String, Object> result = usersService.getUserInfo(loginUser);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeListUser() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            Map<String, Object> result = usersService.queryAllGeneralUsers(loginUser);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeListAll() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            Map<String, Object> result = usersService.queryUserList(loginUser);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeVerifyUserName() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String userName = environment.getArgument("userName");

            return usersService.verifyUserName(userName);
        };
    }

    public DataFetcher<Result> queryTypeUnauthorizedUser() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int alertgroupId = environment.getArgument("alertgroupId");

            Map<String, Object> result = usersService.unauthorizedUser(loginUser, alertgroupId);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeAuthorizedUser() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int alertgroupId = environment.getArgument("alertgroupId");

            try {
                Map<String, Object> result = usersService.authorizedUser(loginUser, alertgroupId);
                return returnDataList(result);
            } catch (Exception e) {
                logger.error(Status.AUTHORIZED_USER_ERROR.getMsg(), e);
                return error(Status.AUTHORIZED_USER_ERROR.getCode(), Status.AUTHORIZED_USER_ERROR.getMsg());
            }
        };
    }

    public DataFetcher<Result> mutationTypeRegisterUser() {
        return environment -> {
            String userName = environment.getArgument("userName");
            String userPassword = environment.getArgument("userPassword");
            String repeatPassword = environment.getArgument("repeatPassword");
            String email = environment.getArgument("email");


            userName = ParameterUtils.handleEscapes(userName);
            userPassword = ParameterUtils.handleEscapes(userPassword);
            repeatPassword = ParameterUtils.handleEscapes(repeatPassword);
            email = ParameterUtils.handleEscapes(email);
            Map<String, Object> result = usersService.registerUser(userName, userPassword, repeatPassword, email);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeActivateUser() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String userName = environment.getArgument("userName");

            userName = ParameterUtils.handleEscapes(userName);
            Map<String, Object> result = usersService.activateUser(loginUser, userName);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeBatchActivateUser() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            List<String> userNames = environment.getArgument("userNames");

            List<String> formatUserNames = userNames.stream().map(ParameterUtils::handleEscapes).collect(Collectors.toList());
            Map<String, Object> result = usersService.batchActivateUser(loginUser, formatUserNames);
            return returnDataList(result);
        };
    }

}
