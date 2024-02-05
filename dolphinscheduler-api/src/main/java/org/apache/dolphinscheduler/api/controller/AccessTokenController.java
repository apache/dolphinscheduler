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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_ACCESS_TOKEN_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_ACCESS_TOKEN_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GENERATE_TOKEN_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_ACCESSTOKEN_BY_USER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_ACCESSTOKEN_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_ACCESS_TOKEN_ERROR;

import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.AccessTokenService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.AccessToken;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * access token controller
 */
@Tag(name = "ACCESS_TOKEN_TAG")
@RestController
@RequestMapping("/access-tokens")
public class AccessTokenController extends BaseController {

    @Autowired
    private AccessTokenService accessTokenService;

    /**
     * create token
     *
     * @param loginUser login user
     * @param userId token for user id
     * @param expireTime expire time for the token
     * @param token token string (if it is absent, it will be automatically generated)
     * @return create result state code
     */
    @Operation(summary = "createToken", description = "CREATE_TOKEN_NOTES")
    @Parameters({
            @Parameter(name = "userId", description = "USER_ID", schema = @Schema(implementation = int.class), required = true),
            @Parameter(name = "expireTime", description = "EXPIRE_TIME", schema = @Schema(implementation = String.class), required = true, example = "2021-12-31 00:00:00"),
            @Parameter(name = "token", description = "TOKEN", required = false, schema = @Schema(implementation = String.class), example = "xxxx")
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_ACCESS_TOKEN_ERROR)
    public Result<AccessToken> createToken(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @RequestParam(value = "userId") int userId,
                                           @RequestParam(value = "expireTime") String expireTime,
                                           @RequestParam(value = "token", required = false) String token) {

        AccessToken accessToken = accessTokenService.createToken(loginUser, userId, expireTime, token);
        return Result.success(accessToken);
    }

    /**
     * generate token string
     *
     * @param loginUser login user
     * @param userId token for user
     * @param expireTime expire time
     * @return token string
     */
    @Parameter(hidden = true)
    @PostMapping(value = "/generate")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(GENERATE_TOKEN_ERROR)
    public Result<String> generateToken(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @RequestParam(value = "userId") int userId,
                                        @RequestParam(value = "expireTime") String expireTime) {
        String token = accessTokenService.generateToken(loginUser, userId, expireTime);
        return Result.success(token);
    }

    /**
     * query access token list paging
     *
     * @param loginUser login user
     * @param pageNo page number
     * @param searchVal search value
     * @param pageSize page size
     * @return token list of page number and page size
     */
    @Operation(summary = "queryAccessTokenList", description = "QUERY_ACCESS_TOKEN_LIST_NOTES")
    @Parameters({
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class), example = "1"),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class), example = "20")
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ACCESSTOKEN_LIST_PAGING_ERROR)
    public Result<PageInfo<AccessToken>> queryAccessTokenList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                              @RequestParam("pageNo") Integer pageNo,
                                                              @RequestParam(value = "searchVal", required = false) String searchVal,
                                                              @RequestParam("pageSize") Integer pageSize) {

        checkPageParams(pageNo, pageSize);
        searchVal = ParameterUtils.handleEscapes(searchVal);
        PageInfo<AccessToken> accessTokenPageInfo =
                accessTokenService.queryAccessTokenList(loginUser, searchVal, pageNo, pageSize);
        return Result.success(accessTokenPageInfo);
    }

    /**
     * query access token for specified user
     *
     * @param loginUser login user
     * @param userId user id
     * @return token list for specified user
     */
    @Operation(summary = "queryAccessTokenByUser", description = "QUERY_ACCESS_TOKEN_BY_USER_NOTES")
    @Parameters({
            @Parameter(name = "userId", description = "USER_ID", schema = @Schema(implementation = int.class))
    })
    @GetMapping(value = "/user/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ACCESSTOKEN_BY_USER_ERROR)
    public Result<List<AccessToken>> queryAccessTokenByUser(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                            @PathVariable("userId") Integer userId) {
        List<AccessToken> accessTokens = accessTokenService.queryAccessTokenByUser(loginUser, userId);
        return Result.success(accessTokens);
    }

    /**
     * delete access token by id
     *
     * @param loginUser login user
     * @param id token id
     * @return delete result code
     */
    @Parameter(hidden = true)
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_ACCESS_TOKEN_ERROR)
    public Result<Boolean> delAccessTokenById(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @PathVariable(value = "id") int id) {
        accessTokenService.deleteAccessTokenById(loginUser, id);
        return Result.success(true);
    }

    /**
     * update token
     *
     * @param loginUser login user
     * @param id token id
     * @param userId token for user
     * @param expireTime token expire time
     * @param token token string (if it is absent, it will be automatically generated)
     * @return updated access token entity
     */
    @Operation(summary = "updateToken", description = "UPDATE_TOKEN_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "TOKEN_ID", required = true, schema = @Schema(implementation = int.class)),
            @Parameter(name = "userId", description = "USER_ID", required = true, schema = @Schema(implementation = int.class)),
            @Parameter(name = "expireTime", description = "EXPIRE_TIME", required = true, schema = @Schema(implementation = String.class), example = "2021-12-31 00:00:00"),
            @Parameter(name = "token", description = "TOKEN", required = false, schema = @Schema(implementation = String.class), example = "xxxx")
    })
    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_ACCESS_TOKEN_ERROR)
    public Result<AccessToken> updateToken(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @PathVariable(value = "id") int id,
                                           @RequestParam(value = "userId") int userId,
                                           @RequestParam(value = "expireTime") String expireTime,
                                           @RequestParam(value = "token", required = false) String token) {

        AccessToken accessToken = accessTokenService.updateToken(loginUser, id, userId, expireTime, token);
        return Result.success(accessToken);
    }

}
