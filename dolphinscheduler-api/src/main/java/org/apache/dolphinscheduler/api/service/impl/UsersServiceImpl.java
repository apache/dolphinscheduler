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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.USER_MANAGER;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.CheckUtils;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.constants.SystemConstants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.EncryptionUtils;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.DatasourceUser;
import org.apache.dolphinscheduler.dao.entity.K8sNamespaceUser;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AccessTokenMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.K8sNamespaceUserMapper;
import org.apache.dolphinscheduler.dao.repository.DataSourceUserDao;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.ProjectUserDao;
import org.apache.dolphinscheduler.dao.repository.TenantDao;
import org.apache.dolphinscheduler.dao.repository.UserDao;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Service
@Slf4j
public class UsersServiceImpl extends BaseServiceImpl implements UsersService {

    @Autowired
    private AccessTokenMapper accessTokenMapper;

    @Autowired
    private UserDao userDao;

    @Autowired
    private TenantDao tenantDao;

    @Autowired
    private ProjectUserDao projectUserDao;

    @Autowired
    private DataSourceUserDao datasourceUserDao;

    @Autowired
    private AlertGroupMapper alertGroupMapper;

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private K8sNamespaceUserMapper k8sNamespaceUserMapper;

    @Autowired
    private SessionService sessionService;

    /**
     * create user, only system admin have permission
     *
     * @param loginUser    login user
     * @param userName     user name
     * @param userPassword user password
     * @param email        email
     * @param tenantId     tenant id
     * @param phone        phone
     * @param queue        queue
     * @return create result code
     * @throws Exception exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createUser(User loginUser,
                           String userName,
                           String userPassword,
                           String email,
                           int tenantId,
                           String phone,
                           String queue,
                           int state) throws Exception {
        if (!isAdmin(loginUser)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        // check all user params
        String msg = this.checkUserParams(userName, userPassword, email, phone);
        if (!StringUtils.isEmpty(msg)) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, msg);
        }

        if (!checkTenantExists(tenantId)) {
            log.warn("Tenant does not exist, tenantId:{}.", tenantId);
            throw new ServiceException(Status.TENANT_NOT_EXIST);
        }

        User user = createUser(userName, userPassword, email, tenantId, phone, queue, state);
        log.info("User is created and id is {}.", user.getId());
        return user;
    }

    @Override
    @Transactional
    public User createUser(String userName,
                           String userPassword,
                           String email,
                           int tenantId,
                           String phone,
                           String queue,
                           int state) {
        User user = new User();
        Date now = new Date();

        user.setUserName(userName);
        user.setUserPassword(EncryptionUtils.getMd5(userPassword));
        user.setEmail(email);
        user.setTenantId(tenantId);
        user.setPhone(phone);
        user.setState(state);
        // create general users, administrator users are currently built-in
        user.setUserType(UserType.GENERAL_USER);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        if (StringUtils.isEmpty(queue)) {
            queue = "";
        }
        user.setQueue(queue);

        // save user
        userDao.insert(user);
        return user;
    }

    /***
     * create User for ldap、Casdoor SSO and OAuth2.0 login
     */
    @Override
    @Transactional
    public User createUser(UserType userType, String userId, String email) {
        User user = new User();
        Date now = new Date();

        user.setUserName(userId);
        user.setEmail(email);
        // create general users, administrator users are currently built-in
        user.setUserType(userType);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setTenantId(-1);
        user.setQueue("");
        user.setState(Flag.YES.getCode());

        // save user
        userDao.insert(user);
        return user;
    }

    /**
     * get user by user name
     *
     * @param userName user name
     * @return exist user or null
     */
    @Override
    public User getUserByUserName(String userName) {
        return userDao.queryByUserNameAccurately(userName);
    }

    /**
     * query user by id
     *
     * @param id id
     * @return user info
     */
    @Override
    public User queryUser(int id) {
        return userDao.queryById(id);
    }

    @Override
    public List<User> queryUser(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return userDao.queryByIds(ids);
    }

