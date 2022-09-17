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

package org.apache.dolphinscheduler.api.service.impl;

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ACCESS_TOKEN_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ACCESS_TOKEN_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.ACCESS_TOKEN_UPDATE;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.AccessTokenService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.EncryptionUtils;
import org.apache.dolphinscheduler.dao.entity.AccessToken;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AccessTokenMapper;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * access token service impl
 */
@Service
public class AccessTokenServiceImpl extends BaseServiceImpl implements AccessTokenService {

    private static final Logger logger = LoggerFactory.getLogger(AccessTokenServiceImpl.class);

    @Autowired
    private AccessTokenMapper accessTokenMapper;

    /**
     * query access token list
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return token list for page number and page size
     */
    @Override
    public Result queryAccessTokenList(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        Result result = new Result();
        PageInfo<AccessToken> pageInfo = new PageInfo<>(pageNo, pageSize);
        Page<AccessToken> page = new Page<>(pageNo, pageSize);
        int userId = loginUser.getId();
        if (loginUser.getUserType() == UserType.ADMIN_USER) {
            userId = 0;
        }
        IPage<AccessToken> accessTokenList = accessTokenMapper.selectAccessTokenPage(page, searchVal, userId);
        pageInfo.setTotal((int) accessTokenList.getTotal());
        pageInfo.setTotalList(accessTokenList.getRecords());
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query access token for specified user
     *
     * @param loginUser login user
     * @param userId user id
     * @return token list for specified user
     */
    @Override
    public Map<String, Object> queryAccessTokenByUser(User loginUser, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, false);
        // no permission
        if (loginUser.getUserType().equals(UserType.GENERAL_USER) && loginUser.getId() != userId) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        userId = loginUser.getUserType().equals(UserType.ADMIN_USER) ? 0 : userId;
        // query access token for specified user
        List<AccessToken> accessTokenList = this.accessTokenMapper.queryAccessTokenByUser(userId);
        result.put(Constants.DATA_LIST, accessTokenList);
        this.putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * create token
     *
     * @param loginUser loginUser
     * @param userId token for user
     * @param expireTime token expire time
     * @param token token string (if it is absent, it will be automatically generated)
     * @return create result code
     */
    @SuppressWarnings("checkstyle:WhitespaceAround")
    @Override
    public Result createToken(User loginUser, int userId, String expireTime, String token) {
        Result result = new Result();

        // 1. check permission
        if (!(canOperatorPermissions(loginUser,null, AuthorizationType.ACCESS_TOKEN,ACCESS_TOKEN_CREATE) || loginUser.getId() == userId)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        // 2. check if user is existed
        if (userId <= 0) {
            String errorMsg = "User id should not less than or equals to 0.";
            logger.error(errorMsg);
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, errorMsg);
            return result;
        }

        // 3. generate access token if absent
        if (StringUtils.isBlank(token)) {
            token = EncryptionUtils.getMd5(userId + expireTime + System.currentTimeMillis());
        }

        // 4. persist to the database
        AccessToken accessToken = new AccessToken();
        accessToken.setUserId(userId);
        accessToken.setExpireTime(DateUtils.stringToDate(expireTime));
        accessToken.setToken(token);
        accessToken.setCreateTime(new Date());
        accessToken.setUpdateTime(new Date());

        int insert = accessTokenMapper.insert(accessToken);

        if (insert > 0) {
            result.setData(accessToken);
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.CREATE_ACCESS_TOKEN_ERROR);
        }

        return result;
    }

    /**
     * generate token
     *
     * @param loginUser
     * @param userId token for user
     * @param expireTime token expire time
     * @return token string
     */
    @Override
    public Map<String, Object> generateToken(User loginUser, int userId, String expireTime) {
        Map<String, Object> result = new HashMap<>();
        String token = EncryptionUtils.getMd5(userId + expireTime + System.currentTimeMillis());
        result.put(Constants.DATA_LIST, token);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * delete access token
     *
     * @param loginUser login user
     * @param id token id
     * @return delete result code
     */
    @Override
    public Map<String, Object> delAccessTokenById(User loginUser, int id) {
        Map<String, Object> result = new HashMap<>();
        if (!canOperatorPermissions(loginUser, null, AuthorizationType.ACCESS_TOKEN,ACCESS_TOKEN_DELETE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        AccessToken accessToken = accessTokenMapper.selectById(id);
        if (accessToken == null) {
            logger.error("Access token does not exist, accessTokenId:{}.", id);
            putMsg(result, Status.ACCESS_TOKEN_NOT_EXIST);
            return result;
        }

        // admin can operate all, non-admin can operate their own
        if (accessToken.getUserId() != loginUser.getId() && !loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        accessTokenMapper.deleteById(id);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * update token by id
     *
     * @param id token id
     * @param userId token for user
     * @param expireTime token expire time
     * @param token token string (if it is absent, it will be automatically generated)
     * @return updated access token entity
     */
    @Override
    public Map<String, Object> updateToken(User loginUser, int id, int userId, String expireTime, String token) {
        Map<String, Object> result = new HashMap<>();

        // 1. check permission
        if (!canOperatorPermissions(loginUser, null,AuthorizationType.ACCESS_TOKEN,ACCESS_TOKEN_UPDATE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        // 2. check if token is existed
        AccessToken accessToken = accessTokenMapper.selectById(id);
        if (accessToken == null) {
            logger.error("Access token does not exist, accessTokenId:{}.", id);
            putMsg(result, Status.ACCESS_TOKEN_NOT_EXIST);
            return result;
        }
        // admin can operate all, non-admin can operate their own
        if (accessToken.getUserId() != loginUser.getId() && !loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        // 3. generate access token if absent
        if (StringUtils.isBlank(token)) {
            token = EncryptionUtils.getMd5(userId + expireTime + System.currentTimeMillis());
        }

        // 4. persist to the database
        accessToken.setUserId(userId);
        accessToken.setExpireTime(DateUtils.stringToDate(expireTime));
        accessToken.setToken(token);
        accessToken.setUpdateTime(new Date());

        accessTokenMapper.updateById(accessToken);

        result.put(Constants.DATA_LIST, accessToken);
        putMsg(result, Status.SUCCESS);
        return result;
    }
}
