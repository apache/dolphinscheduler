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
import org.apache.dolphinscheduler.api.security.Authenticator;
import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import io.swagger.annotations.*;
import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;

import static org.apache.dolphinscheduler.api.enums.Status.*;

/**
 * user login controller
 *
 * swagger bootstrap ui docs refer : https://doc.xiaominfo.com/guide/enh-func.html
 */
@Api(tags = "LOGIN_TAG", position = 1)
@RestController
@RequestMapping("")
public class LoginController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private static final String COOKIE_ROOT_PATH = "/";

    @Autowired
    private SessionService sessionService;

    @Autowired
    private Authenticator authenticator;

    /**
     * login
     *
     * @param userName user name
     * @param userPassword user password
     * @param request request
     * @param response  response
     * @return login result
     */
    @ApiOperation(value = "login", notes= "LOGIN_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "USER_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "userPassword", value = "USER_PASSWORD", required = true, dataType ="String")
    })
    @PostMapping(value = "/login")
    public Result login(@RequestParam(value = "userName") String userName,
                        @RequestParam(value = "userPassword") String userPassword,
                        HttpServletRequest request,
                        HttpServletResponse response) {

        try {
            logger.info("login user name: {} ", userName);

            //user name check
            if (StringUtils.isEmpty(userName)) {
                return error(Status.USER_NAME_NULL.getCode(),
                        Status.USER_NAME_NULL.getMsg());
            }

            // user ip check
            String ip = getClientIpAddress(request);
            if (StringUtils.isEmpty(ip)) {
                return error(IP_IS_EMPTY.getCode(), IP_IS_EMPTY.getMsg());
            }

            // verify username and password
            Result<Map<String, String>> result = authenticator.authenticate(userName, userPassword, ip);
            if (result.getCode() != Status.SUCCESS.getCode()) {
                return result;
            }

            response.setStatus(HttpStatus.SC_OK);
            Map<String, String> cookieMap = result.getData();
            for (Map.Entry<String, String> cookieEntry : cookieMap.entrySet()) {
                Cookie cookie = new Cookie(cookieEntry.getKey(), cookieEntry.getValue());
                cookie.setHttpOnly(true);
                cookie.setPath(COOKIE_ROOT_PATH);
                response.addCookie(cookie);
            }

            return result;
        } catch (Exception e) {
            logger.error(USER_LOGIN_FAILURE.getMsg(),e);
            return error(USER_LOGIN_FAILURE.getCode(), USER_LOGIN_FAILURE.getMsg());
        }
    }

    /**
     * sign out
     *
     * @param loginUser login user
     * @param request  request
     * @return sign out result
     */
    @ApiOperation(value = "signOut", notes = "SIGNOUT_NOTES")
    @PostMapping(value = "/signOut")
    public Result signOut(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                          HttpServletRequest request) {

        try {
            logger.info("login user:{} sign out", loginUser.getUserName());
            String ip = getClientIpAddress(request);
            sessionService.signOut(ip, loginUser);
            //clear session
            request.removeAttribute(Constants.SESSION_USER);
            return success();
        } catch (Exception e) {
            logger.error(SIGN_OUT_ERROR.getMsg(),e);
            return error(SIGN_OUT_ERROR.getCode(), SIGN_OUT_ERROR.getMsg());
        }
    }
}