    /**
     * query user
     *
     * @param name name
     * @return user info
     */
    @Override
    public User queryUser(String name) {
        return userDao.queryByUserNameAccurately(name);
    }

    /**
     * query user
     *
     * @param name     name
     * @param password password
     * @return user info
     */
    @Override
    public User queryUser(String name, String password) {
        String md5 = EncryptionUtils.getMd5(password);
        return userDao.queryUserByNamePassword(name, md5);
    }

    /**
     * get user id by user name
     *
     * @param name user name
     * @return if name empty 0, user not exists -1, user exist user id
     */
    @Override
    public int getUserIdByName(String name) {
        // executor name query
        int executorId = 0;
        if (StringUtils.isNotEmpty(name)) {
            User executor = queryUser(name);
            if (null != executor) {
                executorId = executor.getId();
            } else {
                executorId = -1;
            }
        }

        return executorId;
    }

    /**
     * query user list
     *
     * @param loginUser login user
     * @param pageNo    page number
     * @param searchVal search value
     * @param pageSize  page size
     * @return user list page
     */
    @Override
    public Result<Object> queryUserList(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        Result<Object> result = new Result<>();

        if (!isAdmin(loginUser)) {
            log.warn("User does not have permission for this feature, userId:{}, userName:{}.", loginUser.getId(),
                    loginUser.getUserName());
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        Page<User> page = new Page<>(pageNo, pageSize);

        IPage<User> scheduleList = userDao.queryUserPaging(page, searchVal);

        PageInfo<User> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal((int) scheduleList.getTotal());
        pageInfo.setTotalList(scheduleList.getRecords());
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        if (user == null || user.getId() == null) {
            throw new ServiceException(Status.USER_NOT_EXIST);
        }
        // Ensure the update time is set
        user.setUpdateTime(new Date());
        boolean updated = userDao.updateById(user);

        if (!updated) {
            throw new ServiceException(Status.UPDATE_USER_ERROR);
        }
        return user;
    }

    @Override
    @Transactional
    public User updateUser(User loginUser,
                           Integer userId,
                           String userName,
                           String userPassword,
                           String email,
                           Integer tenantId,
                           String phone,
                           String queue,
                           int state,
                           String timeZone) {

        if (!canOperator(loginUser, userId)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }
        User user = userDao.queryById(userId);
        if (user == null) {
            throw new ServiceException(Status.USER_NOT_EXIST, userId);
        }

        // non-admin should not modify tenantId and queue
        if (!isAdmin(loginUser)) {
            if (tenantId != null && user.getTenantId() != tenantId) {
                throw new ServiceException(Status.USER_NO_OPERATION_PERM);
            }
            if (StringUtils.isNotEmpty(queue) && !StringUtils.equals(queue, user.getQueue())) {
                throw new ServiceException(Status.USER_NO_OPERATION_PERM);
            }
        }

        if (StringUtils.isNotEmpty(userName)) {

            if (!CheckUtils.checkUserName(userName)) {
                throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, userName);
            }

            // todo: use the db unique index
            User tempUser = userDao.queryByUserNameAccurately(userName);
            if (tempUser != null && !userId.equals(tempUser.getId())) {
                throw new ServiceException(Status.USER_NAME_EXIST);
            }
            user.setUserName(userName);
        }

        if (StringUtils.isNotEmpty(userPassword)) {
            if (!CheckUtils.checkPasswordLength(userPassword)) {
                throw new ServiceException(Status.USER_PASSWORD_LENGTH_ERROR);
            }
            user.setUserPassword(EncryptionUtils.getMd5(userPassword));
            sessionService.expireSession(user.getId());
        }

        if (StringUtils.isNotEmpty(email)) {
            if (!CheckUtils.checkEmail(email)) {
                throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, email);
            }
            user.setEmail(email);
        }

