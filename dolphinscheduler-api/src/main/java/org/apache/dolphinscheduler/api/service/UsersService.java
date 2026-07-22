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
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface UsersService {

    /**
     * create user, only system admin have permission
     */
    User createUser(User loginUser, String userName, String userPassword, String email,
                    int tenantId, String phone, String queue, int state) throws Exception;

    User createUser(String userName, String userPassword, String email,
                    int tenantId, String phone, String queue, int state);

    /***
     * create User for ldap login
     */
    User createUser(UserType userType, String userId, String email);

    /**
     * get user by user name
     */
    User getUserByUserName(String userName);

    /**
     * query user by id
     */
    User queryUser(int id);

    /**
     * query user by ids
     */
    List<User> queryUser(List<Integer> ids);

    /**
     * query user
     */
    User queryUser(String name);

    /**
     * query user
     */
    User queryUser(String name, String password);

    /**
     * get user id by user name
     *
     * @return if name empty 0, user not exists -1, user exist user id
     */
    int getUserIdByName(String name);

    /**
     * query user list
     */
    Result queryUserList(User loginUser, String searchVal, Integer pageNo, Integer pageSize);

    /**
     * Update an existing user entity.
     */
    User updateUser(User user);

    User updateUser(User loginUser,
                    Integer userId,
                    String userName,
                    String userPassword,
                    String email,
                    Integer tenantId,
                    String phone,
                    String queue,
                    int state,
                    String timeZone) throws IOException;

    /**
     * delete user
     */
    void deleteUserById(User loginUser, int id) throws IOException;

    /**
     * grant project
     */
    void grantProject(User loginUser, int userId, String projectIds);

    /**
     * grant project with read permission
     */
    void grantProjectWithReadPerm(User loginUser, int userId, String projectIds);

    /**
     * grant project by code
     */
    void grantProjectByCode(User loginUser, int userId, long projectCode);

    /**
     * revoke the project permission for specified user by id
     */
    void revokeProjectById(User loginUser, int userId, String projectIds);

    /**
     * revoke the project permission for specified user by project code
     */
    void revokeProject(User loginUser, int userId, long projectCode);

    /**
     * grant namespace
     */
    void grantNamespaces(User loginUser, int userId, String namespaceIds);

    /**
     * grant datasource
     */
    void grantDataSource(User loginUser, int userId, String datasourceIds);

    /**
     * query user info (with tenantCode / alertGroup / timeZone populated, password stripped)
     */
    User getUserInfo(User loginUser);

    /**
     * query general user list
     */
    List<User> queryAllGeneralUsers(User loginUser);

    /**
     * query enabled user list
     */
    List<User> queryUserList(User loginUser);

    /**
     * verify user name exists
     */
    Result<Object> verifyUserName(String userName);

    /**
     * registry user, default state is 0, default tenant_id is 1, no phone, no queue
     */
    User registerUser(String userName, String userPassword, String repeatPassword, String email);

    /**
     * activate user, only system admin have permission, change user state code 0 to 1
     */
    User activateUser(User loginUser, String userName);

    /**
     * activate users, only system admin have permission, change users state code 0 to 1.
     * Returns a map with two buckets: {@code success} and {@code failed}, each carrying a
     * {@code sum} count plus per-user details, matching the historical wire shape.
     */
    Map<String, Object> batchActivateUser(User loginUser, List<String> userNames);

    /**
     * Make sure user with given name exists, and create the user if not exists
     * <p>
     * ONLY for python gateway server, and should not use this in web ui function
     */
    User createUserIfNotExists(String userName, String userPassword, String email, String phone, String tenantCode,
                               String queue,
                               int state) throws IOException;
}
