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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_ALERT_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_ALERT_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GET_ALERT_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.LIST_PAGING_ALERT_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_ALL_ALERT_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_ALERT_PLUGIN_INSTANCE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.dto.alert.AlertPluginInstanceListPagingResponse;
import org.apache.dolphinscheduler.api.dto.alert.AlertPluginInstanceListResponse;
import org.apache.dolphinscheduler.api.dto.alert.AlertPluginInstanceResponse;
import org.apache.dolphinscheduler.api.dto.alert.AlertPluginQueryRequest;
import org.apache.dolphinscheduler.api.dto.alert.CreatePluginRequest;
import org.apache.dolphinscheduler.api.dto.alert.UpdatePluginRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.AlertPluginInstanceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
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
 * alert plugin instance controller
 */
@Api(tags = "ALERT_PLUGIN_INSTANCE_TAG")
@RestController
@RequestMapping("/v2/alert-plugin-instances")
public class AlertPluginInstanceV2Controller extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(AlertPluginInstanceController.class);

    @Autowired
    private AlertPluginInstanceService alertPluginInstanceService;

    /**
     * create alert plugin instance
     *
     * @param loginUser login user
     * @param request request
     * @return result
     */
    @ApiOperation(value = "createAlertPluginInstance", notes = "CREATE_ALERT_PLUGIN_INSTANCE_NOTES")
    @PostMapping(consumes = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createAlertPluginInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestBody CreatePluginRequest request) {
        return alertPluginInstanceService.create(loginUser, request.getId(), request.getInstanceName(),
                request.getPluginInstanceParams());
    }

    /**
     * updateAlertPluginInstance
     *
     * @param loginUser login user
     * @param id alert plugin instance id
     * @param request request
     * @return request
     */
    @ApiOperation(value = "updateAlertPluginInstance", notes = "UPDATE_ALERT_PLUGIN_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "alertPluginInstanceId", value = "ALERT_PLUGIN_INSTANCE_ID", required = true, dataType = "Int", example = "100"),
    })
    @PutMapping(consumes = {"application/json"}, value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateAlertPluginInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @PathVariable(value = "id") int id,
                                            @RequestBody UpdatePluginRequest request) {
        return alertPluginInstanceService.update(loginUser, id, request.getInstanceName(),
                request.getPluginInstanceParams());
    }

    /**
     * deleteAlertPluginInstance
     *
     * @param loginUser login user
     * @param id id
     * @return result
     */
    @ApiOperation(value = "deleteAlertPluginInstance", notes = "DELETE_ALERT_PLUGIN_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ALERT_PLUGIN_ID", required = true, dataType = "Int", example = "100")
    })
    @DeleteMapping(consumes = {"application/json"}, value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteAlertPluginInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @PathVariable(value = "id") int id) {

        return alertPluginInstanceService.delete(loginUser, id);
    }

    /**
     * getAlertPluginInstance
     *
     * @param loginUser login user
     * @param id alert plugin instance id
     * @return result
     */
    @ApiOperation(value = "getAlertPluginInstance", notes = "GET_ALERT_PLUGIN_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ALERT_PLUGIN_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(consumes = {"application/json"}, value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public AlertPluginInstanceResponse getAlertPluginInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                              @PathVariable(value = "id") int id) {
        return new AlertPluginInstanceResponse(alertPluginInstanceService.get(loginUser, id));
    }

    /**
     * getAlertPluginInstance
     *
     * @param loginUser login user
     * @return result
     */
    @ApiOperation(value = "queryAlertPluginInstanceList", notes = "QUERY_ALL_ALERT_PLUGIN_INSTANCE_NOTES")
    @GetMapping(consumes = {"application/json"}, value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ALL_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public AlertPluginInstanceListResponse getAlertPluginInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        return new AlertPluginInstanceListResponse(alertPluginInstanceService.queryAll());
    }

    /**
     * check alert group exist
     *
     * @param loginUser login user
     * @param alertInstanceName alert instance name
     * @return check result code
     */
    @ApiOperation(value = "verifyAlertInstanceName", notes = "VERIFY_ALERT_INSTANCE_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "alertInstanceName", value = "ALERT_INSTANCE_NAME", required = true, dataType = "String"),
    })
    @GetMapping(consumes = {"application/json"}, value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result verifyGroupName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam(value = "alertInstanceName") String alertInstanceName) {

        boolean exist = alertPluginInstanceService.checkExistPluginInstanceName(alertInstanceName);
        if (exist) {
            logger.error("alert plugin instance {} has exist, can't create again.", alertInstanceName);
            return Result.error(Status.PLUGIN_INSTANCE_ALREADY_EXIT);
        } else {
            return Result.success();
        }
    }

    /**
     * paging query alert plugin instance group list
     *
     * @param loginUser
     * @param request
     * @return
     */
    @ApiOperation(value = "queryAlertPluginInstanceListPaging", notes = "QUERY_ALERT_PLUGIN_INSTANCE_LIST_PAGING_NOTES")
    @GetMapping(consumes = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LIST_PAGING_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public AlertPluginInstanceListPagingResponse listPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                            @RequestBody AlertPluginQueryRequest request) {
        Result result = checkPageParams(request.getPageNo(), request.getPageSize());
        if (!result.checkResult()) {
            return new AlertPluginInstanceListPagingResponse(result);
        }
        request.setSearchVal(ParameterUtils.handleEscapes(request.getSearchVal()));
        return new AlertPluginInstanceListPagingResponse(alertPluginInstanceService.listPaging(loginUser,
                request.getSearchVal(), request.getPageNo(), request.getPageSize()));
    }

}
