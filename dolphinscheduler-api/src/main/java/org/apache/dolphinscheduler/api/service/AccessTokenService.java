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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.AccessToken;
import org.apache.dolphinscheduler.dao.entity.User;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.EncryptionUtils;
import org.apache.dolphinscheduler.dao.mapper.AccessTokenMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * user service
 */
@Service
public class AccessTokenService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(AccessTokenService.class);

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
    public Map<String, Object> queryAccessTokenList(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        Map<String, Object> result = new HashMap<>(5);

        PageInfo<AccessToken> pageInfo = new PageInfo<>(pageNo, pageSize);
        Page<AccessToken> page = new Page(pageNo, pageSize);
        int userId = loginUser.getId();
        if (loginUser.getUserType() == UserType.ADMIN_USER){
            userId = 0;
        }
        IPage<AccessToken> accessTokenList = accessTokenMapper.selectAccessTokenPage(page, searchVal, userId);
        pageInfo.setTotalCount((int)accessTokenList.getTotal());
        pageInfo.setLists(accessTokenList.getRecords());
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * create token
     *
     * @param loginUser
     * @param userId token for user
     * @param expireTime token expire time
     * @param token token string
     * @return create result code
     */
    public Map<String, Object> createToken(User loginUser, int userId, String expireTime, String token) {
        Map<String, Object> result = new HashMap<>(5);

        if (!hasPerm(loginUser,userId)){
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        if (userId <= 0) {
            throw new IllegalArgumentException("User id should not less than or equals to 0.");
        }
        AccessToken accessToken = new AccessToken();
        accessToken.setUserId(userId);
        accessToken.setExpireTime(DateUtils.stringToDate(expireTime));
        accessToken.setToken(token);
        accessToken.setCreateTime(new Date());
        accessToken.setUpdateTime(new Date());

        // insert
        int insert = accessTokenMapper.insert(accessToken);

        if (insert > 0) {
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
    public Map<String, Object> generateToken(User loginUser, int userId, String expireTime) {
        Map<String, Object> result = new HashMap<>(5);
        if (!hasPerm(loginUser,userId)){
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        String token = EncryptionUtils.getMd5(userId + expireTime + String.valueOf(System.currentTimeMillis()));
        result.put(Constants.DATA_LIST, token);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     *  delete access token
     * @param loginUser login user
     * @param id token id
     * @return delete result code
     */
    public Map<String, Object> delAccessTokenById(User loginUser, int id) {
        Map<String, Object> result = new HashMap<>(5);

        AccessToken accessToken = accessTokenMapper.selectById(id);

        if (accessToken == null) {
            logger.error("access token not exist,  access token id {}", id);
            putMsg(result, Status.ACCESS_TOKEN_NOT_EXIST);
            return result;
        }

        if (!hasPerm(loginUser,accessToken.getUserId())){
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
     * @param loginUser
     * @param id token id
     * @param userId token for user
     * @param expireTime token expire time
     * @param token token string
     * @return update result code
     */
    public Map<String, Object> updateToken(User loginUser, int id, int userId, String expireTime, String token) {
        Map<String, Object> result = new HashMap<>(5);

        if (!hasPerm(loginUser,userId)){
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        AccessToken accessToken = accessTokenMapper.selectById(id);
        if (accessToken == null) {
            logger.error("access token not exist,  access token id {}", id);
            putMsg(result, Status.ACCESS_TOKEN_NOT_EXIST);
            return result;
        }
        accessToken.setUserId(userId);
        accessToken.setExpireTime(DateUtils.stringToDate(expireTime));
        accessToken.setToken(token);
        accessToken.setUpdateTime(new Date());

        accessTokenMapper.updateById(accessToken);

        putMsg(result, Status.SUCCESS);
        return result;
    }
}
