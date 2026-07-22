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

package org.apache.dolphinscheduler.dao.repository;

import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.UserWithWorkflowDefinitionCode;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface UserDao extends IDao<User> {

    List<User> queryAllGeneralUser();

    User queryByUserNameAccurately(String userName);

    User queryUserByNamePassword(String userName, String password);

    IPage<User> queryUserPaging(Page<User> page, String userName);

    User queryDetailsById(int userId);

    List<User> queryUserListByTenant(int tenantId);

    User queryTenantCodeByUserId(int userId);

    User queryUserByToken(String token, Date now);

    List<User> queryUserListByQueue(String queueName);

    Boolean existUser(String queue);

    Integer updateUserQueue(String oldQueue, String newQueue);

    List<User> queryAuthedUserListByProjectId(int projectId);

    List<User> queryEnabledUsers();

    List<UserWithWorkflowDefinitionCode> queryUserWithWorkflowDefinitionCode(List<Long> workflowDefinitionCodes);
}
