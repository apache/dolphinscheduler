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

import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PLUGINS_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.UiPluginService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.PluginType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * ui plugin controller
 * Some plugins (such as alert plugin) need to provide UI interfaces to users.
 * We use from-creat to dynamically generate UI interfaces. Related parameters are mainly provided by pluginParams.
 * From-create can generate dynamic ui based on this parameter.
 */
@Api(tags = "UI_PLUGINS_TAG")
@RestController
@RequestMapping("ui-plugins")
public class UiPluginController extends BaseController {

    @Autowired
    UiPluginService uiPluginService;

    @ApiOperation(value = "queryUiPluginsByType", notes = "QUERY_UI_PLUGINS_BY_TYPE")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pluginType", value = "pluginType", required = true, dataType = "PluginType"),
    })
    @GetMapping(value = "/query-by-type")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(QUERY_PLUGINS_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryUiPluginsByType(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam(value = "pluginType") PluginType pluginType) {

        Map<String, Object> result = uiPluginService.queryUiPluginsByType(pluginType);
        return returnDataList(result);
    }

    @ApiOperation(value = "queryUiPluginDetailById", notes = "QUERY_UI_PLUGIN_DETAIL_BY_ID")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", value = "PLUGIN_ID", required = true, dataType = "Int", example = "100"),
    })
    @GetMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(QUERY_PLUGINS_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryUiPluginDetailById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @PathVariable("id") Integer pluginId) {

        Map<String, Object> result = uiPluginService.queryUiPluginDetailById(pluginId);
        return returnDataList(result);
    }
}
