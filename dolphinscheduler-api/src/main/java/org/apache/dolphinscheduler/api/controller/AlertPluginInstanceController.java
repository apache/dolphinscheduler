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

import static org.apache.dolphinscheduler.api.enums.v2.PluginStatus.CREATE_ALERT_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.v2.PluginStatus.DELETE_ALERT_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.v2.PluginStatus.GET_ALERT_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.v2.PluginStatus.LIST_PAGING_ALERT_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.v2.PluginStatus.QUERY_ALL_ALERT_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.v2.PluginStatus.SEND_TEST_ALERT_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.v2.PluginStatus.UPDATE_ALERT_PLUGIN_INSTANCE_ERROR;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.PluginApiException;
import org.apache.dolphinscheduler.api.service.AlertPluginInstanceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.AlertPluginInstanceVO;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AlertPluginInstanceType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

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
 * alert plugin instance controller
 */
@Tag(name = "ALERT_PLUGIN_INSTANCE_TAG")
@RestController
@RequestMapping("alert-plugin-instances")
@Slf4j
public class AlertPluginInstanceController extends BaseController {

    @Autowired
    private AlertPluginInstanceService alertPluginInstanceService;

    /**
     * create alert plugin instance
     *
     * @param loginUser login user
     * @param pluginDefineId alert plugin define id
     * @param instanceName instance name
     * @param pluginInstanceParams instance params
     * @return result
     */
    @Operation(summary = "createAlertPluginInstance", description = "CREATE_ALERT_PLUGIN_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "pluginDefineId", description = "ALERT_PLUGIN_DEFINE_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "instanceName", description = "ALERT_PLUGIN_INSTANCE_NAME", required = true, schema = @Schema(implementation = String.class, example = "DING TALK")),
            @Parameter(name = "pluginInstanceParams", description = "ALERT_PLUGIN_INSTANCE_PARAMS", required = true, schema = @Schema(implementation = String.class, example = "ALERT_PLUGIN_INSTANCE_PARAMS"))
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @PluginApiException(CREATE_ALERT_PLUGIN_INSTANCE_ERROR)
    public Result<AlertPluginInstance> createAlertPluginInstance(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                 @RequestParam(value = "pluginDefineId") int pluginDefineId,
                                                                 @RequestParam(value = "instanceName") String instanceName,
                                                                 @RequestParam(value = "instanceType") AlertPluginInstanceType instanceType,
                                                                 @RequestParam(value = "warningType") WarningType warningType,
                                                                 @RequestParam(value = "pluginInstanceParams") String pluginInstanceParams) {
        AlertPluginInstance alertPluginInstance = alertPluginInstanceService.create(loginUser, pluginDefineId,
                instanceName, instanceType, warningType, pluginInstanceParams);
        return Result.success(alertPluginInstance);
    }

