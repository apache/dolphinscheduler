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
package cn.escheduler.api.service;

import cn.escheduler.api.enums.Status;
import cn.escheduler.api.utils.CheckUtils;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.PageInfo;
import cn.escheduler.api.utils.Result;
import cn.escheduler.common.enums.UserType;
import cn.escheduler.common.utils.*;
import cn.escheduler.dao.mapper.*;
import cn.escheduler.dao.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * @param loginUser
     * @param searchVal
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Map<String, Object> queryAccessTokenList(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        Map<String, Object> result = new HashMap<>(5);

        PageInfo<AccessToken> pageInfo = new PageInfo<>(pageNo, pageSize);
        Integer count;
        List<AccessToken> accessTokenList;
        if (loginUser.getUserType() == UserType.ADMIN_USER){
             count = accessTokenMapper.countAccessTokenPaging(0,searchVal);
            accessTokenList = accessTokenMapper.queryAccessTokenPaging(0,searchVal, pageInfo.getStart(), pageSize);
        }else {
            count = accessTokenMapper.countAccessTokenPaging(loginUser.getId(),searchVal);
            accessTokenList = accessTokenMapper.queryAccessTokenPaging(loginUser.getId(),searchVal, pageInfo.getStart(), pageSize);
        }

        pageInfo.setTotalCount(count);
        pageInfo.setLists(accessTokenList);
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * check
     *
     * @param result
     * @param bool
     * @param userNoOperationPerm
     * @param status
     * @return
     */
    private boolean check(Map<String, Object> result, boolean bool, Status userNoOperationPerm, String status) {
        //only admin can operate
        if (bool) {
            result.put(Constants.STATUS, userNoOperationPerm);
            result.put(status, userNoOperationPerm.getMsg());
            return true;
        }
        return false;
    }


    /**
     * create token
     *
     * @param userId
     * @param expireTime
     * @param token
     * @return
     */
    public Map<String, Object> createToken(int userId, String expireTime, String token) {
        Map<String, Object> result = new HashMap<>(5);

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
            putMsg(result, Status.CREATE_ALERT_GROUP_ERROR);
        }

        return result;
    }

    /**
     * generate token
     * @param userId
     * @param expireTime
     * @return
     */
    public Map<String, Object> generateToken(int userId, String expireTime) {
        Map<String, Object> result = new HashMap<>(5);
        String token = EncryptionUtils.getMd5(userId + expireTime + String.valueOf(System.currentTimeMillis()));
        result.put(Constants.DATA_LIST, token);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     *  delete access token
     * @param loginUser
     * @param id
     * @return
     */
    public Map<String, Object> delAccessTokenById(User loginUser, int id) {
        Map<String, Object> result = new HashMap<>(5);
        //only admin can operate
        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NOT_EXIST, id);
            return result;
        }

        accessTokenMapper.delete(id);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * update token by id
     * @param id
     * @param userId
     * @param expireTime
     * @param token
     * @return
     */
    public Map<String, Object> updateToken(int id,int userId, String expireTime, String token) {
        Map<String, Object> result = new HashMap<>(5);
        AccessToken accessToken = new AccessToken();
        accessToken.setId(id);
        accessToken.setUserId(userId);
        accessToken.setExpireTime(DateUtils.stringToDate(expireTime));
        accessToken.setToken(token);
        accessToken.setUpdateTime(new Date());

        accessTokenMapper.update(accessToken);

        putMsg(result, Status.SUCCESS);
        return result;
    }
}
