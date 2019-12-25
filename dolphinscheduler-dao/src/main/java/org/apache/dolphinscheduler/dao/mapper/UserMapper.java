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
package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * user mapper interface
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * query all general user
     * @return user list
     */
    List<User> queryAllGeneralUser();

    /**
     * query user by name
     * @param userName userName
     * @return user
     */
    User queryByUserNameAccurately(@Param("userName") String userName);

    /**
     * query user by userName and password
     * @param userName userName
     * @param password password
     * @return user
     */
    User queryUserByNamePassword(@Param("userName") String userName, @Param("password") String password);


    /**
     * user page
     * @param page page
     * @param userName userName
     * @return user IPage
     */
    IPage<User> queryUserPaging(Page page,
                                @Param("userName") String userName);

    /**
     * query user detail by id
     * @param userId userId
     * @return user
     */
    User queryDetailsById(@Param("userId") int userId);

    /**
     * query user list by alertgroupId
     * @param alertgroupId alertgroupId
     * @return user list
     */
    List<User> queryUserListByAlertGroupId(@Param("alertgroupId") int alertgroupId);

    /**
     * query user list by tenantId
     * @param tenantId tenantId
     * @return user list
     */
    List<User> queryUserListByTenant(@Param("tenantId") int tenantId);

    /**
     * query user by userId
     * @param userId userId
     * @return user
     */
    User queryTenantCodeByUserId(@Param("userId") int userId);

    /**
     * query user by token
     * @param token token
     * @return user
     */
    User queryUserByToken(@Param("token") String token);

    /**
     * query user by queue name
     * @param queueName queue name
     * @return user list
     */
    List<User> queryUserListByQueue(@Param("queueName") String queueName);

    /**
     * update user with old queue
     * @param oldQueue old queue name
     * @param newQueue new queue name
     * @return update rows
     */
    Integer updateUserQueue(@Param("oldQueue") String oldQueue, @Param("newQueue") String newQueue);
}