    @Operation(summary = "testSendAlertPluginInstance", description = "TEST_SEND_ALERT_PLUGIN_INSTANCE")
    @Parameters({
            @Parameter(name = "pluginDefineId", description = "ALERT_PLUGIN_DEFINE_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "pluginInstanceParams", description = "ALERT_PLUGIN_INSTANCE_PARAMS", required = true, schema = @Schema(implementation = String.class, example = "ALERT_PLUGIN_INSTANCE_PARAMS"))
    })
    @PostMapping(value = "/test-send")
    @ResponseStatus(HttpStatus.OK)
    @PluginApiException(SEND_TEST_ALERT_PLUGIN_INSTANCE_ERROR)
    public Result<Boolean> testSendAlertPluginInstance(@RequestParam(value = "pluginDefineId") int pluginDefineId,
                                                       @RequestParam(value = "pluginInstanceParams") String pluginInstanceParams) {
        alertPluginInstanceService.testSend(pluginDefineId, pluginInstanceParams);
        return Result.success(true);
    }

    /**
     * updateAlertPluginInstance
     *
     * @param loginUser login user
     * @param id alert plugin instance id
     * @param instanceName instance name
     * @param pluginInstanceParams instance params
     * @return result
     */
    @Operation(summary = "updateAlertPluginInstance", description = "UPDATE_ALERT_PLUGIN_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "alertPluginInstanceId", description = "ALERT_PLUGIN_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "instanceName", description = "ALERT_PLUGIN_INSTANCE_NAME", required = true, schema = @Schema(implementation = String.class, example = "DING TALK")),
            @Parameter(name = "pluginInstanceParams", description = "ALERT_PLUGIN_INSTANCE_PARAMS", required = true, schema = @Schema(implementation = String.class, example = "ALERT_PLUGIN_INSTANCE_PARAMS"))
    })
    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PluginApiException(UPDATE_ALERT_PLUGIN_INSTANCE_ERROR)
    public Result<AlertPluginInstance> updateAlertPluginInstanceById(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                     @PathVariable(value = "id") int id,
                                                                     @RequestParam(value = "instanceName") String instanceName,
                                                                     @RequestParam(value = "warningType") WarningType warningType,
                                                                     @RequestParam(value = "pluginInstanceParams") String pluginInstanceParams) {
        AlertPluginInstance alertPluginInstance =
                alertPluginInstanceService.updateById(loginUser, id, instanceName, warningType, pluginInstanceParams);
        return Result.success(alertPluginInstance);
    }

    /**
     * deleteAlertPluginInstance
     *
     * @param loginUser login user
     * @param id id
     * @return result
     */
    @Operation(summary = "deleteAlertPluginInstance", description = "DELETE_ALERT_PLUGIN_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "ALERT_PLUGIN_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PluginApiException(DELETE_ALERT_PLUGIN_INSTANCE_ERROR)
    public Result<Boolean> deleteAlertPluginInstance(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                     @PathVariable(value = "id") int id) {

        alertPluginInstanceService.deleteById(loginUser, id);
        return Result.success(true);
    }

    /**
     * getAlertPluginInstance
     *
     * @param loginUser login user
     * @param id alert plugin instance id
     * @return result
     */
    @Operation(summary = "getAlertPluginInstance", description = "GET_ALERT_PLUGIN_INSTANCE_NOTES")
    @GetMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PluginApiException(GET_ALERT_PLUGIN_INSTANCE_ERROR)
    public Result<AlertPluginInstance> getAlertPluginInstance(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                              @PathVariable(value = "id") int id) {
        AlertPluginInstance alertPluginInstance = alertPluginInstanceService.getById(loginUser, id);
        return Result.success(alertPluginInstance);
    }

    /**
     * getAlertPluginInstance
     *
     * @param loginUser login user
     * @return result
     */
    @Operation(summary = "queryAlertPluginInstanceList", description = "QUERY_ALL_ALERT_PLUGIN_INSTANCE_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @PluginApiException(QUERY_ALL_ALERT_PLUGIN_INSTANCE_ERROR)
    public Result<List<AlertPluginInstanceVO>> getAlertPluginInstance(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        List<AlertPluginInstanceVO> alertPluginInstanceVOS = alertPluginInstanceService.queryAll();
        return Result.success(alertPluginInstanceVOS);
    }

    /**
     * check alert group exist
     *
     * @param loginUser login user
     * @param alertInstanceName alert instance name
     * @return check result code
     */
    @Operation(summary = "verifyAlertInstanceName", description = "VERIFY_ALERT_INSTANCE_NAME_NOTES")
    @Parameters({
            @Parameter(name = "alertInstanceName", description = "ALERT_INSTANCE_NAME", required = true, schema = @Schema(implementation = String.class)),
    })
    @GetMapping(value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    public Result verifyGroupName(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam(value = "alertInstanceName") String alertInstanceName) {

        boolean exist = alertPluginInstanceService.checkExistPluginInstanceName(alertInstanceName);
        if (exist) {
            log.error("alert plugin instance {} has exist, can't create again.", alertInstanceName);
            return Result.error(Status.PLUGIN_INSTANCE_ALREADY_EXISTS);
        } else {
            return Result.success();
        }
    }

    /**
     * paging query alert plugin instance group list
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return alert plugin instance list page
     */
    @Operation(summary = "queryAlertPluginInstanceListPaging", description = "QUERY_ALERT_PLUGIN_INSTANCE_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "20"))
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @PluginApiException(LIST_PAGING_ALERT_PLUGIN_INSTANCE_ERROR)
    public Result<PageInfo<AlertPluginInstanceVO>> listPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                              @RequestParam(value = "searchVal", required = false) String searchVal,
                                                              @RequestParam("pageNo") Integer pageNo,
                                                              @RequestParam("pageSize") Integer pageSize) {
        checkPageParams(pageNo, pageSize);
        searchVal = ParameterUtils.handleEscapes(searchVal);
        PageInfo<AlertPluginInstanceVO> alertPluginInstanceVOPageInfo =
                alertPluginInstanceService.listPaging(loginUser, searchVal, pageNo, pageSize);
        return Result.success(alertPluginInstanceVOPageInfo);
    }

}
