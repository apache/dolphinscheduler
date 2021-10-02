/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.graphql.datafetcher;

import graphql.schema.DataFetcher;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.AccessTokenService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.graphql.datafetcher.service.UserArgumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AccessTokenDataFetchers extends BaseDataFetchers {

    private static final Logger logger = LoggerFactory.getLogger(AccessTokenDataFetchers.class);

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private UserArgumentService userArgumentService;


    public DataFetcher<Result> mutationTypeCreateToken() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            Integer userId = dataFetchingEnvironment.getArgument("userId");
            String expireTime = dataFetchingEnvironment.getArgument("expireTime");
            String token = dataFetchingEnvironment.getArgument("token");

            Map<String, Object> result = accessTokenService.createToken(loginUser, userId, expireTime, token);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeGenerateToken() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            Integer userId = dataFetchingEnvironment.getArgument("userId");
            String expireTime = dataFetchingEnvironment.getArgument("expireTime");

            Map<String, Object> result = accessTokenService.generateToken(loginUser, userId, expireTime);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryAccessTokenList() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            Integer pageNo = dataFetchingEnvironment.getArgument("pageNo");
            String searchVal = dataFetchingEnvironment.getArgument("searchVal");
            Integer pageSize = dataFetchingEnvironment.getArgument("pageSize");

            Result result = checkPageParams(pageNo, pageSize);
            if (!result.checkResult()) {
                return result;
            }
            searchVal = ParameterUtils.handleEscapes(searchVal);
            result = accessTokenService.queryAccessTokenList(loginUser, searchVal, pageNo, pageSize);
            return result;
        };
    }


    public DataFetcher<Result> mutationTypeDelAccessTokenById() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int id = dataFetchingEnvironment.getArgument("id");

            Map<String, Object> result = accessTokenService.delAccessTokenById(loginUser, id);
            return returnDataList(result);
        };
    }


    public DataFetcher<Result> mutationTypeUpdateToken() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");

            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);

            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }

            User loginUser = (User) selectUserResult.getData();

            int id = dataFetchingEnvironment.getArgument("id");
            int userId = dataFetchingEnvironment.getArgument("userId");
            String expireTime = dataFetchingEnvironment.getArgument("expireTime");
            String token = dataFetchingEnvironment.getArgument("token");

            Map<String, Object> result = accessTokenService.updateToken(loginUser, id, userId, expireTime, token);
            return returnDataList(result);
        };
    }

}
