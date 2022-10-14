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

import org.apache.dolphinscheduler.api.dto.resources.ResourceComponent;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.CheckUtils;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.EncryptionUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.DatasourceUser;
import org.apache.dolphinscheduler.dao.entity.K8sNamespaceUser;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.ResourcesUser;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.UDFUser;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AccessTokenMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.DataSourceUserMapper;
import org.apache.dolphinscheduler.dao.mapper.K8sNamespaceUserMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectUserMapper;
import org.apache.dolphinscheduler.dao.mapper.ResourceMapper;
import org.apache.dolphinscheduler.dao.mapper.ResourceUserMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UDFUserMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.dao.utils.ResourceProcessDefinitionUtils;
import org.apache.dolphinscheduler.service.storage.StorageOperate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * users service impl
 */
@Service
public class UsersServiceImpl extends BaseServiceImpl implements UsersService {

    private static final Logger logger = LoggerFactory.getLogger(UsersServiceImpl.class);

    @Autowired
    private AccessTokenMapper accessTokenMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private ProjectUserMapper projectUserMapper;

    @Autowired
    private ResourceUserMapper resourceUserMapper;

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private DataSourceUserMapper datasourceUserMapper;

    @Autowired
    private UDFUserMapper udfUserMapper;

    @Autowired
    private AlertGroupMapper alertGroupMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired(required = false)
    private StorageOperate storageOperate;

