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
import cn.escheduler.common.utils.CollectionUtils;
import cn.escheduler.common.utils.EncryptionUtils;
import cn.escheduler.common.utils.HadoopUtils;
import cn.escheduler.common.utils.PropertyUtils;
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
public class UsersService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(UsersService.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private ProjectUserMapper projectUserMapper;

    @Autowired
    private ResourcesUserMapper resourcesUserMapper;

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private DatasourceUserMapper datasourceUserMapper;

    @Autowired
    private UDFUserMapper udfUserMapper;

    @Autowired
    private AlertGroupMapper alertGroupMapper;


    /**
     * create user, only system admin have permission
     *
     * @param loginUser
     * @param userName
     * @param userPassword
     * @param email
     * @param tenantId
     * @param phone
     * @return
     */
    @Transactional(value = "TransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> createUser(User loginUser,
                                          String userName,
                                          String userPassword,
                                          String email,
                                          int tenantId,
                                          String phone,
                                          String queue) throws Exception {

        Map<String, Object> result = new HashMap<>(5);
        result = CheckUtils.checkUserParams(userName, userPassword, email, phone);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        if (check(result, !isAdmin(loginUser), Status.USER_NO_OPERATION_PERM, Constants.STATUS)) {
            return result;
        }

        if (check(result, checkTenant(tenantId), Status.TENANT_NOT_EXIST, Constants.STATUS)) {
            return result;
        }

        User user = new User();
        Date now = new Date();

        user.setUserName(userName);
        user.setUserPassword(EncryptionUtils.getMd5(userPassword));
        user.setEmail(email);
        user.setTenantId(tenantId);
        user.setPhone(phone);
        // create general users, administrator users are currently built-in
        user.setUserType(UserType.GENERAL_USER);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setQueue(queue);

        // save user
        userMapper.insert(user);

        Tenant tenant = tenantMapper.queryById(tenantId);
        // if hdfs startup
        if (PropertyUtils.getBoolean(cn.escheduler.common.Constants.HDFS_STARTUP_STATE)){
            String userPath = HadoopUtils.getHdfsDataBasePath() + "/" + tenant.getTenantCode() + "/home/" + user.getId();

            HadoopUtils.getInstance().mkdir(userPath);
        }

        putMsg(result, Status.SUCCESS);
        return result;

    }

    /**
     * query user
     *
     * @param name
     * @param password
     * @return
     */
    public User queryUser(String name, String password) {
        String md5 = EncryptionUtils.getMd5(password);
        return userMapper.queryForCheck(name, md5);
    }

    /**
     * check general user or not
     *
     * @param user
     * @return
     */
    public boolean isGeneral(User user) {
        return user.getUserType() == UserType.GENERAL_USER;
    }

    /**
     * query user list
     *
     * @param loginUser
     * @param searchVal
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Map<String, Object> queryUserList(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        Map<String, Object> result = new HashMap<>(5);

        if (check(result, !isAdmin(loginUser), Status.USER_NO_OPERATION_PERM, Constants.STATUS)) {
            return result;
        }

        Integer count = userMapper.countUserPaging(searchVal);

        PageInfo<User> pageInfo = new PageInfo<>(pageNo, pageSize);

        List<User> scheduleList = userMapper.queryUserPaging(searchVal, pageInfo.getStart(), pageSize);

        pageInfo.setTotalCount(count);
        pageInfo.setLists(scheduleList);
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * updateProcessInstance user
     *
     * @param userId
     * @param userName
     * @param userPassword
     * @param email
     * @param tenantId
     * @param phone
     * @return
     */
    public Map<String, Object> updateUser(int userId,
                                          String userName,
                                          String userPassword,
                                          String email,
                                          int tenantId,
                                          String phone,
                                          String queue) throws Exception {
        Map<String, Object> result = new HashMap<>(5);
        result.put(Constants.STATUS, false);

        User user = userMapper.queryById(userId);

        if (user == null) {
            putMsg(result, Status.USER_NOT_EXIST, userId);
            return result;
        }

        Date now = new Date();

        if (StringUtils.isNotEmpty(userName)) {
            User tempUser = userMapper.queryByUserName(userName);
            if (tempUser != null && tempUser.getId() != userId) {
                putMsg(result, Status.USER_NAME_EXIST);
                return result;
            }
            user.setUserName(userName);
        }

        if (StringUtils.isNotEmpty(userPassword)) {
            user.setUserPassword(EncryptionUtils.getMd5(userPassword));
        }

        if (StringUtils.isNotEmpty(email)) {
            user.setEmail(email);
        }
        user.setQueue(queue);
        user.setPhone(phone);
        user.setUpdateTime(now);

        //if user switches the tenant, the user's resources need to be copied to the new tenant
        if (user.getTenantId() != tenantId) {
            Tenant oldTenant = tenantMapper.queryById(user.getTenantId());
            //query tenant
            Tenant newTenant = tenantMapper.queryById(tenantId);
            if (newTenant != null) {
                // if hdfs startup
                if (PropertyUtils.getBoolean(cn.escheduler.common.Constants.HDFS_STARTUP_STATE)){
                    String newTenantCode = newTenant.getTenantCode();
                    String oldResourcePath = HadoopUtils.getHdfsDataBasePath() + "/" + oldTenant.getTenantCode() + "/resources";
                    String oldUdfsPath = HadoopUtils.getHdfsUdfDir(oldTenant.getTenantCode());


                    String newResourcePath = HadoopUtils.getHdfsDataBasePath() + "/" + newTenantCode + "/resources";
                    String newUdfsPath = HadoopUtils.getHdfsUdfDir(newTenantCode);

                    //file resources list
                    List<Resource> fileResourcesList = resourceMapper.queryResourceCreatedByUser(userId, 0);
                    if (CollectionUtils.isNotEmpty(fileResourcesList)) {
                        for (Resource resource : fileResourcesList) {
                            HadoopUtils.getInstance().copy(oldResourcePath + "/" + resource.getAlias(), newResourcePath, false, true);
                        }
                    }

                    //udf resources
                    List<Resource> udfResourceList = resourceMapper.queryResourceCreatedByUser(userId, 1);
                    if (CollectionUtils.isNotEmpty(udfResourceList)) {
                        for (Resource resource : udfResourceList) {
                            HadoopUtils.getInstance().copy(oldUdfsPath + "/" + resource.getAlias(), newUdfsPath, false, true);
                        }
                    }

                    //Delete the user from the old tenant directory
                    String oldUserPath = HadoopUtils.getHdfsDataBasePath() + "/" + oldTenant.getTenantCode() + "/home/" + userId;
                    HadoopUtils.getInstance().delete(oldUserPath, true);


                    //create user in the new tenant directory
                    String newUserPath = HadoopUtils.getHdfsDataBasePath() + "/" + newTenant.getTenantCode() + "/home/" + user.getId();
                    HadoopUtils.getInstance().mkdir(newUserPath);
                }
            }
            user.setTenantId(tenantId);
        }

        // updateProcessInstance user
        userMapper.update(user);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * delete user
     *
     * @param loginUser
     * @param id
     * @return
     */
    public Map<String, Object> deleteUserById(User loginUser, int id) throws Exception {
        Map<String, Object> result = new HashMap<>(5);
        //only admin can operate
        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NOT_EXIST, id);
            return result;
        }

        // delete user
        User user = userMapper.queryTenantCodeByUserId(id);


        if (PropertyUtils.getBoolean(cn.escheduler.common.Constants.HDFS_STARTUP_STATE)){
            String userPath = HadoopUtils.getHdfsDataBasePath() + "/" + user.getTenantCode() + "/home/" + id;

            HadoopUtils.getInstance().delete(userPath, true);
        }

        userMapper.delete(id);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * grant project
     *
     * @param loginUser
     * @param userId
     * @param projectIds
     * @return
     */
    public Map<String, Object> grantProject(User loginUser, int userId, String projectIds) {
        Map<String, Object> result = new HashMap<>(5);
        result.put(Constants.STATUS, false);

        //only admin can operate
        if (check(result, !isAdmin(loginUser), Status.USER_NO_OPERATION_PERM, Constants.STATUS)) {
            return result;
        }

        //if the selected projectIds are empty, delete all items associated with the user
        projectUserMapper.deleteByUserId(userId);

        if (check(result, StringUtils.isEmpty(projectIds), Status.SUCCESS, Constants.MSG)) {
            return result;
        }

        String[] projectIdArr = projectIds.split(",");

        for (String projectId : projectIdArr) {
            Date now = new Date();
            ProjectUser projectUser = new ProjectUser();
            projectUser.setUserId(userId);
            projectUser.setProjectId(Integer.parseInt(projectId));
            projectUser.setPerm(7);
            projectUser.setCreateTime(now);
            projectUser.setUpdateTime(now);
            projectUserMapper.insert(projectUser);
        }

        putMsg(result, Status.SUCCESS);

        return result;
    }


    /**
     * grant resource
     *
     * @param loginUser
     * @param userId
     * @param resourceIds
     * @return
     */
    public Map<String, Object> grantResources(User loginUser, int userId, String resourceIds) {
        Map<String, Object> result = new HashMap<>(5);
        //only admin can operate
        if (check(result, !isAdmin(loginUser), Status.USER_NO_OPERATION_PERM, Constants.STATUS)) {
            return result;
        }

        resourcesUserMapper.deleteByUserId(userId);

        if (check(result, StringUtils.isEmpty(resourceIds), Status.SUCCESS, Constants.MSG)) {
            return result;
        }

        String[] resourcesIdArr = resourceIds.split(",");

        for (String resourceId : resourcesIdArr) {
            Date now = new Date();
            ResourcesUser resourcesUser = new ResourcesUser();
            resourcesUser.setUserId(userId);
            resourcesUser.setResourcesId(Integer.parseInt(resourceId));
            resourcesUser.setPerm(7);
            resourcesUser.setCreateTime(now);
            resourcesUser.setUpdateTime(now);
            resourcesUserMapper.insert(resourcesUser);
        }

        putMsg(result, Status.SUCCESS);

        return result;
    }


    /**
     * grant udf function
     *
     * @param loginUser
     * @param userId
     * @param udfIds
     * @return
     */
    public Map<String, Object> grantUDFFunction(User loginUser, int userId, String udfIds) {
        Map<String, Object> result = new HashMap<>(5);

        //only admin can operate
        if (check(result, !isAdmin(loginUser), Status.USER_NO_OPERATION_PERM, Constants.STATUS)) {
            return result;
        }

        udfUserMapper.deleteByUserId(userId);

        if (check(result, StringUtils.isEmpty(udfIds), Status.SUCCESS, Constants.MSG)) {
            return result;
        }

        String[] resourcesIdArr = udfIds.split(",");

        for (String udfId : resourcesIdArr) {
            Date now = new Date();
            UDFUser udfUser = new UDFUser();
            udfUser.setUserId(userId);
            udfUser.setUdfId(Integer.parseInt(udfId));
            udfUser.setPerm(7);
            udfUser.setCreateTime(now);
            udfUser.setUpdateTime(now);
            udfUserMapper.insert(udfUser);
        }

        putMsg(result, Status.SUCCESS);

        return result;
    }


    /**
     * grant datasource
     *
     * @param loginUser
     * @param userId
     * @param datasourceIds
     * @return
     */
    public Map<String, Object> grantDataSource(User loginUser, int userId, String datasourceIds) {
        Map<String, Object> result = new HashMap<>(5);
        result.put(Constants.STATUS, false);

        //only admin can operate
        if (check(result, !isAdmin(loginUser), Status.USER_NO_OPERATION_PERM, Constants.STATUS)) {
            return result;
        }

        datasourceUserMapper.deleteByUserId(userId);

        if (check(result, StringUtils.isEmpty(datasourceIds), Status.SUCCESS, Constants.MSG)) {
            return result;
        }

        String[] datasourceIdArr = datasourceIds.split(",");

        for (String datasourceId : datasourceIdArr) {
            Date now = new Date();

            DatasourceUser datasourceUser = new DatasourceUser();
            datasourceUser.setUserId(userId);
            datasourceUser.setDatasourceId(Integer.parseInt(datasourceId));
            datasourceUser.setPerm(7);
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
     * @param loginUser
     * @return
     */
    public Map<String, Object> getUserInfo(User loginUser) {

        Map<String, Object> result = new HashMap<>();

        User user = null;
        if (loginUser.getUserType() == UserType.ADMIN_USER) {
            user = loginUser;
        } else {
            user = userMapper.queryDetailsById(loginUser.getId());

            List<AlertGroup> alertGroups = alertGroupMapper.queryByUserId(loginUser.getId());

            StringBuilder sb = new StringBuilder();

            if (alertGroups != null && alertGroups.size() > 0) {
                for (int i = 0; i < alertGroups.size() - 1; i++) {
                    sb.append(alertGroups.get(i).getGroupName() + ",");
                }
                sb.append(alertGroups.get(alertGroups.size() - 1));
                user.setAlertGroup(sb.toString());
            }
        }

        result.put(Constants.DATA_LIST, user);

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query user list
     *
     * @param loginUser
     * @return
     */
    public Map<String, Object> queryUserList(User loginUser) {
        Map<String, Object> result = new HashMap<>(5);
        //only admin can operate
        if (check(result, !isAdmin(loginUser), Status.USER_NO_OPERATION_PERM, Constants.STATUS)) {
            return result;
        }

        List<User> userList = userMapper.queryAllUsers();
        result.put(Constants.DATA_LIST, userList);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * verify user name exists
     *
     * @param userName
     * @return
     */
    public Result verifyUserName(String userName) {

        cn.escheduler.api.utils.Result result = new cn.escheduler.api.utils.Result();
        User user = userMapper.queryByUserName(userName);
        if (user != null) {
            logger.error("user {} has exist, can't create again.", userName);

            putMsg(result, Status.USER_NAME_EXIST);
        } else {
            putMsg(result, Status.SUCCESS);
        }

        return result;
    }


    /**
     * unauthorized user
     *
     * @param loginUser
     * @param alertgroupId
     * @return
     */
    public Map<String, Object> unauthorizedUser(User loginUser, Integer alertgroupId) {

        Map<String, Object> result = new HashMap<>(5);
        //only admin can operate
        if (check(result, !isAdmin(loginUser), Status.USER_NO_OPERATION_PERM, Constants.STATUS)) {
            return result;
        }

        List<User> userList = userMapper.queryAllUsers();
        List<User> resultUsers = new ArrayList<>();
        Set<User> userSet = null;
        if (userList != null && userList.size() > 0) {
            userSet = new HashSet<>(userList);

            List<User> authedUserList = userMapper.queryUserListByAlertGroupId(alertgroupId);

            Set<User> authedUserSet = null;
            if (authedUserList != null && authedUserList.size() > 0) {
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
     * @param loginUser
     * @param alertgroupId
     * @return
     */
    public Map<String, Object> authorizedUser(User loginUser, Integer alertgroupId) {
        Map<String, Object> result = new HashMap<>(5);
        //only admin can operate
        if (check(result, !isAdmin(loginUser), Status.USER_NO_OPERATION_PERM, Constants.STATUS)) {
            return result;
        }
        List<User> userList = userMapper.queryUserListByAlertGroupId(alertgroupId);
        result.put(Constants.DATA_LIST, userList);
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
     * @param tenantId
     * @return
     */
    private boolean checkTenant(int tenantId) {
        return tenantMapper.queryById(tenantId) == null ? true : false;
    }
}
