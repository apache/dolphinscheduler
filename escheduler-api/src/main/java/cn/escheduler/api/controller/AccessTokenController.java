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
import cn.escheduler.api.service.AccessTokenService;
import cn.escheduler.api.service.UsersService;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.Result;
import cn.escheduler.common.utils.ParameterUtils;
import cn.escheduler.dao.model.User;
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

import static cn.escheduler.api.enums.Status.*;


/**
 * access token controller
 */
@Api(tags = "ACCESS_TOKEN_TAG", position = 1)
@RestController
@RequestMapping("/access-token")
public class AccessTokenController extends BaseController{


    private static final Logger logger = LoggerFactory.getLogger(AccessTokenController.class);


    @Autowired
    private AccessTokenService accessTokenService;

    /**
     * create token
     * @param loginUser
     * @return
     */
    @ApiIgnore
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Result createToken(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @RequestParam(value = "userId") int userId,
                                                  @RequestParam(value = "expireTime") String expireTime,
                                                  @RequestParam(value = "token") String token){
        logger.info("login user {}, create token , userId : {} , token expire time : {} , token : {}", loginUser.getUserName(),
                userId,expireTime,token);

        try {
            Map<String, Object> result = accessTokenService.createToken(userId, expireTime, token);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(CREATE_ACCESS_TOKEN_ERROR.getMsg(),e);
            return error(CREATE_ACCESS_TOKEN_ERROR.getCode(), CREATE_ACCESS_TOKEN_ERROR.getMsg());
        }
    }

    /**
     * create token
     * @param loginUser
     * @return
     */
    @ApiIgnore
    @PostMapping(value = "/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public Result generateToken(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                              @RequestParam(value = "userId") int userId,
                              @RequestParam(value = "expireTime") String expireTime){
        logger.info("login user {}, generate token , userId : {} , token expire time : {}",loginUser,userId,expireTime);
        try {
            Map<String, Object> result = accessTokenService.generateToken(userId, expireTime);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(GENERATE_TOKEN_ERROR.getMsg(),e);
            return error(GENERATE_TOKEN_ERROR.getCode(), GENERATE_TOKEN_ERROR.getMsg());
        }
    }

    /**
     * query access token list paging
     *
     * @param loginUser
     * @param pageNo
     * @param searchVal
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "queryAccessTokenList", notes= "QUERY_ACCESS_TOKEN_LIST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataType ="String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType ="Int",example = "20")
    })
    @GetMapping(value="/list-paging")
    @ResponseStatus(HttpStatus.OK)
    public Result queryAccessTokenList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam("pageNo") Integer pageNo,
                                @RequestParam(value = "searchVal", required = false) String searchVal,
                                @RequestParam("pageSize") Integer pageSize){
        logger.info("login user {}, list access token paging, pageNo: {}, searchVal: {}, pageSize: {}",
                loginUser.getUserName(),pageNo,searchVal,pageSize);
        try{
            Map<String, Object> result = checkPageParams(pageNo, pageSize);
            if(result.get(Constants.STATUS) != Status.SUCCESS){
                return returnDataListPaging(result);
            }
            searchVal = ParameterUtils.handleEscapes(searchVal);
            result = accessTokenService.queryAccessTokenList(loginUser, searchVal, pageNo, pageSize);
            return returnDataListPaging(result);
        }catch (Exception e){
            logger.error(QUERY_ACCESSTOKEN_LIST_PAGING_ERROR.getMsg(),e);
            return error(QUERY_ACCESSTOKEN_LIST_PAGING_ERROR.getCode(),QUERY_ACCESSTOKEN_LIST_PAGING_ERROR.getMsg());
        }
    }

    /**
     * delete access token by id
     * @param loginUser
     * @param id
     * @return
     */
    @ApiIgnore
    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    public Result delAccessTokenById(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                              @RequestParam(value = "id") int  id) {
        logger.info("login user {}, delete access token, id: {},", loginUser.getUserName(), id);
        try {
            Map<String, Object> result = accessTokenService.delAccessTokenById(loginUser, id);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(DELETE_USER_BY_ID_ERROR.getMsg(),e);
            return error(Status.DELETE_USER_BY_ID_ERROR.getCode(), Status.DELETE_USER_BY_ID_ERROR.getMsg());
        }
    }


    /**
     * update token
     * @param loginUser
     * @return
     */
    @ApiIgnore
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.CREATED)
    public Result updateToken(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                              @RequestParam(value = "id") int id,
                              @RequestParam(value = "userId") int userId,
                              @RequestParam(value = "expireTime") String expireTime,
                              @RequestParam(value = "token") String token){
        logger.info("login user {}, update token , userId : {} , token expire time : {} , token : {}", loginUser.getUserName(),
                userId,expireTime,token);

        try {
            Map<String, Object> result = accessTokenService.updateToken(id,userId, expireTime, token);
            return returnDataList(result);
        }catch (Exception e){
            logger.error(CREATE_ACCESS_TOKEN_ERROR.getMsg(),e);
            return error(CREATE_ACCESS_TOKEN_ERROR.getCode(), CREATE_ACCESS_TOKEN_ERROR.getMsg());
        }
    }

}