    @Autowired
    private K8sNamespaceUserMapper k8sNamespaceUserMapper;

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
    public Map<String, Object> createUser(User loginUser,
                                          String userName,
                                          String userPassword,
                                          String email,
                                          int tenantId,
                                          String phone,
                                          String queue,
                                          int state) throws Exception {
        Map<String, Object> result = new HashMap<>();

        // check all user params
        String msg = this.checkUserParams(userName, userPassword, email, phone);
        if (resourcePermissionCheckService.functionDisabled()) {
            putMsg(result, Status.FUNCTION_DISABLED, msg);
            return result;
        }

        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        if (!StringUtils.isEmpty(msg)) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, msg);
            return result;
        }

        if (!checkTenantExists(tenantId)) {
            logger.warn("Tenant does not exist, tenantId:{}.", tenantId);
            putMsg(result, Status.TENANT_NOT_EXIST);
            return result;
        }

        User user = createUser(userName, userPassword, email, tenantId, phone, queue, state);

        Tenant tenant = tenantMapper.queryById(tenantId);
        // resource upload startup
        if (PropertyUtils.getResUploadStartupState()) {
            storageOperate.createTenantDirIfNotExists(tenant.getTenantCode());
        }

        logger.info("User is created and id is {}.", user.getId());
        result.put(Constants.DATA_LIST, user);
        putMsg(result, Status.SUCCESS);
        return result;
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
        userMapper.insert(user);
        return user;
    }

    /***
     * create User for ldap login
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
        user.setQueue("");
        user.setState(Flag.YES.getCode());

        // save user
        userMapper.insert(user);
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
        return userMapper.queryByUserNameAccurately(userName);
    }

    /**
     * query user by id
     *
     * @param id id
     * @return user info
     */
    @Override
    public User queryUser(int id) {
        return userMapper.selectById(id);
    }

    @Override
    public List<User> queryUser(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return userMapper.selectByIds(ids);
    }

    /**
     * query user
     *
     * @param name name
     * @return user info
     */
    @Override
    public User queryUser(String name) {
        return userMapper.queryByUserNameAccurately(name);
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
        return userMapper.queryUserByNamePassword(name, md5);
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
        if (resourcePermissionCheckService.functionDisabled()) {
            putMsg(result, Status.FUNCTION_DISABLED);
            return result;
        }
        if (!isAdmin(loginUser)) {
            logger.warn("User does not have permission for this feature, userId:{}, userName:{}.", loginUser.getId(), loginUser.getUserName());
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        Page<User> page = new Page<>(pageNo, pageSize);

        IPage<User> scheduleList = userMapper.queryUserPaging(page, searchVal);

        PageInfo<User> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal((int) scheduleList.getTotal());
        pageInfo.setTotalList(scheduleList.getRecords());
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * updateProcessInstance user
     *
     * @param userId       user id
     * @param userName     user name
     * @param userPassword user password
     * @param email        email
     * @param tenantId     tenant id
     * @param phone        phone
     * @param queue        queue
     * @param state        state
     * @param timeZone     timeZone
     * @return update result code
     * @throws Exception exception
     */
    @Override
    public Map<String, Object> updateUser(User loginUser, int userId,
                                          String userName,
                                          String userPassword,
                                          String email,
                                          int tenantId,
                                          String phone,
                                          String queue,
                                          int state,
                                          String timeZone) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, false);

        if (resourcePermissionCheckService.functionDisabled()) {
            putMsg(result, Status.FUNCTION_DISABLED);
            return result;
        }
        if (check(result, !canOperator(loginUser, userId), Status.USER_NO_OPERATION_PERM)) {
            logger.warn("User does not have permission for this feature, userId:{}, userName:{}.", loginUser.getId(), loginUser.getUserName());
            return result;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            logger.error("User does not exist, userId:{}.", userId);
            putMsg(result, Status.USER_NOT_EXIST, userId);
            return result;
        }
        if (StringUtils.isNotEmpty(userName)) {

            if (!CheckUtils.checkUserName(userName)) {
                logger.warn("Parameter userName check failed.");
                putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, userName);
                return result;
            }

            User tempUser = userMapper.queryByUserNameAccurately(userName);
            if (tempUser != null && tempUser.getId() != userId) {
                logger.warn("User name already exists, userName:{}.", tempUser.getUserName());
                putMsg(result, Status.USER_NAME_EXIST);
                return result;
            }
            user.setUserName(userName);
        }

        if (StringUtils.isNotEmpty(userPassword)) {
            if (!CheckUtils.checkPasswordLength(userPassword)) {
                logger.warn("Parameter userPassword check failed.");
                putMsg(result, Status.USER_PASSWORD_LENGTH_ERROR);
                return result;
            }
            user.setUserPassword(EncryptionUtils.getMd5(userPassword));
        }

        if (StringUtils.isNotEmpty(email)) {
            if (!CheckUtils.checkEmail(email)) {
                logger.warn("Parameter email check failed.");
                putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, email);
                return result;
            }
            user.setEmail(email);
        }

        if (StringUtils.isNotEmpty(phone) && !CheckUtils.checkPhone(phone)) {
            logger.warn("Parameter phone check failed.");
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, phone);
            return result;
        }

        if (state == 0 && user.getState() != state && Objects.equals(loginUser.getId(), user.getId())) {
            logger.warn("Not allow to disable your own account, userId:{}, userName:{}.", user.getId(), user.getUserName());
            putMsg(result, Status.NOT_ALLOW_TO_DISABLE_OWN_ACCOUNT);
            return result;
        }

        if (StringUtils.isNotEmpty(timeZone)) {
            if (!CheckUtils.checkTimeZone(timeZone)) {
                logger.warn("Parameter time zone is illegal.");
                putMsg(result, Status.TIME_ZONE_ILLEGAL, timeZone);
                return result;
            }
            user.setTimeZone(timeZone);
        }

        user.setPhone(phone);
        user.setQueue(queue);
        user.setState(state);
        Date now = new Date();
        user.setUpdateTime(now);
        user.setTenantId(tenantId);
        // updateProcessInstance user
        int update = userMapper.updateById(user);
        if (update > 0) {
            logger.info("User is updated and id is :{}.", userId);
            putMsg(result, Status.SUCCESS);
        } else {
            logger.error("User update error, userId:{}.", userId);
            putMsg(result, Status.UPDATE_USER_ERROR);
        }

        return result;
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
    public Map<String, Object> deleteUserById(User loginUser, int id) throws IOException {
        Map<String, Object> result = new HashMap<>();
        if (resourcePermissionCheckService.functionDisabled()) {
            putMsg(result, Status.FUNCTION_DISABLED);
            return result;
        }
        // only admin can operate
        if (!isAdmin(loginUser)) {
            logger.warn("User does not have permission for this feature, userId:{}, userName:{}.", loginUser.getId(), loginUser.getUserName());
            putMsg(result, Status.USER_NO_OPERATION_PERM, id);
            return result;
        }
        // check exist
        User tempUser = userMapper.selectById(id);
        if (tempUser == null) {
            logger.error("User does not exist, userId:{}.", id);
            putMsg(result, Status.USER_NOT_EXIST, id);
            return result;
        }
        // check if is a project owner
        List<Project> projects = projectMapper.queryProjectCreatedByUser(id);
        if (CollectionUtils.isNotEmpty(projects)) {
            String projectNames = projects.stream().map(Project::getName).collect(Collectors.joining(","));
            putMsg(result, Status.TRANSFORM_PROJECT_OWNERSHIP, projectNames);
            logger.warn("Please transfer the project ownership before deleting the user, userId:{}, projects:{}.", id, projectNames);
            return result;
        }
        // delete user
        userMapper.queryTenantCodeByUserId(id);

        accessTokenMapper.deleteAccessTokenByUserId(id);

        if (userMapper.deleteById(id) > 0) {
            logger.info("User is deleted and id is :{}.", id);
            putMsg(result, Status.SUCCESS);
            return result;
        } else {
            logger.error("User delete error, userId:{}.", id);
            putMsg(result, Status.DELETE_USER_BY_ID_ERROR);
            return result;
        }
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
    public Map<String, Object> grantProject(User loginUser, int userId, String projectIds) {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, false);

        if (resourcePermissionCheckService.functionDisabled()) {
            putMsg(result, Status.FUNCTION_DISABLED);
            return result;
        }
        // check exist
        User tempUser = userMapper.selectById(userId);
        if (tempUser == null) {
            logger.error("User does not exist, userId:{}.", userId);
            putMsg(result, Status.USER_NOT_EXIST, userId);
            return result;
        }

        projectUserMapper.deleteProjectRelation(0, userId);

        if (check(result, StringUtils.isEmpty(projectIds), Status.SUCCESS)) {
            logger.warn("Parameter projectIds is empty.");
            return result;
        }
        Arrays.stream(projectIds.split(",")).distinct().forEach(projectId -> {
            Date now = new Date();
            ProjectUser projectUser = new ProjectUser();
            projectUser.setUserId(userId);
            projectUser.setProjectId(Integer.parseInt(projectId));
            projectUser.setPerm(Constants.AUTHORIZE_WRITABLE_PERM);
            projectUser.setCreateTime(now);
            projectUser.setUpdateTime(now);
            projectUserMapper.insert(projectUser);
        });
        putMsg(result, Status.SUCCESS);

        return result;
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
    public Map<String, Object> grantProjectByCode(final User loginUser, final int userId, final long projectCode) {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, false);

        if (resourcePermissionCheckService.functionDisabled()) {
            putMsg(result, Status.FUNCTION_DISABLED);
            return result;
        }

        // 1. check if user is existed
        User tempUser = this.userMapper.selectById(userId);
        if (tempUser == null) {
            logger.error("User does not exist, userId:{}.", userId);
            this.putMsg(result, Status.USER_NOT_EXIST, userId);
            return result;
        }

        // 2. check if project is existed
        Project project = this.projectMapper.queryByCode(projectCode);
        if (project == null) {
            logger.error("Project does not exist, projectCode:{}.", projectCode);
            this.putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);
            return result;
        }

        // 3. only project owner can operate
        if (!this.canOperator(loginUser, project.getUserId())) {
            logger.warn("User does not have permission for project, userId:{}, userName:{}, projectCode:{}.", loginUser.getId(), loginUser.getUserName(), projectCode);
            this.putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        // 4. maintain the relationship between project and user if not exists
        ProjectUser projectUser = projectUserMapper.queryProjectRelation(project.getId(), userId);
        if (projectUser == null) {
            Date today = new Date();
            projectUser = new ProjectUser();
            projectUser.setUserId(userId);
            projectUser.setProjectId(project.getId());
            projectUser.setPerm(7);
            projectUser.setCreateTime(today);
            projectUser.setUpdateTime(today);
            this.projectUserMapper.insert(projectUser);
        }
        logger.info("User is granted permission for projects, userId:{}, projectCode:{}.", userId, projectCode);
        this.putMsg(result, Status.SUCCESS);
        return result;
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
    public Map<String, Object> revokeProject(User loginUser, int userId, long projectCode) {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, false);

        if (resourcePermissionCheckService.functionDisabled()) {
            putMsg(result, Status.FUNCTION_DISABLED);
            return result;
        }
        // 1. only admin can operate
        if (this.check(result, !this.isAdmin(loginUser), Status.USER_NO_OPERATION_PERM)) {
            logger.warn("Only admin can revoke the project permission.");
            return result;
        }

        // 2. check if user is existed
        User user = this.userMapper.selectById(userId);
        if (user == null) {
            logger.error("User does not exist, userId:{}.", userId);
            this.putMsg(result, Status.USER_NOT_EXIST, userId);
            return result;
        }

        // 3. check if project is existed
        Project project = this.projectMapper.queryByCode(projectCode);
        if (project == null) {
            logger.error("Project does not exist, projectCode:{}.", projectCode);
            this.putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);
            return result;
        }

        // 4. delete th relationship between project and user
        this.projectUserMapper.deleteProjectRelation(project.getId(), user.getId());
        logger.info("User is revoked permission for projects, userId:{}, projectCode:{}.", userId, projectCode);
        this.putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * grant resource
     *
     * @param loginUser   login user
     * @param userId      user id
     * @param resourceIds resource id array
     * @return grant result code
     */
    @Override
    @Transactional
    public Map<String, Object> grantResources(User loginUser, int userId, String resourceIds) {
        Map<String, Object> result = new HashMap<>();

        if (resourcePermissionCheckService.functionDisabled()) {
            putMsg(result, Status.FUNCTION_DISABLED);
            return result;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            logger.error("User does not exist, userId:{}.", userId);
            putMsg(result, Status.USER_NOT_EXIST, userId);
            return result;
        }

        Set<Integer> needAuthorizeResIds = new HashSet<>();
        if (StringUtils.isNotBlank(resourceIds)) {
            String[] resourceFullIdArr = resourceIds.split(",");
            // need authorize resource id set
            for (String resourceFullId : resourceFullIdArr) {
                String[] resourceIdArr = resourceFullId.split("-");
                for (int i = 0; i <= resourceIdArr.length - 1; i++) {
                    int resourceIdValue = Integer.parseInt(resourceIdArr[i]);
                    needAuthorizeResIds.add(resourceIdValue);
                }
            }
        }

        // get the authorized resource id list by user id
        List<Integer> resIds =
                resourceUserMapper.queryResourcesIdListByUserIdAndPerm(userId, Constants.AUTHORIZE_WRITABLE_PERM);
        List<Resource> oldAuthorizedRes =
                CollectionUtils.isEmpty(resIds) ? new ArrayList<>() : resourceMapper.queryResourceListById(resIds);
        // if resource type is UDF,need check whether it is bound by UDF function
        Set<Integer> oldAuthorizedResIds = oldAuthorizedRes.stream().map(Resource::getId).collect(Collectors.toSet());

        // get the unauthorized resource id list
        oldAuthorizedResIds.removeAll(needAuthorizeResIds);

        if (CollectionUtils.isNotEmpty(oldAuthorizedResIds)) {

            // get all resource id of process definitions those are released
            List<Map<String, Object>> list = processDefinitionMapper.listResourcesByUser(userId);
            Map<Integer, Set<Long>> resourceProcessMap =
                    ResourceProcessDefinitionUtils.getResourceProcessDefinitionMap(list);
            Set<Integer> resourceIdSet = resourceProcessMap.keySet();

            resourceIdSet.retainAll(oldAuthorizedResIds);
            if (CollectionUtils.isNotEmpty(resourceIdSet)) {
                for (Integer resId : resourceIdSet) {
                    logger.error("Resource id:{} is used by process definition {}", resId,
                            resourceProcessMap.get(resId));
                }
                putMsg(result, Status.RESOURCE_IS_USED);
                return result;
            }

        }

        resourceUserMapper.deleteResourceUser(userId, 0);

        if (check(result, StringUtils.isEmpty(resourceIds), Status.SUCCESS)) {
            logger.warn("Parameter resourceIds is empty.");
            return result;
        }

        for (int resourceIdValue : needAuthorizeResIds) {
            Resource resource = resourceMapper.selectById(resourceIdValue);
            if (resource == null) {
                logger.error("Resource does not exist, resourceId:{}.", resourceIdValue);
                putMsg(result, Status.RESOURCE_NOT_EXIST);
                return result;
            }

            Date now = new Date();
            ResourcesUser resourcesUser = new ResourcesUser();
            resourcesUser.setUserId(userId);
            resourcesUser.setResourcesId(resourceIdValue);
            if (resource.isDirectory()) {
                resourcesUser.setPerm(Constants.AUTHORIZE_READABLE_PERM);
            } else {
                resourcesUser.setPerm(Constants.AUTHORIZE_WRITABLE_PERM);
            }

            resourcesUser.setCreateTime(now);
            resourcesUser.setUpdateTime(now);
            resourceUserMapper.insert(resourcesUser);

        }

        logger.info("User is granted permission for resources, userId:{}, resourceIds:{}.", user.getId(), needAuthorizeResIds);

        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * grant udf function
     *
     * @param loginUser login user
     * @param userId    user id
     * @param udfIds    udf id array
     * @return grant result code
     */
    @Override
    @Transactional
    public Map<String, Object> grantUDFFunction(User loginUser, int userId, String udfIds) {
        Map<String, Object> result = new HashMap<>();

        if (resourcePermissionCheckService.functionDisabled()) {
            putMsg(result, Status.FUNCTION_DISABLED);
            return result;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            logger.error("User does not exist, userId:{}.", userId);
            putMsg(result, Status.USER_NOT_EXIST, userId);
            return result;
        }

        udfUserMapper.deleteByUserId(userId);

        if (check(result, StringUtils.isEmpty(udfIds), Status.SUCCESS)) {
            logger.warn("Parameter udfIds is empty.");
            return result;
        }

        String[] resourcesIdArr = udfIds.split(",");

        for (String udfId : resourcesIdArr) {
            Date now = new Date();
            UDFUser udfUser = new UDFUser();
            udfUser.setUserId(userId);
            udfUser.setUdfId(Integer.parseInt(udfId));
            udfUser.setPerm(Constants.AUTHORIZE_WRITABLE_PERM);
            udfUser.setCreateTime(now);
            udfUser.setUpdateTime(now);
            udfUserMapper.insert(udfUser);
        }

        logger.info("User is granted permission for UDF, userName:{}.", user.getUserName());

        putMsg(result, Status.SUCCESS);

        return result;
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
    public Map<String, Object> grantNamespaces(User loginUser, int userId, String namespaceIds) {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, false);
        if (resourcePermissionCheckService.functionDisabled()) {
            putMsg(result, Status.FUNCTION_DISABLED);
            return result;
        }
        // only admin can operate
        if (this.check(result, !this.isAdmin(loginUser), Status.USER_NO_OPERATION_PERM)) {
            logger.warn("Only admin can grant namespaces.");
            return result;
        }

        // check exist
        User tempUser = userMapper.selectById(userId);
        if (tempUser == null) {
            logger.error("User does not exist, userId:{}.", userId);
            putMsg(result, Status.USER_NOT_EXIST, userId);
            return result;
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

        logger.info("User is granted permission for namespace, userId:{}.", tempUser.getId());

        putMsg(result, Status.SUCCESS);

        return result;
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
    public Map<String, Object> grantDataSource(User loginUser, int userId, String datasourceIds) {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, false);

        if (resourcePermissionCheckService.functionDisabled()) {
            putMsg(result, Status.FUNCTION_DISABLED);
            return result;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            putMsg(result, Status.USER_NOT_EXIST, userId);
            return result;
        }

        datasourceUserMapper.deleteByUserId(userId);

        if (check(result, StringUtils.isEmpty(datasourceIds), Status.SUCCESS)) {
            return result;
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
            datasourceUserMapper.insert(datasourceUser);
        }

        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * query user info
     *
     * @param loginUser login user
     * @return user info
     */
    @Override
    public Map<String, Object> getUserInfo(User loginUser) {

        Map<String, Object> result = new HashMap<>();

        if (resourcePermissionCheckService.functionDisabled()) {
            putMsg(result, Status.FUNCTION_DISABLED);
            return result;
        }
        User user = null;
        if (loginUser.getUserType() == UserType.ADMIN_USER) {
            user = loginUser;
        } else {
            user = userMapper.queryDetailsById(loginUser.getId());

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

        // add system default timezone if not user timezone
        if (StringUtils.isEmpty(user.getTimeZone())) {
            user.setTimeZone(TimeZone.getDefault().toZoneId().getId());
        }

        result.put(Constants.DATA_LIST, user);

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query user list
     *
     * @param loginUser login user
     * @return user list
     */
    @Override
    public Map<String, Object> queryAllGeneralUsers(User loginUser) {
        Map<String, Object> result = new HashMap<>();
        if (resourcePermissionCheckService.functionDisabled()) {
            putMsg(result, Status.FUNCTION_DISABLED);
            return result;
        }
        // only admin can operate
        if (check(result, !isAdmin(loginUser), Status.USER_NO_OPERATION_PERM)) {
            logger.warn("Only admin can query all general users.");
            return result;
        }

        List<User> userList = userMapper.queryAllGeneralUser();
        result.put(Constants.DATA_LIST, userList);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * query user list
     *
     * @param loginUser login user
     * @return user list
     */
    @Override
    public Map<String, Object> queryUserList(User loginUser) {
        Map<String, Object> result = new HashMap<>();
        // only admin can operate
        if (!canOperatorPermissions(loginUser, null, AuthorizationType.ACCESS_TOKEN, USER_MANAGER)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        List<User> userList = userMapper.queryEnabledUsers();
        result.put(Constants.DATA_LIST, userList);
        putMsg(result, Status.SUCCESS);

        return result;
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
        User user = userMapper.queryByUserNameAccurately(userName);
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
    public Map<String, Object> unauthorizedUser(User loginUser, Integer alertgroupId) {

        Map<String, Object> result = new HashMap<>();
        if (resourcePermissionCheckService.functionDisabled()) {
            putMsg(result, Status.FUNCTION_DISABLED);
            return result;
        }
        // only admin can operate
        if (check(result, !isAdmin(loginUser), Status.USER_NO_OPERATION_PERM)) {
            logger.warn("Only admin can deauthorize user.");
            return result;
        }

        List<User> userList = userMapper.selectList(null);
        List<User> resultUsers = new ArrayList<>();
        Set<User> userSet = null;
        if (userList != null && !userList.isEmpty()) {
            userSet = new HashSet<>(userList);

            List<User> authedUserList = userMapper.queryUserListByAlertGroupId(alertgroupId);

            Set<User> authedUserSet = null;
            if (authedUserList != null && !authedUserList.isEmpty()) {
                authedUserSet = new HashSet<>(authedUserList);
                userSet.removeAll(authedUserSet);
            }
            resultUsers = new ArrayList<>(userSet);
        }
        result.put(Constants.DATA_LIST, resultUsers);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * authorized user
     *
     * @param loginUser    login user
     * @param alertGroupId alert group id
     * @return authorized result code
     */
    @Override
    public Map<String, Object> authorizedUser(User loginUser, Integer alertGroupId) {
        Map<String, Object> result = new HashMap<>();
        if (resourcePermissionCheckService.functionDisabled()) {
            putMsg(result, Status.FUNCTION_DISABLED);
            return result;
        }
        // only admin can operate
        if (check(result, !isAdmin(loginUser), Status.USER_NO_OPERATION_PERM)) {
            logger.warn("Only admin can authorize user.");
            return result;
        }
        List<User> userList = userMapper.queryUserListByAlertGroupId(alertGroupId);
        result.put(Constants.DATA_LIST, userList);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * @param tenantId tenant id
     * @return true if tenant exists, otherwise return false
     */
    private boolean checkTenantExists(int tenantId) {
        return tenantMapper.queryById(tenantId) != null;
    }

    /**
     * @return if check failed return the field, otherwise return null
     */
    private String checkUserParams(String userName, String password, String email, String phone) {

        String msg = null;
        if (!CheckUtils.checkUserName(userName)) {
            logger.warn("Parameter userName check failed.");
            msg = userName;
        } else if (!CheckUtils.checkPassword(password)) {
            logger.warn("Parameter password check failed.");
            msg = password;
        } else if (!CheckUtils.checkEmail(email)) {
            logger.warn("Parameter email check failed.");
            msg = email;
        } else if (!CheckUtils.checkPhone(phone)) {
            logger.warn("Parameter phone check failed.");
            msg = phone;
        }

        return msg;
    }

    /**
     * copy resource files
     * xxx unchecked
     *
     * @param resourceComponent resource component
     * @param srcBasePath       src base path
     * @param dstBasePath       dst base path
     * @throws IOException io exception
     */
    private void copyResourceFiles(String oldTenantCode, String newTenantCode, ResourceComponent resourceComponent,
                                   String srcBasePath, String dstBasePath) {
        List<ResourceComponent> components = resourceComponent.getChildren();

        try {
            if (CollectionUtils.isNotEmpty(components)) {
                for (ResourceComponent component : components) {
                    // verify whether exist
                    if (!storageOperate.exists(oldTenantCode,
                            String.format(Constants.FORMAT_S_S, srcBasePath, component.getFullName()))) {
                        logger.error("Resource file: {} does not exist, copy error.", component.getFullName());
                        throw new ServiceException(Status.RESOURCE_NOT_EXIST);
                    }

                    if (!component.isDirctory()) {
                        // copy it to dst
                        storageOperate.copy(String.format(Constants.FORMAT_S_S, srcBasePath, component.getFullName()),
                                String.format(Constants.FORMAT_S_S, dstBasePath, component.getFullName()), false, true);
                        continue;
                    }

                    if (CollectionUtils.isEmpty(component.getChildren())) {
                        // if not exist,need create it
                        if (!storageOperate.exists(oldTenantCode,
                                String.format(Constants.FORMAT_S_S, dstBasePath, component.getFullName()))) {
                            storageOperate.mkdir(newTenantCode,
                                    String.format(Constants.FORMAT_S_S, dstBasePath, component.getFullName()));
                        }
                    } else {
                        copyResourceFiles(oldTenantCode, newTenantCode, component, srcBasePath, dstBasePath);
                    }
                }

            }
        } catch (IOException e) {
            logger.error("copy the resources failed,the error message  is {}", e.getMessage());
        }
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
    public Map<String, Object> registerUser(String userName, String userPassword, String repeatPassword, String email) {
        Map<String, Object> result = new HashMap<>();

        // check user params
        String msg = this.checkUserParams(userName, userPassword, email, "");
        if (resourcePermissionCheckService.functionDisabled()) {
            putMsg(result, Status.FUNCTION_DISABLED);
            return result;
        }
        if (!StringUtils.isEmpty(msg)) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, msg);
            return result;
        }

        if (!userPassword.equals(repeatPassword)) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, "two passwords are not same");
            return result;
        }
        User user = createUser(userName, userPassword, email, 1, "", "", Flag.NO.ordinal());
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, user);
        return result;
    }

    /**
     * activate user, only system admin have permission, change user state code 0 to 1
     *
     * @param loginUser login user
     * @param userName  user name
     * @return create result code
     */
    @Override
    public Map<String, Object> activateUser(User loginUser, String userName) {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, false);
        if (resourcePermissionCheckService.functionDisabled()) {
            putMsg(result, Status.FUNCTION_DISABLED);
            return result;
        }
        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        if (!CheckUtils.checkUserName(userName)) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, userName);
            return result;
        }

        User user = userMapper.queryByUserNameAccurately(userName);

        if (user == null) {
            putMsg(result, Status.USER_NOT_EXIST, userName);
            return result;
        }

        if (user.getState() != Flag.NO.ordinal()) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, userName);
            return result;
        }

        user.setState(Flag.YES.ordinal());
        Date now = new Date();
        user.setUpdateTime(now);
        userMapper.updateById(user);

        User responseUser = userMapper.queryByUserNameAccurately(userName);
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, responseUser);
        return result;
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
        Map<String, Object> result = new HashMap<>();

        if (resourcePermissionCheckService.functionDisabled()) {
            putMsg(result, Status.FUNCTION_DISABLED);
            return result;
        }
        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        int totalSuccess = 0;
        List<String> successUserNames = new ArrayList<>();
        Map<String, Object> successRes = new HashMap<>();
        int totalFailed = 0;
        List<Map<String, String>> failedInfo = new ArrayList<>();
        Map<String, Object> failedRes = new HashMap<>();
        for (String userName : userNames) {
            Map<String, Object> tmpResult = activateUser(loginUser, userName);
            if (tmpResult.get(Constants.STATUS) != Status.SUCCESS) {
                totalFailed++;
                Map<String, String> failedBody = new HashMap<>();
                failedBody.put("userName", userName);
                Status status = (Status) tmpResult.get(Constants.STATUS);
                String errorMessage = MessageFormat.format(status.getMsg(), userName);
                failedBody.put("msg", errorMessage);
                failedInfo.add(failedBody);
            } else {
                totalSuccess++;
                successUserNames.add(userName);
            }
        }
        successRes.put("sum", totalSuccess);
        successRes.put("userName", successUserNames);
        failedRes.put("sum", totalFailed);
        failedRes.put("info", failedInfo);
        Map<String, Object> res = new HashMap<>();
        res.put("success", successRes);
        res.put("failed", failedRes);
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, res);
        return result;
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
    public User createUserIfNotExists(String userName, String userPassword, String email, String phone,
                                      String tenantCode,
                                      String queue,
                                      int state) throws IOException {
        User user = userMapper.queryByUserNameAccurately(userName);
        if (Objects.isNull(user)) {
            Tenant tenant = tenantMapper.queryByTenantCode(tenantCode);
            user = createUser(userName, userPassword, email, tenant.getId(), phone, queue, state);
            return user;
        }

        updateUser(user, user.getId(), userName, userPassword, email, user.getTenantId(), phone, queue, state, null);
        user = userMapper.queryDetailsById(user.getId());
        return user;
    }
}
