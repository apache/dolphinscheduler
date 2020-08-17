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


import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.AccessTokenService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

import static org.apache.dolphinscheduler.api.enums.Status.*;

/**
 * access token controller
 */
@Api(tags = "ACCESS_TOKEN_TAG", position = 1)
@RestController
@RequestMapping("/access-token")
public class AccessTokenController extends BaseController {


    private static final Logger logger = LoggerFactory.getLogger(AccessTokenController.class);


    @Autowired
    private AccessTokenService accessTokenService;

    /**
     * create token
     *
     * @param loginUser  login user
     * @param userId     token for user id
     * @param expireTime expire time for the token
     * @param token      token
     * @return create result state code
     */
    @ApiIgnore
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_ACCESS_TOKEN_ERROR)
    public Result createToken(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                              @RequestParam(value = "userId") int userId,
                              @RequestParam(value = "expireTime") String expireTime,
                              @RequestParam(value = "token") String token) {
        logger.info("login user {}, create token , userId : {} , token expire time : {} , token : {}", loginUser.getUserName(),
                userId, expireTime, token);

        Map<String, Object> result = accessTokenService.createToken(loginUser, userId, expireTime, token);
        return returnDataList(result);
    }

    /**
     * generate token string
     *
     * @param loginUser  login user
     * @param userId     token for user
     * @param expireTime expire time
     * @return token string
     */
    @ApiIgnore
    @PostMapping(value = "/generate")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(GENERATE_TOKEN_ERROR)
    public Result generateToken(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam(value = "userId") int userId,
                                @RequestParam(value = "expireTime") String expireTime) {
        logger.info("login user {}, generate token , userId : {} , token expire time : {}", loginUser, userId, expireTime);
        Map<String, Object> result = accessTokenService.generateToken(loginUser, userId, expireTime);
        return returnDataList(result);
    }

    /**
     * query access token list paging
     *
     * @param loginUser login user
     * @param pageNo    page number
     * @param searchVal search value
     * @param pageSize  page size
     * @return token list of page number and page size
     */
    @ApiOperation(value = "queryAccessTokenList", notes = "QUERY_ACCESS_TOKEN_LIST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataType = "String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType = "Int", example = "20")
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ACCESSTOKEN_LIST_PAGING_ERROR)
    public Result queryAccessTokenList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam("pageNo") Integer pageNo,
                                       @RequestParam(value = "searchVal", required = false) String searchVal,
                                       @RequestParam("pageSize") Integer pageSize) {
        logger.info("login user {}, list access token paging, pageNo: {}, searchVal: {}, pageSize: {}",
                loginUser.getUserName(), pageNo, searchVal, pageSize);

        Map<String, Object> result = checkPageParams(pageNo, pageSize);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataListPaging(result);
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = accessTokenService.queryAccessTokenList(loginUser, searchVal, pageNo, pageSize);
        return returnDataListPaging(result);
    }

    /**
     * delete access token by id
     *
     * @param loginUser login user
     * @param id        token id
     * @return delete result code
     */
    @ApiIgnore
    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_ACCESS_TOKEN_ERROR)
    public Result delAccessTokenById(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                     @RequestParam(value = "id") int id) {
        logger.info("login user {}, delete access token, id: {},", loginUser.getUserName(), id);
        Map<String, Object> result = accessTokenService.delAccessTokenById(loginUser, id);
        return returnDataList(result);
    }


    /**
     * update token
     *
     * @param loginUser  login user
     * @param id         token id
     * @param userId     token for user
     * @param expireTime token expire time
     * @param token      token string
     * @return update result code
     */
    @ApiIgnore
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_ACCESS_TOKEN_ERROR)
    public Result updateToken(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                              @RequestParam(value = "id") int id,
                              @RequestParam(value = "userId") int userId,
                              @RequestParam(value = "expireTime") String expireTime,
                              @RequestParam(value = "token") String token) {
        logger.info("login user {}, update token , userId : {} , token expire time : {} , token : {}", loginUser.getUserName(),
                userId, expireTime, token);

        Map<String, Object> result = accessTokenService.updateToken(loginUser, id, userId, expireTime, token);
        return returnDataList(result);
    }

}
