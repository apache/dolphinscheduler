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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

/**
 * access token service
 */
public interface AccessTokenService {

    /**
     * query access token list
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return token list for page number and page size
     */
    Result queryAccessTokenList(User loginUser, String searchVal, Integer pageNo, Integer pageSize);

    /**
     * query access token for specified user
     *
     * @param loginUser login user
     * @param userId user id
     * @return token list for specified user
     */
    Map<String, Object> queryAccessTokenByUser(User loginUser, Integer userId);

    /**
     * create token
     *
     * @param userId token for user
     * @param expireTime token expire time
     * @param token token string (if it is absent, it will be automatically generated)
     * @return create result code
     */
    Result createToken(User loginUser, int userId, String expireTime, String token);

    /**
     * generate token
     *
     * @param userId token for user
     * @param expireTime token expire time
     * @return token string
     */
    Map<String, Object> generateToken(User loginUser, int userId, String expireTime);

    /**
     * delete access token
     *
     * @param loginUser login user
     * @param id token id
     * @return delete result code
     */
    Map<String, Object> delAccessTokenById(User loginUser, int id);

    /**
     * update token by id
     *
     * @param id token id
     * @param userId token for user
     * @param expireTime token expire time
     * @param token token string (if it is absent, it will be automatically generated)
     * @return updated access token entity
     */
    Map<String, Object> updateToken(User loginUser, int id, int userId, String expireTime, String token);
}
