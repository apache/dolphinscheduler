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

import static org.apache.dolphinscheduler.api.enums.Status.LIST_AZURE_DATA_FACTORY_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.LIST_AZURE_DATA_FACTORY_PIPELINE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.LIST_AZURE_RESOURCE_GROUP_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.CloudService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;

import javax.annotation.Resource;

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

/**
 * cloud controller
 */
@Tag(name = "CLOUD_TAG")
@RestController
@RequestMapping("/cloud")
public class CloudController extends BaseController {

    @Resource
    private CloudService cloudService;

    /**
     * get datafactory list
     *
     * @param loginUser login user
     * @return datafactory name list
     */
    @Operation(summary = "listDataFactory", description = "LIST_DATA_FACTORY")
    @GetMapping(value = "/azure/datafactory/factories")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LIST_AZURE_DATA_FACTORY_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result listDataFactory(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        List<String> factoryNames = cloudService.listDataFactory(loginUser);
        return success(Status.SUCCESS.getMsg(), factoryNames);
    }

    /**
     * get resourceGroup list
     *
     * @param loginUser login user
     * @return resourceGroup list
     */
    @Operation(summary = "listResourceGroup", description = "LIST_RESOURCE_GROUP")
    @GetMapping(value = "/azure/datafactory/resourceGroups")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LIST_AZURE_RESOURCE_GROUP_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result listResourceGroup(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        List<String> resourceGroupNames = cloudService.listResourceGroup(loginUser);
        return success(Status.SUCCESS.getMsg(), resourceGroupNames);
    }

    /**
     * get resourceGroup list
     *
     * @param loginUser login user
     * @return resourceGroup list
     */
    @Operation(summary = "listPipeline", description = "LIST_PIPELINE")
    @GetMapping(value = "/azure/datafactory/pipelines")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LIST_AZURE_DATA_FACTORY_PIPELINE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result listPipeline(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                               @RequestParam("factoryName") String factoryName,
                               @RequestParam("resourceGroupName") String resourceGroupName) {
        List<String> pipelineNames = cloudService.listPipeline(loginUser, factoryName, resourceGroupName);
        return success(Status.SUCCESS.getMsg(), pipelineNames);
    }
}
