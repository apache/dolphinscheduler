package org.apache.dolphinscheduler.graphql.datafetcher;

import graphql.schema.DataFetcher;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.security.Authenticator;
import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.graphql.datafetcher.service.UserArgumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.apache.dolphinscheduler.api.enums.Status.IP_IS_EMPTY;

@Component
public class LoginDataFetchers extends BaseDataFetchers {

    private static final Logger logger = LoggerFactory.getLogger(LoginDataFetchers.class);

    @Autowired
    private SessionService sessionService;

    @Autowired
    private Authenticator authenticator;

    @Autowired
    private UserArgumentService userArgumentService;

    public DataFetcher<Result> queryTypeLogin() {
        return dataFetchingEnvironment -> {
            String userName = dataFetchingEnvironment.getArgument("userName");
            String userPassword = dataFetchingEnvironment.getArgument("userPassword");
            String ip = dataFetchingEnvironment.getArgument("ip");

            // user name check
            if (StringUtils.isEmpty(userName)) {
                return error(Status.USER_NAME_NULL.getCode(),
                        Status.USER_NAME_NULL.getMsg());
            }

            // user ip check
            if (StringUtils.isEmpty(ip)) {
                return error(IP_IS_EMPTY.getCode(), IP_IS_EMPTY.getMsg());
            }

            // verify username and password
            Result<Map<String, String>> resultMap = authenticator.authenticate(userName, userPassword, ip);
            Result<String> result = new Result<>();
            result.setCode(resultMap.getCode());
            result.setMsg(resultMap.getMsg());
            result.setData(JSONUtils.toJsonString(resultMap.getData()));
            if (result.isFailed()) {
                return result;
            }

            return result;
        };
    }

    public DataFetcher<Result> mutationTypeLogOut() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String ip = dataFetchingEnvironment.getArgument("ip");
            sessionService.signOut(ip, loginUser);
            return success();
        };
    }

}
