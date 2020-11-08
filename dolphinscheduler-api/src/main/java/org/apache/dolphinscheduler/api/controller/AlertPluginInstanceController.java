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
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_ALERT_PLUGIN_INSTANCE_ERROR;

import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.AlertPluginInstanceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * alert plugin instance controller
 */
@Api(tags = "ALERT_PLUGIN_INSTANCE_TAG", position = 1)
@RestController
@RequestMapping("alert-plugin-instance")
public class AlertPluginInstanceController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(AlertPluginInstanceController.class);

    @Autowired
    private AlertPluginInstanceService alertPluginInstanceService;


    /**
     * create alert plugin instance
     *
     * @param loginUser login user
     * @param alertPluginInstance alertPluginInstance
     * @return create result code
     */
    @ApiOperation(value = "createAlertPluginInstance", notes = "CREATE_ALERT_PLUGIN_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupName", value = "GROUP_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "groupType", value = "GROUP_TYPE", required = true, dataType = "AlertType"),
            @ApiImplicitParam(name = "description", value = "DESC", dataType = "String")
    })
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_ALERT_PLUGIN_INSTANCE_ERROR)
    public Result createAlertPluginInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestBody AlertPluginInstance alertPluginInstance) {
        logger.info("loginUser user {}, create alert plugin instance, groupName: "
                , loginUser.getUserName());
        Map<String, Object> result = alertPluginInstanceService.create(loginUser, alertPluginInstance);
        return returnDataList(result);
    }

    /**
     * updateAlertPluginInstance
     *
     * @param loginUser login user
     * @param alertPluginInstance alertPluginInstance
     * @return result
     */
    @ApiOperation(value = "update", notes = "UPDATE_ALERT_PLUGIN_INSTANCE_NOTES")
    @GetMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_ALERT_PLUGIN_INSTANCE_ERROR)
    public Result updateAlertPluginInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestBody AlertPluginInstance alertPluginInstance) {
        logger.info("login  user {}, update alert plugin instance id {}",
                loginUser.getUserName(), alertPluginInstance.getId());
        Map<String, Object> result = alertPluginInstanceService.update(loginUser, alertPluginInstance);
        return returnDataList(result);
    }

    /**
     * deleteAlertPluginInstance
     *
     * @param loginUser login user
     * @param alertPluginInstance alertPluginInstance
     * @return result
     */
    @ApiOperation(value = "delete", notes = "DELETE_ALERT_PLUGIN_INSTANCE_NOTES")
    @GetMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_ALERT_PLUGIN_INSTANCE_ERROR)
    public Result deleteAlertPluginInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestBody AlertPluginInstance alertPluginInstance) {
        logger.info("login  user {}, delete alert plugin instance id {}", loginUser.getUserName(), alertPluginInstance.getId());

        Map<String, Object> result = alertPluginInstanceService.delete(loginUser, alertPluginInstance);
        return returnDataListPaging(result);
    }

    /**
     * getAlertPluginInstance
     *
     * @param loginUser login user
     * @param id alert plugin instance id
     * @return result
     */
    @ApiOperation(value = "get", notes = "GET_ALERT_PLUGIN_INSTANCE_NOTES")
    @PostMapping(value = "/get")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_ALERT_PLUGIN_INSTANCE_ERROR)
    public Result getAlertPluginInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @RequestParam(value = "id") int id) {
        logger.info("login  user {}, get alert plugin instance, id {}",
                loginUser.getUserName(), id);
        Map<String, Object> result = alertPluginInstanceService.get(loginUser, id);
        return returnDataList(result);
    }
}
