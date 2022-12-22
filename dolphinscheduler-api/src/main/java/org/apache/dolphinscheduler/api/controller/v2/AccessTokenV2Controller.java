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

package org.apache.dolphinscheduler.api.controller.v2;

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_ACCESS_TOKEN_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.controller.BaseController;
import org.apache.dolphinscheduler.api.dto.CreateTokenRequest;
import org.apache.dolphinscheduler.api.dto.CreateTokenResponse;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.AccessTokenService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * access token controller
 */
@Tag(name = "ACCESS_TOKEN_TAG")
@RestController
@RequestMapping("/v2/access-tokens")
public class AccessTokenV2Controller extends BaseController {

    @Autowired
    private AccessTokenService accessTokenService;

    /**
     * create token
     *
     * @param loginUser          login user
     * @param createTokenRequest createTokenRequest
     * @return CreateTokenResponse CreateTokenResponse
     */
    @Operation(summary = "createTokenV2", description = "CREATE_TOKEN_V2")
    @PostMapping(consumes = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    @Parameter(name = "createTokenRequest", description = "createTokenRequest", required = true, schema = @Schema(implementation = CreateTokenRequest.class))
    @ApiException(CREATE_ACCESS_TOKEN_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public CreateTokenResponse createToken(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @RequestBody CreateTokenRequest createTokenRequest) {
        Result result = accessTokenService.createToken(loginUser,
                createTokenRequest.getUserId(),
                createTokenRequest.getExpireTime(),
                createTokenRequest.getToken());
        return new CreateTokenResponse(result);
    }
}