        if (StringUtils.isNotEmpty(phone) && !CheckUtils.checkPhone(phone)) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, phone);
        }

        if (state == 0 && user.getState() != state && Objects.equals(loginUser.getId(), user.getId())) {
            throw new ServiceException(Status.NOT_ALLOW_TO_DISABLE_OWN_ACCOUNT);
        }

        if (StringUtils.isNotEmpty(timeZone)) {
            if (!CheckUtils.checkTimeZone(timeZone)) {
                throw new ServiceException(Status.TIME_ZONE_ILLEGAL, timeZone);
            }
            user.setTimeZone(timeZone);
        }

        user.setPhone(phone);
        user.setQueue(queue);
        user.setState(state);
        user.setUpdateTime(new Date());
        user.setTenantId(tenantId);
        // updateWorkflowInstance user
        if (!userDao.updateById(user)) {
            throw new ServiceException(Status.UPDATE_USER_ERROR);
        }
        return user;
    }

    /**
     * delete user
     *
     * @param loginUser login user
     * @param id        user id
     * @return delete result code
     * @throws Exception exception when operate hdfs
     */
    @Override
    @Transactional
    public void deleteUserById(User loginUser, int id) throws IOException {
        // only admin can operate
        if (!isAdmin(loginUser)) {
            log.warn("User does not have permission for this feature, userId:{}, userName:{}.", loginUser.getId(),
                    loginUser.getUserName());
            throw new ServiceException(Status.USER_NO_OPERATION_PERM, id);
        }
        // check exist
        User tempUser = userDao.queryById(id);
        if (tempUser == null) {
            log.error("User does not exist, userId:{}.", id);
            throw new ServiceException(Status.USER_NOT_EXIST, id);
        }
        // check if is a project owner
        List<Project> projects = projectDao.queryProjectCreatedByUser(id);
        if (CollectionUtils.isNotEmpty(projects)) {
            String projectNames = projects.stream().map(Project::getName).collect(Collectors.joining(","));
            log.warn("Please transfer the project ownership before deleting the user, userId:{}, projects:{}.", id,
                    projectNames);
            throw new ServiceException(Status.TRANSFORM_PROJECT_OWNERSHIP, projectNames);
        }
        // delete user
        userDao.queryTenantCodeByUserId(id);

        accessTokenMapper.deleteAccessTokenByUserId(id);
        sessionService.expireSession(id);

        if (!userDao.deleteById(id)) {
            log.error("User delete error, userId:{}.", id);
            throw new ServiceException(Status.DELETE_USER_BY_ID_ERROR);
        }
        log.info("User is deleted and id is :{}.", id);
    }

    /**
     * revoke the project permission for specified user by id
     *
     * @param loginUser  Login user
     * @param userId     User id
     * @param projectIds project id array
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void revokeProjectById(User loginUser, int userId, String projectIds) {
        // 1. only admin can operate
        if (!this.isAdmin(loginUser)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        // 2. check if user is existed
        User user = this.userDao.queryById(userId);
        if (user == null) {
            throw new ServiceException(Status.USER_NOT_EXIST, userId);
        }

        Arrays.stream(projectIds.split(",")).distinct().forEach(projectId -> {
            // 3. check if project is existed
            Project project = this.projectDao.queryDetailById(Integer.parseInt(projectId));
            if (project != null) {
                // 4. delete the relationship between project and user
                this.projectUserDao.deleteProjectRelation(project.getId(), user.getId());
            }
        });
    }

    /**
     * grant project with read permission
     *
     * @param loginUser  login user
     * @param userId     user id
     * @param projectIds project id array
     * @return grant result code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void grantProjectWithReadPerm(User loginUser, int userId, String projectIds) {
        if (!isAdmin(loginUser)) {
            throw new ServiceException(Status.NO_CURRENT_OPERATING_PERMISSION);
        }

        // check exist
        User tempUser = userDao.queryById(userId);
        if (tempUser == null) {
            throw new ServiceException(Status.USER_NOT_EXIST, userId);
        }

        if (StringUtils.isEmpty(projectIds)) {
            return;
        }
        Arrays.stream(projectIds.split(Constants.COMMA)).distinct().forEach(projectId -> {
            ProjectUser projectUserOld = projectUserDao.queryProjectRelation(Integer.parseInt(projectId), userId);
            if (projectUserOld != null) {
                projectUserDao.deleteProjectRelation(Integer.parseInt(projectId), userId);
            }
            Date now = new Date();
            ProjectUser projectUser = new ProjectUser();
            projectUser.setUserId(userId);
            projectUser.setProjectId(Integer.parseInt(projectId));
            projectUser.setPerm(Constants.READ_PERMISSION);
            projectUser.setCreateTime(now);
            projectUser.setUpdateTime(now);
            projectUserDao.insert(projectUser);
        });
    }

    /**
     * grant project
     *
     * @param loginUser  login user
     * @param userId     user id
     * @param projectIds project id array
     * @return grant result code
     */
    @Override
    @Transactional
    public void grantProject(User loginUser, int userId, String projectIds) {
        // check exist
        User tempUser = userDao.queryById(userId);
        if (tempUser == null) {
            log.error("User does not exist, userId:{}.", userId);
            throw new ServiceException(Status.USER_NOT_EXIST, userId);
        }

        if (!isAdmin(loginUser)) {
            throw new ServiceException(Status.NO_CURRENT_OPERATING_PERMISSION);
        }

        if (StringUtils.isEmpty(projectIds)) {
            log.warn("Parameter projectIds is empty.");
            return;
        }
        Arrays.stream(projectIds.split(",")).distinct().forEach(projectId -> {
            ProjectUser projectUserOld = projectUserDao.queryProjectRelation(Integer.parseInt(projectId), userId);
            if (projectUserOld != null) {
                projectUserDao.deleteProjectRelation(Integer.parseInt(projectId), userId);
            }
            Date now = new Date();
            ProjectUser projectUser = new ProjectUser();
            projectUser.setUserId(userId);
            projectUser.setProjectId(Integer.parseInt(projectId));
            projectUser.setPerm(Constants.AUTHORIZE_WRITABLE_PERM);
            projectUser.setCreateTime(now);
            projectUser.setUpdateTime(now);
            projectUserDao.insert(projectUser);
        });
    }

    /**
     * grant project by code
     *
     * @param loginUser   login user
     * @param userId      user id
     * @param projectCode project code
     * @return grant result code
     */
    @Override
    public void grantProjectByCode(final User loginUser, final int userId, final long projectCode) {
        // 1. check if user is existed
        User tempUser = this.userDao.queryById(userId);
        if (tempUser == null) {
            log.error("User does not exist, userId:{}.", userId);
            throw new ServiceException(Status.USER_NOT_EXIST, userId);
        }

        // 2. check if project is existed
        Project project = this.projectDao.queryByCode(projectCode);
        if (project == null) {
            log.error("Project does not exist, projectCode:{}.", projectCode);
            throw new ServiceException(Status.PROJECT_NOT_FOUND, projectCode);
        }

        // 3. only project owner can operate
        if (!this.canOperator(loginUser, project.getUserId())) {
            log.warn("User does not have permission for project, userId:{}, userName:{}, projectCode:{}.",
                    loginUser.getId(), loginUser.getUserName(), projectCode);
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        // 4. maintain the relationship between project and user if not exists
        ProjectUser projectUser = projectUserDao.queryProjectRelation(project.getId(), userId);
        if (projectUser == null) {
            Date today = new Date();
            projectUser = new ProjectUser();
            projectUser.setUserId(userId);
            projectUser.setProjectId(project.getId());
            projectUser.setPerm(Constants.AUTHORIZE_WRITABLE_PERM);
            projectUser.setCreateTime(today);
            projectUser.setUpdateTime(today);
            this.projectUserDao.insert(projectUser);
        }
        log.info("User is granted permission for projects, userId:{}, projectCode:{}.", userId, projectCode);
    }

    /**
     * revoke the project permission for specified user.
     *
     * @param loginUser   Login user
     * @param userId      User id
     * @param projectCode Project Code
     * @return
     */
    @Override
    public void revokeProject(User loginUser, int userId, long projectCode) {
        // 1. only admin can operate
        if (!this.isAdmin(loginUser)) {
            log.warn("Only admin can revoke the project permission.");
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        // 2. check if user is existed
        User user = this.userDao.queryById(userId);
        if (user == null) {
            log.error("User does not exist, userId:{}.", userId);
            throw new ServiceException(Status.USER_NOT_EXIST, userId);
        }

        // 3. check if project is existed
        Project project = this.projectDao.queryByCode(projectCode);
        if (project == null) {
            log.error("Project does not exist, projectCode:{}.", projectCode);
            throw new ServiceException(Status.PROJECT_NOT_FOUND, projectCode);
        }

        // 4. delete th relationship between project and user
        this.projectUserDao.deleteProjectRelation(project.getId(), user.getId());
        log.info("User is revoked permission for projects, userId:{}, projectCode:{}.", userId, projectCode);
    }

    /**
     * grant namespace
     *
     * @param loginUser    login user
     * @param userId       user id
     * @param namespaceIds namespace id array
     * @return grant result code
     */
    @Override
    @Transactional
    public void grantNamespaces(User loginUser, int userId, String namespaceIds) {
        // only admin can operate
        if (!this.isAdmin(loginUser)) {
            log.warn("Only admin can grant namespaces.");
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        // check exist
        User tempUser = userDao.queryById(userId);
        if (tempUser == null) {
            log.error("User does not exist, userId:{}.", userId);
            throw new ServiceException(Status.USER_NOT_EXIST, userId);
        }

        k8sNamespaceUserMapper.deleteNamespaceRelation(0, userId);
        if (StringUtils.isNotEmpty(namespaceIds)) {
            String[] namespaceIdArr = namespaceIds.split(",");
            for (String namespaceId : namespaceIdArr) {
                Date now = new Date();
                K8sNamespaceUser namespaceUser = new K8sNamespaceUser();
                namespaceUser.setUserId(userId);
                namespaceUser.setNamespaceId(Integer.parseInt(namespaceId));
                namespaceUser.setPerm(7);
                namespaceUser.setCreateTime(now);
                namespaceUser.setUpdateTime(now);
                k8sNamespaceUserMapper.insert(namespaceUser);
            }
        }

        log.info("User is granted permission for namespace, userId:{}.", tempUser.getId());
    }

    /**
     * grant datasource
     *
     * @param loginUser     login user
     * @param userId        user id
     * @param datasourceIds data source id array
     * @return grant result code
     */
    @Override
    @Transactional
    public void grantDataSource(User loginUser, int userId, String datasourceIds) {
        // only admin can operate
        if (!this.isAdmin(loginUser)) {
            log.warn("Only admin can grant datasource.");
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }
        User user = userDao.queryById(userId);
        if (user == null) {
            throw new ServiceException(Status.USER_NOT_EXIST, userId);
        }

        datasourceUserDao.deleteByUserId(userId);

        if (StringUtils.isEmpty(datasourceIds)) {
            return;
        }

        String[] datasourceIdArr = datasourceIds.split(",");

        for (String datasourceId : datasourceIdArr) {
            Date now = new Date();

            DatasourceUser datasourceUser = new DatasourceUser();
            datasourceUser.setUserId(userId);
            datasourceUser.setDatasourceId(Integer.parseInt(datasourceId));
            datasourceUser.setPerm(Constants.AUTHORIZE_WRITABLE_PERM);
            datasourceUser.setCreateTime(now);
            datasourceUser.setUpdateTime(now);
            datasourceUserDao.insert(datasourceUser);
        }
    }

    /**
     * query user info
     *
     * @param loginUser login user
     * @return user info
     */
    @Override
    public User getUserInfo(User loginUser) {
        User user;
        if (loginUser.getUserType() == UserType.ADMIN_USER) {
            user = loginUser;
        } else {
            user = userDao.queryDetailsById(loginUser.getId());

            List<AlertGroup> alertGroups = alertGroupMapper.queryByUserId(loginUser.getId());

            StringBuilder sb = new StringBuilder();

            if (alertGroups != null && !alertGroups.isEmpty()) {
                for (int i = 0; i < alertGroups.size() - 1; i++) {
                    sb.append(alertGroups.get(i).getGroupName()).append(",");
                }
                sb.append(alertGroups.get(alertGroups.size() - 1));
                user.setAlertGroup(sb.toString());
            }
        }

        Tenant tenant = tenantDao.queryById(user.getTenantId());
        if (tenant != null) {
            user.setTenantCode(tenant.getTenantCode());
        }

        // add system default timezone if not user timezone
        if (StringUtils.isEmpty(user.getTimeZone())) {
            user.setTimeZone(SystemConstants.DEFAULT_TIME_ZONE.toZoneId().getId());
        }

        // remove password
        user.setUserPassword(null);
        return user;
    }

    /**
     * query user list
     *
     * @param loginUser login user
     * @return user list
     */
    @Override
    public List<User> queryAllGeneralUsers(User loginUser) {
        // only admin can operate
        if (!isAdmin(loginUser)) {
            log.warn("Only admin can query all general users.");
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }
        return userDao.queryAllGeneralUser();
    }

    /**
     * query user list
     *
     * @param loginUser login user
     * @return user list
     */
    @Override
    public List<User> queryUserList(User loginUser) {
        // only admin can operate
        if (!canOperatorPermissions(loginUser, null, AuthorizationType.ACCESS_TOKEN, USER_MANAGER)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }
        return userDao.queryEnabledUsers();
    }

    /**
     * verify user name exists
     *
     * @param userName user name
     * @return true if user name not exists, otherwise return false
     */
    @Override
    public Result<Object> verifyUserName(String userName) {

        Result<Object> result = new Result<>();
        User user = userDao.queryByUserNameAccurately(userName);
        if (user != null) {
            putMsg(result, Status.USER_NAME_EXIST);
        } else {
            putMsg(result, Status.SUCCESS);
        }

        return result;
    }

    /**
     * unauthorized user
     *
     * @param loginUser    login user
     * @param alertgroupId alert group id
     * @return unauthorize result code
     */
    @Override
    public List<User> unauthorizedUser(User loginUser, Integer alertgroupId) {
        // only admin can operate
        if (!isAdmin(loginUser)) {
            log.warn("Only admin can deauthorize user.");
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        List<User> userList = userDao.queryAll();
        List<User> resultUsers = new ArrayList<>();
        Set<User> userSet;
        if (userList != null && !userList.isEmpty()) {
            userSet = new HashSet<>(userList);

            List<User> authedUserList = userDao.queryUserListByAlertGroupId(alertgroupId);

            if (authedUserList != null && !authedUserList.isEmpty()) {
                Set<User> authedUserSet = new HashSet<>(authedUserList);
                userSet.removeAll(authedUserSet);
            }
            resultUsers = new ArrayList<>(userSet);
        }
        return resultUsers;
    }

    /**
     * authorized user
     *
     * @param loginUser    login user
     * @param alertGroupId alert group id
     * @return authorized result code
     */
    @Override
    public List<User> authorizedUser(User loginUser, Integer alertGroupId) {
        // only admin can operate
        if (!isAdmin(loginUser)) {
            log.warn("Only admin can authorize user.");
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }
        return userDao.queryUserListByAlertGroupId(alertGroupId);
    }

    /**
     * @param tenantId tenant id
     * @return true if tenant exists, otherwise return false
     */
    private boolean checkTenantExists(int tenantId) {
        return tenantDao.queryDetailById(tenantId) != null;
    }

    /**
     * @return if check failed return the field, otherwise return null
     */
    private String checkUserParams(String userName, String password, String email, String phone) {

        String msg = null;
        if (!CheckUtils.checkUserName(userName)) {
            log.warn("Parameter userName check failed.");
            msg = userName;
        } else if (!CheckUtils.checkPassword(password)) {
            log.warn("Parameter password check failed.");
            msg = password;
        } else if (!CheckUtils.checkEmail(email)) {
            log.warn("Parameter email check failed.");
            msg = email;
        } else if (!CheckUtils.checkPhone(phone)) {
            log.warn("Parameter phone check failed.");
            msg = phone;
        }

        return msg;
    }

    /**
     * registry user, default state is 0, default tenant_id is 1, no phone, no queue
     *
     * @param userName       user name
     * @param userPassword   user password
     * @param repeatPassword repeat password
     * @param email          email
     * @return registry result code
     * @throws Exception exception
     */
    @Override
    @Transactional
    public User registerUser(String userName, String userPassword, String repeatPassword, String email) {
        // check user params
        String msg = this.checkUserParams(userName, userPassword, email, "");
        if (!StringUtils.isEmpty(msg)) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, msg);
        }

        if (!userPassword.equals(repeatPassword)) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, "two passwords are not same");
        }
        return createUser(userName, userPassword, email, -1, "", "", Flag.NO.ordinal());
    }

    /**
     * activate user, only system admin have permission, change user state code 0 to 1
     *
     * @param loginUser login user
     * @param userName  user name
     * @return create result code
     */
    @Override
    public User activateUser(User loginUser, String userName) {
        if (!isAdmin(loginUser)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        if (!CheckUtils.checkUserName(userName)) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, userName);
        }

        User user = userDao.queryByUserNameAccurately(userName);

        if (user == null) {
            throw new ServiceException(Status.USER_NOT_EXIST, userName);
        }

        if (user.getState() != Flag.NO.ordinal()) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, userName);
        }

        user.setState(Flag.YES.ordinal());
        user.setUpdateTime(new Date());
        userDao.updateById(user);

        return userDao.queryByUserNameAccurately(userName);
    }

    /**
     * activate user, only system admin have permission, change users state code 0 to 1
     *
     * @param loginUser login user
     * @param userNames user name
     * @return create result code
     */
    @Override
    public Map<String, Object> batchActivateUser(User loginUser, List<String> userNames) {
        if (!isAdmin(loginUser)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        int totalSuccess = 0;
        List<String> successUserNames = new ArrayList<>();
        int totalFailed = 0;
        List<Map<String, String>> failedInfo = new ArrayList<>();
        for (String userName : userNames) {
            try {
                activateUser(loginUser, userName);
                totalSuccess++;
                successUserNames.add(userName);
            } catch (ServiceException e) {
                totalFailed++;
                Map<String, String> failedBody = new HashMap<>();
                failedBody.put("userName", userName);
                failedBody.put("msg", e.getMessage());
                failedInfo.add(failedBody);
            }
        }
        Map<String, Object> successRes = new HashMap<>();
        successRes.put("sum", totalSuccess);
        successRes.put("userName", successUserNames);
        Map<String, Object> failedRes = new HashMap<>();
        failedRes.put("sum", totalFailed);
        failedRes.put("info", failedInfo);
        Map<String, Object> res = new HashMap<>();
        res.put("success", successRes);
        res.put("failed", failedRes);
        return res;
    }

    /**
     * Make sure user with given name exists, and create the user if not exists
     * <p>
     * ONLY for python gateway server, and should not use this in web ui function
     *
     * @param userName     user name
     * @param userPassword user password
     * @param email        user email
     * @param phone        user phone
     * @param tenantCode   tenant code
     * @param queue        queue
     * @param state        state
     * @return create result code
     */
    @Override
    @Transactional
    public User createUserIfNotExists(String userName,
                                      String userPassword,
                                      String email,
                                      String phone,
                                      String tenantCode,
                                      String queue,
                                      int state) {
        User user = userDao.queryByUserNameAccurately(userName);
        if (Objects.isNull(user)) {
            Tenant tenant = tenantDao.queryByCode(tenantCode).orElse(null);
            user = createUser(userName, userPassword, email, tenant.getId(), phone, queue, state);
            return user;
        }

        updateUser(user, user.getId(), userName, userPassword, email, user.getTenantId(), phone, queue, state, null);
        user = userDao.queryDetailsById(user.getId());
        return user;
    }
}
