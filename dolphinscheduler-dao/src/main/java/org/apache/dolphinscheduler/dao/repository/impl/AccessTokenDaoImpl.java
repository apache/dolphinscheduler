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

package org.apache.dolphinscheduler.dao.repository.impl;

import org.apache.dolphinscheduler.dao.entity.AccessToken;
import org.apache.dolphinscheduler.dao.mapper.AccessTokenMapper;
import org.apache.dolphinscheduler.dao.repository.AccessTokenDao;
import org.apache.dolphinscheduler.dao.repository.BaseDao;

import java.util.List;

import lombok.NonNull;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Repository
public class AccessTokenDaoImpl extends BaseDao<AccessToken, AccessTokenMapper> implements AccessTokenDao {

    public AccessTokenDaoImpl(@NonNull AccessTokenMapper accessTokenMapper) {
        super(accessTokenMapper);
    }

    @Override
    public IPage<AccessToken> queryAccessTokenPage(Page<AccessToken> page, String userName, int userId) {
        return mybatisMapper.selectAccessTokenPage(page, userName, userId);
    }

    @Override
    public List<AccessToken> queryAccessTokenByUser(int userId) {
        return mybatisMapper.queryAccessTokenByUser(userId);
    }

    @Override
    public void deleteByUserId(int userId) {
        mybatisMapper.deleteAccessTokenByUserId(userId);
    }

    @Override
    public List<AccessToken> listAuthorizedAccessToken(int userId, List<Integer> accessTokensIds) {
        return mybatisMapper.listAuthorizedAccessToken(userId, accessTokensIds);
    }
}
