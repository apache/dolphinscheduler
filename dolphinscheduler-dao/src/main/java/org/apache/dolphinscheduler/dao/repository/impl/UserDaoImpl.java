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

import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.UserWithWorkflowDefinitionCode;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.UserDao;

import java.util.Date;
import java.util.List;

import lombok.NonNull;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Repository
public class UserDaoImpl extends BaseDao<User, UserMapper> implements UserDao {

    public UserDaoImpl(@NonNull UserMapper userMapper) {
        super(userMapper);
    }

    @Override
    public List<User> queryAllGeneralUser() {
        return mybatisMapper.queryAllGeneralUser();
    }

    @Override
    public User queryByUserNameAccurately(String userName) {
        return mybatisMapper.queryByUserNameAccurately(userName);
    }

    @Override
    public User queryUserByNamePassword(String userName, String password) {
        return mybatisMapper.queryUserByNamePassword(userName, password);
    }

    @Override
    public IPage<User> queryUserPaging(Page<User> page, String userName) {
        return mybatisMapper.queryUserPaging(page, userName);
    }

    @Override
    public User queryDetailsById(int userId) {
        return mybatisMapper.queryDetailsById(userId);
    }

    @Override
    public List<User> queryUserListByTenant(int tenantId) {
        return mybatisMapper.queryUserListByTenant(tenantId);
    }

    @Override
    public User queryTenantCodeByUserId(int userId) {
        return mybatisMapper.queryTenantCodeByUserId(userId);
    }

    @Override
    public User queryUserByToken(String token, Date now) {
        return mybatisMapper.queryUserByToken(token, now);
    }

    @Override
    public List<User> queryUserListByQueue(String queueName) {
        return mybatisMapper.queryUserListByQueue(queueName);
    }

    @Override
    public Boolean existUser(String queue) {
        return mybatisMapper.existUser(queue);
    }

    @Override
    public Integer updateUserQueue(String oldQueue, String newQueue) {
        return mybatisMapper.updateUserQueue(oldQueue, newQueue);
    }

    @Override
    public List<User> queryAuthedUserListByProjectId(int projectId) {
        return mybatisMapper.queryAuthedUserListByProjectId(projectId);
    }

    @Override
    public List<User> queryEnabledUsers() {
        return mybatisMapper.queryEnabledUsers();
    }

    @Override
    public List<UserWithWorkflowDefinitionCode> queryUserWithWorkflowDefinitionCode(List<Long> workflowDefinitionCodes) {
        return mybatisMapper.queryUserWithWorkflowDefinitionCode(workflowDefinitionCodes);
    }
}
