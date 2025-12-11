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

import static org.apache.dolphinscheduler.api.enums.Status.QUERY_EXTERNAL_SYSTEM_ERROR;

import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ExternalSystemService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.ExternalSystemTaskQuery;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "EXTERNAL_SYSTEM_TAG")
@RestController
@RequestMapping("external-systems")
public class ExternalSystemController extends BaseController {

    @Autowired
    private ExternalSystemService externalSystemService;

    @Operation(summary = "queryExternalSystemTasks", description = "QUERY_EXTERNAL_SYSTEM_TASKS_NOTES")
    @GetMapping(value = "/queryExternalSystemTasks")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_EXTERNAL_SYSTEM_ERROR)
    public Result<List<ExternalSystemTaskQuery>> queryExternalSystemTasks(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                          @RequestParam("externalSystemId") Integer externalSystemId) {
        List<ExternalSystemTaskQuery> result =
                externalSystemService.queryExternalSystemTasks(loginUser, externalSystemId);
        return Result.success(result);
    }

}
