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

package org.apache.dolphinscheduler.api.controller;

import static org.apache.dolphinscheduler.api.enums.Status.AUTHORIZED_USER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.CREATE_USER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_USER_BY_ID_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GET_USER_INFO_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GRANT_DATASOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GRANT_K8S_NAMESPACE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GRANT_PROJECT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_USER_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.REVOKE_PROJECT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UNAUTHORIZED_USER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_USER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.USER_LIST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_USERNAME_ERROR;

import org.apache.dolphinscheduler.api.audit.OperatorLog;
import org.apache.dolphinscheduler.api.audit.enums.AuditType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "USERS_TAG")
@RestController
@RequestMapping("/users")
@Slf4j
public class UsersController extends BaseController {

    @Autowired
    private UsersService usersService;

    @Operation(summary = "createUser", description = "CREATE_USER_NOTES")
    @Parameters({
            @Parameter(name = "userName", description = "USER_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "userPassword", description = "USER_PASSWORD", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "tenantId", description = "TENANT_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "queue", description = "QUEUE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "email", description = "EMAIL", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "phone", description = "PHONE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "state", description = "STATE", schema = @Schema(implementation = int.class, example = "1"))
    })
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_USER_ERROR)
    @OperatorLog(auditType = AuditType.USER_CREATE)
    public Result<User> createUser(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam(value = "userName") String userName,
                                   @RequestParam(value = "userPassword") String userPassword,
                                   @RequestParam(value = "tenantId") int tenantId,
                                   @RequestParam(value = "queue", required = false, defaultValue = "") String queue,
                                   @RequestParam(value = "email") String email,
                                   @RequestParam(value = "phone", required = false) String phone,
                                   @RequestParam(value = "state", required = false) int state) throws Exception {
        Result verifyRet = usersService.verifyUserName(userName);
        if (verifyRet.getCode() != Status.SUCCESS.getCode()) {
            return verifyRet;
        }
        User user = usersService.createUser(loginUser, userName, userPassword, email, tenantId, phone, queue, state);
        return Result.success(user);
    }

    @Operation(summary = "queryUserList", description = "QUERY_USER_LIST_NOTES")
    @Parameters({
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "10")),
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class))
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_USER_LIST_PAGING_ERROR)
    public Result queryUserList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam("pageNo") Integer pageNo,
                                @RequestParam("pageSize") Integer pageSize,
                                @RequestParam(value = "searchVal", required = false) String searchVal) {
        checkPageParams(pageNo, pageSize);
        searchVal = ParameterUtils.handleEscapes(searchVal);
        return usersService.queryUserList(loginUser, searchVal, pageNo, pageSize);
    }

    @Operation(summary = "updateUser", description = "UPDATE_USER_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "USER_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "userName", description = "USER_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "userPassword", description = "USER_PASSWORD", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "tenantId", description = "TENANT_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "queue", description = "QUEUE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "email", description = "EMAIL", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "phone", description = "PHONE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "state", description = "STATE", schema = @Schema(implementation = int.class, example = "1"))
    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_USER_ERROR)
    @OperatorLog(auditType = AuditType.USER_UPDATE)
    public Result<User> updateUser(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam(value = "id") int id,
                                   @RequestParam(value = "userName") String userName,
                                   @RequestParam(value = "userPassword") String userPassword,
                                   @RequestParam(value = "queue", required = false, defaultValue = "") String queue,
                                   @RequestParam(value = "email") String email,
                                   @RequestParam(value = "tenantId") int tenantId,
                                   @RequestParam(value = "phone", required = false) String phone,
                                   @RequestParam(value = "state", required = false) int state,
                                   @RequestParam(value = "timeZone", required = false) String timeZone) throws Exception {
        User user = usersService.updateUser(loginUser,
                id,
                userName,
                userPassword,
                email,
                tenantId,
                phone,
                queue,
                state,
                timeZone);
        return Result.success(user);
    }

    @Operation(summary = "delUserById", description = "DELETE_USER_BY_ID_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "USER_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_USER_BY_ID_ERROR)
    @OperatorLog(auditType = AuditType.USER_DELETE)
    public Result<Void> delUserById(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @RequestParam(value = "id") int id) throws Exception {
        usersService.deleteUserById(loginUser, id);
        return Result.success();
    }

    @Operation(summary = "revokeProjectById", description = "REVOKE_PROJECT_NOTES")
    @Parameters({
            @Parameter(name = "userId", description = "USER_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "projectIds", description = "PROJECT_IDS", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/revoke-project-by-id")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(REVOKE_PROJECT_ERROR)
    public Result<Void> revokeProjectById(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @RequestParam(value = "userId") int userId,
                                          @RequestParam(value = "projectIds") String projectIds) {
        usersService.revokeProjectById(loginUser, userId, projectIds);
        return Result.success();
    }

    @Operation(summary = "grantProjectWithReadPerm", description = "GRANT_PROJECT_WITH_READ_PERM_NOTES")
    @Parameters({
            @Parameter(name = "userId", description = "USER_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "projectIds", description = "PROJECT_IDS", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/grant-project-with-read-perm")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GRANT_PROJECT_ERROR)
    public Result<Void> grantProjectWithReadPerm(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                 @RequestParam(value = "userId") int userId,
                                                 @RequestParam(value = "projectIds") String projectIds) {
        usersService.grantProjectWithReadPerm(loginUser, userId, projectIds);
        return Result.success();
    }

    @Operation(summary = "grantProject", description = "GRANT_PROJECT_NOTES")
    @Parameters({
            @Parameter(name = "userId", description = "USER_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "projectIds", description = "PROJECT_IDS", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/grant-project")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GRANT_PROJECT_ERROR)
    public Result<Void> grantProject(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                     @RequestParam(value = "userId") int userId,
                                     @RequestParam(value = "projectIds") String projectIds) {
        usersService.grantProject(loginUser, userId, projectIds);
        return Result.success();
    }

    @Operation(summary = "grantProjectByCode", description = "GRANT_PROJECT_BY_CODE_NOTES")
    @Parameters({
            @Parameter(name = "userId", description = "USER_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true, schema = @Schema(implementation = long.class))
    })
    @PostMapping(value = "/grant-project-by-code")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GRANT_PROJECT_ERROR)
    public Result<Void> grantProjectByCode(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @RequestParam(value = "userId") int userId,
                                           @RequestParam(value = "projectCode") long projectCode) {
        usersService.grantProjectByCode(loginUser, userId, projectCode);
        return Result.success();
    }

    @Operation(summary = "revokeProject", description = "REVOKE_PROJECT_NOTES")
    @Parameters({
            @Parameter(name = "userId", description = "USER_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true, schema = @Schema(implementation = long.class, example = "100"))
    })
    @PostMapping(value = "/revoke-project")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(REVOKE_PROJECT_ERROR)
    public Result<Void> revokeProject(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                      @RequestParam(value = "userId") int userId,
                                      @RequestParam(value = "projectCode") long projectCode) {
        usersService.revokeProject(loginUser, userId, projectCode);
        return Result.success();
    }

    @Operation(summary = "grantNamespace", description = "GRANT_NAMESPACE_NOTES")
    @Parameters({
            @Parameter(name = "userId", description = "USER_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "namespaceIds", description = "NAMESPACE_IDS", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/grant-namespace")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GRANT_K8S_NAMESPACE_ERROR)
    public Result<Void> grantNamespace(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam(value = "userId") int userId,
                                       @RequestParam(value = "namespaceIds") String namespaceIds) {
        usersService.grantNamespaces(loginUser, userId, namespaceIds);
        return Result.success();
    }

    @Operation(summary = "grantDataSource", description = "GRANT_DATASOURCE_NOTES")
    @Parameters({
            @Parameter(name = "userId", description = "USER_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "datasourceIds", description = "DATASOURCE_IDS", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/grant-datasource")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GRANT_DATASOURCE_ERROR)
    public Result<Void> grantDataSource(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @RequestParam(value = "userId") int userId,
                                        @RequestParam(value = "datasourceIds") String datasourceIds) {
        usersService.grantDataSource(loginUser, userId, datasourceIds);
        return Result.success();
    }

    @Operation(summary = "getUserInfo", description = "GET_USER_INFO_NOTES")
    @GetMapping(value = "/get-user-info")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_USER_INFO_ERROR)
    public Result<User> getUserInfo(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        User user = usersService.getUserInfo(loginUser);
        return Result.success(user);
    }

    @Operation(summary = "listUser", description = "LIST_USER_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(USER_LIST_ERROR)
    public Result<List<User>> listUser(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        return Result.success(usersService.queryAllGeneralUsers(loginUser));
    }

    @GetMapping(value = "/list-all")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(USER_LIST_ERROR)
    public Result<List<User>> listAll(@RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        return Result.success(usersService.queryUserList(loginUser));
    }

    @Operation(summary = "verifyUserName", description = "VERIFY_USER_NAME_NOTES")
    @Parameters({
            @Parameter(name = "userName", description = "USER_NAME", required = true, schema = @Schema(implementation = String.class))
    })
    @GetMapping(value = "/verify-user-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_USERNAME_ERROR)
    public Result verifyUserName(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam(value = "userName") String userName) {
        return usersService.verifyUserName(userName);
    }

    @Operation(summary = "unauthorizedUser", description = "UNAUTHORIZED_USER_NOTES")
    @Parameters({
            @Parameter(name = "alertgroupId", description = "ALERT_GROUP_ID", required = true, schema = @Schema(implementation = String.class))
    })
    @GetMapping(value = "/unauth-user")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UNAUTHORIZED_USER_ERROR)
    public Result<List<User>> unauthorizedUser(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @RequestParam("alertgroupId") Integer alertgroupId) {
        return Result.success(usersService.unauthorizedUser(loginUser, alertgroupId));
    }

    @Operation(summary = "authorizedUser", description = "AUTHORIZED_USER_NOTES")
    @Parameters({
            @Parameter(name = "alertgroupId", description = "ALERT_GROUP_ID", required = true, schema = @Schema(implementation = String.class))
    })
    @GetMapping(value = "/authed-user")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(AUTHORIZED_USER_ERROR)
    public Result<List<User>> authorizedUser(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @RequestParam("alertgroupId") Integer alertgroupId) {
        return Result.success(usersService.authorizedUser(loginUser, alertgroupId));
    }

    @Operation(summary = "registerUser", description = "REGISTER_USER_NOTES")
    @Parameters({
            @Parameter(name = "userName", description = "USER_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "userPassword", description = "USER_PASSWORD", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "repeatPassword", description = "REPEAT_PASSWORD", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "email", description = "EMAIL", required = true, schema = @Schema(implementation = String.class)),
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(CREATE_USER_ERROR)
    public Result<User> registerUser(@RequestParam(value = "userName") String userName,
                                     @RequestParam(value = "userPassword") String userPassword,
                                     @RequestParam(value = "repeatPassword") String repeatPassword,
                                     @RequestParam(value = "email") String email) throws Exception {
        userName = ParameterUtils.handleEscapes(userName);
        userPassword = ParameterUtils.handleEscapes(userPassword);
        repeatPassword = ParameterUtils.handleEscapes(repeatPassword);
        email = ParameterUtils.handleEscapes(email);
        Result<Object> verifyRet = usersService.verifyUserName(userName);
        if (verifyRet.getCode() != Status.SUCCESS.getCode()) {
            return new Result<>(verifyRet.getCode(), verifyRet.getMsg());
        }
        return Result.success(usersService.registerUser(userName, userPassword, repeatPassword, email));
    }

    @Operation(summary = "activateUser", description = "ACTIVATE_USER_NOTES")
    @Parameters({
            @Parameter(name = "userName", description = "USER_NAME", schema = @Schema(implementation = String.class)),
    })
    @PostMapping("/activate")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_USER_ERROR)
    public Result<User> activateUser(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                     @RequestParam(value = "userName") String userName) {
        userName = ParameterUtils.handleEscapes(userName);
        return Result.success(usersService.activateUser(loginUser, userName));
    }

    @Operation(summary = "batchActivateUser", description = "BATCH_ACTIVATE_USER_NOTES")
    @Parameters({
            @Parameter(name = "userNames", description = "USER_NAMES", required = true, schema = @Schema(implementation = List.class)),
    })
    @PostMapping("/batch/activate")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_USER_ERROR)
    public Result<Map<String, Object>> batchActivateUser(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                         @RequestBody List<String> userNames) {
        List<String> formatUserNames =
                userNames.stream().map(ParameterUtils::handleEscapes).collect(Collectors.toList());
        return Result.success(usersService.batchActivateUser(loginUser, formatUserNames));
    }
}
