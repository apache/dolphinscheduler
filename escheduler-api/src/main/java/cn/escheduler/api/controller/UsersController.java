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
package cn.escheduler.api.controller;


import cn.escheduler.api.enums.Status;
import cn.escheduler.api.service.UsersService;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.Result;
import cn.escheduler.dao.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static cn.escheduler.api.enums.Status.*;


/**
 * user controller
 */
@RestController
@RequestMapping("/users")
public class UsersController extends BaseController{


    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);


    @Autowired
    private UsersService usersService;

    /**
     * create user
     * 
     * @param loginUser
     * @param userName
     * @param userPassword
     * @param email
     * @param tenantId
     * @param phone
     * @return
     */
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Result createUser(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                     @RequestParam(value = "userName") String userName,
                                                     @RequestParam(value = "userPassword") String userPassword,
                                                     @RequestParam(value = "tenantId") int tenantId,
                                                     @RequestParam(value = "queue") String queue,
                                                     @RequestParam(value = "email") String email,
                                                     @RequestParam(value = "phone", required = false) String phone) {
        logger.info("login user {}, create user, userName: {}, email: {}, tenantId: {}, userPassword: {}, phone: {}, user queue: {}",
                loginUser.getUserName(), userName, email, tenantId, Constants.PASSWORD_DEFAULT, phone,queue);

        try {
            Map<String, Object> result = usersService.createUser(loginUser, userName, userPassword,email,tenantId, phone,queue);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(CREATE_USER_ERROR.getMsg(),e);
            return error(CREATE_USER_ERROR.getCode(), CREATE_USER_ERROR.getMsg());
        }
    }

    /**
     * query user list paging
     *
     * @param loginUser
     * @param pageNo
     * @param searchVal
     * @param pageSize
     * @return
     */
    @GetMapping(value="/list-paging")
    @ResponseStatus(HttpStatus.OK)
    public Result queryUserList(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam("pageNo") Integer pageNo,
                                @RequestParam(value = "searchVal", required = false) String searchVal,
                                @RequestParam("pageSize") Integer pageSize){
        logger.info("login user {}, list user paging, pageNo: {}, searchVal: {}, pageSize: {}",
                loginUser.getUserName(),pageNo,searchVal,pageSize);
        try{
            Map<String, Object> result = checkPageParams(pageNo, pageSize);
            if(result.get(Constants.STATUS) != Status.SUCCESS){
                return returnDataListPaging(result);
            }
            result = usersService.queryUserList(loginUser, searchVal, pageNo, pageSize);
            return returnDataListPaging(result);
        }catch (Exception e){
            logger.error(QUERY_USER_LIST_PAGING_ERROR.getMsg(),e);
            return error(Status.QUERY_USER_LIST_PAGING_ERROR.getCode(), Status.QUERY_USER_LIST_PAGING_ERROR.getMsg());
        }
    }


    /**
     * updateProcessInstance user
     *
     * @param loginUser
     * @param id
     * @param userName
     * @param userPassword
     * @param email
     * @param tenantId
     * @param phone
     * @return
     */
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    public Result updateUser(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                     @RequestParam(value = "id") int id,
                                                     @RequestParam(value = "userName") String userName,
                                                     @RequestParam(value = "userPassword") String userPassword,
                                                     @RequestParam(value = "queue") String queue,
                                                     @RequestParam(value = "email") String email,
                                                     @RequestParam(value = "tenantId") int tenantId,
                                                     @RequestParam(value = "phone", required = false) String phone) {
        logger.info("login user {}, updateProcessInstance user, userName: {}, email: {}, tenantId: {}, userPassword: {}, phone: {}, user queue: {}",
                loginUser.getUserName(), userName, email, tenantId, Constants.PASSWORD_DEFAULT, phone,queue);
        try {
            Map<String, Object> result = usersService.updateUser(id,userName,userPassword,email,tenantId,phone,queue);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(UPDATE_USER_ERROR.getMsg(),e);
            return error(Status.UPDATE_USER_ERROR.getCode(), Status.UPDATE_USER_ERROR.getMsg());
        }
    }

    /**
     * delete user by id
     * @param loginUser
     * @param id
     * @return
     */
    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    public Result delUserById(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                      @RequestParam(value = "id") int  id) {
        logger.info("login user {}, delete user, userId: {},", loginUser.getUserName(), id);
        try {
            Map<String, Object> result = usersService.deleteUserById(loginUser, id);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(DELETE_USER_BY_ID_ERROR.getMsg(),e);
            return error(Status.DELETE_USER_BY_ID_ERROR.getCode(), Status.DELETE_USER_BY_ID_ERROR.getMsg());
        }
    }

    /**
     * grant project
     *
     * @param loginUser
     * @param userId
     * @return
     */
    @PostMapping(value = "/grant-project")
    @ResponseStatus(HttpStatus.OK)
    public Result grantProject(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                       @RequestParam(value = "userId") int  userId,
                                                       @RequestParam(value = "projectIds") String projectIds) {
        logger.info("login user {}, grant project, userId: {},projectIds : {}", loginUser.getUserName(), userId,projectIds);
        try {
            Map<String, Object> result = usersService.grantProject(loginUser, userId, projectIds);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(GRANT_PROJECT_ERROR.getMsg(),e);
            return error(Status.GRANT_PROJECT_ERROR.getCode(), Status.GRANT_PROJECT_ERROR.getMsg());
        }
    }

    /**
     * grant resource
     *
     * @param loginUser
     * @param userId
     * @return
     */
    @PostMapping(value = "/grant-file")
    @ResponseStatus(HttpStatus.OK)
    public Result grantResource(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                        @RequestParam(value = "userId") int  userId,
                                                        @RequestParam(value = "resourceIds") String resourceIds) {
        logger.info("login user {}, grant project, userId: {},resourceIds : {}", loginUser.getUserName(), userId,resourceIds);
        try {
            Map<String, Object> result = usersService.grantResources(loginUser, userId, resourceIds);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(GRANT_RESOURCE_ERROR.getMsg(),e);
            return error(Status.GRANT_RESOURCE_ERROR.getCode(), Status.GRANT_RESOURCE_ERROR.getMsg());
        }
    }


    /**
     * grant udf function
     *
     * @param loginUser
     * @param userId
     * @return
     */
    @PostMapping(value = "/grant-udf-func")
    @ResponseStatus(HttpStatus.OK)
    public Result grantUDFFunc(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                       @RequestParam(value = "userId") int  userId,
                                                       @RequestParam(value = "udfIds") String udfIds) {
        logger.info("login user {}, grant project, userId: {},resourceIds : {}", loginUser.getUserName(), userId,udfIds);
        try {
            Map<String, Object> result = usersService.grantUDFFunction(loginUser, userId, udfIds);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(GRANT_UDF_FUNCTION_ERROR.getMsg(),e);
            return error(Status.GRANT_UDF_FUNCTION_ERROR.getCode(), Status.GRANT_UDF_FUNCTION_ERROR.getMsg());
        }
    }



    /**
     * grant datasource
     *
     * @param loginUser
     * @param userId
     * @return
     */
    @PostMapping(value = "/grant-datasource")
    @ResponseStatus(HttpStatus.OK)
    public Result grantDataSource(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                          @RequestParam(value = "userId") int  userId,
                                                          @RequestParam(value = "datasourceIds") String datasourceIds) {
        logger.info("login user {}, grant project, userId: {},projectIds : {}", loginUser.getUserName(),userId,datasourceIds);
        try {
            Map<String, Object> result = usersService.grantDataSource(loginUser, userId, datasourceIds);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(GRANT_DATASOURCE_ERROR.getMsg(),e);
            return error(Status.GRANT_DATASOURCE_ERROR.getCode(), Status.GRANT_DATASOURCE_ERROR.getMsg());
        }
    }


    /**
     * get user info
     *
     * @param loginUser
     * @return
     */
    @GetMapping(value="/get-user-info")
    @ResponseStatus(HttpStatus.OK)
    public Result getUserInfo(@RequestAttribute(value = Constants.SESSION_USER) User loginUser){
        logger.info("login user {},get user info : {}", loginUser.getUserName());
        try{
            Map<String, Object> result = usersService.getUserInfo(loginUser);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(GET_USER_INFO_ERROR.getMsg(),e);
            return error(Status.GET_USER_INFO_ERROR.getCode(), Status.GET_USER_INFO_ERROR.getMsg());
        }
    }

    /**
     * user list no paging
     *
     * @param loginUser
     * @return
     */
    @GetMapping(value="/list")
    @ResponseStatus(HttpStatus.OK)
    public Result listUser(@RequestAttribute(value = Constants.SESSION_USER) User loginUser){
        logger.info("login user {}, user list");
        try{
            Map<String, Object> result = usersService.queryUserList(loginUser);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(USER_LIST_ERROR.getMsg(),e);
            return error(Status.USER_LIST_ERROR.getCode(), Status.USER_LIST_ERROR.getMsg());
        }
    }

    /**
     * verify username
     *
     * @param loginUser
     * @param userName
     * @return
     */
    @GetMapping(value = "/verify-user-name")
    @ResponseStatus(HttpStatus.OK)
    public Result verifyUserName(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                         @RequestParam(value ="userName") String userName
    ) {
        try{

            logger.info("login user {}, verfiy user name: {}",
                    loginUser.getUserName(),userName);
            return usersService.verifyUserName(userName);
        }catch (Exception e){
            logger.error(VERIFY_USERNAME_ERROR.getMsg(),e);
            return error(Status.VERIFY_USERNAME_ERROR.getCode(), Status.VERIFY_USERNAME_ERROR.getMsg());
        }
    }


    /**
     * unauthorized user
     *
     * @param loginUser
     * @param alertgroupId
     * @return
     */
    @GetMapping(value = "/unauth-user")
    @ResponseStatus(HttpStatus.OK)
    public Result unauthorizedUser(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam("alertgroupId") Integer alertgroupId) {
        try{
            logger.info("unauthorized user, login user:{}, alert group id:{}",
                    loginUser.getUserName(), alertgroupId);
            Map<String, Object> result =  usersService.unauthorizedUser(loginUser, alertgroupId);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(UNAUTHORIZED_USER_ERROR.getMsg(),e);
            return error(Status.UNAUTHORIZED_USER_ERROR.getCode(), Status.UNAUTHORIZED_USER_ERROR.getMsg());
        }
    }


    /**
     * authorized user
     *
     * @param loginUser
     * @param alertgroupId
     * @return
     */
    @GetMapping(value = "/authed-user")
    @ResponseStatus(HttpStatus.OK)
    public Result authorizedUser(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam("alertgroupId") Integer alertgroupId) {
        try{
            logger.info("authorized user , login user:{}, alert group id:{}",
                    loginUser.getUserName(), alertgroupId);
            Map<String, Object> result = usersService.authorizedUser(loginUser, alertgroupId);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(AUTHORIZED_USER_ERROR.getMsg(),e);
            return error(Status.AUTHORIZED_USER_ERROR.getCode(), Status.AUTHORIZED_USER_ERROR.getMsg());
        }
    }


}
