/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.api.controller;

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_LISTENER_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_LISTENER_PLUGIN_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_LISTENER_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.LIST_LISTENER_PLUGIN_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.LIST_PAGING_LISTENER_PLUGIN_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.LIST_PAGING_LISTENER_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.REGISTER_LISTENER_PLUGIN_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_LISTENER_PLUGIN_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_LISTENER_PLUGIN_INSTANCE_ERROR;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ListenerPluginService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.listener.enums.ListenerEventType;
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
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "LISTENER_PLUGIN_TAG")
@RestController
@RequestMapping("listener")
@Slf4j
public class ListenerPluginController extends BaseController {

    @Autowired
    private ListenerPluginService listenerPluginService;

    @Operation(summary = "registerListenerPlugin", description = "REGISTER_LISTENER_PLUGIN_NOTES")
    @Parameters({
            @Parameter(name = "pluginJar", description = "PLUGIN_JAR_FILE", required = true, schema = @Schema(implementation = MultipartFile.class)),
            @Parameter(name = "classPath", description = "PLUGIN_CLASS_PATH", required = true, schema = @Schema(implementation = String.class, example = "com.example.ListenerPlugin"))
    })
    @PostMapping("/plugin")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(REGISTER_LISTENER_PLUGIN_ERROR)
    public Result registerPlugin(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam("pluginJar") MultipartFile file,
                                 @RequestParam("classPath") String classPath) {
        return listenerPluginService.registerListenerPlugin(loginUser, file, classPath);
    }

    @Operation(summary = "updateListenerPlugin", description = "UPDATE_LISTENER_PLUGIN_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "PLUGIN_DEFINED_ID", required = true, schema = @Schema(implementation = Integer.class, example = "1")),
            @Parameter(name = "pluginJar", description = "PLUGIN_JAR_FILE", required = true, schema = @Schema(implementation = MultipartFile.class)),
            @Parameter(name = "classPath", description = "PLUGIN_CLASS_PATH", required = true, schema = @Schema(implementation = String.class, example = "com.example.ListenerPlugin"))
    })
    @PutMapping("/plugin/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_LISTENER_PLUGIN_ERROR)
    public Result updatePlugin(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                               @PathVariable("id") int id,
                               @RequestParam("pluginJar") MultipartFile file,
                               @RequestParam("classPath") String classPath) {
        return listenerPluginService.updateListenerPlugin(loginUser, id, file, classPath);
    }

    @Operation(summary = "removeListenerPlugin", description = "REMOVE_LISTENER_PLUGIN_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "PLUGIN_DEFINED_ID", required = true, schema = @Schema(implementation = Integer.class, example = "1"))
    })
    @DeleteMapping("/plugin/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_LISTENER_PLUGIN_ERROR)
    public Result removePlugin(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                               @PathVariable("id") int id) {
        return listenerPluginService.removeListenerPlugin(loginUser, id);
    }

    @Operation(summary = "queryListenerPluginPaging", description = "QUERY_LISTENER_PLUGIN_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "20"))
    })
    @GetMapping("/plugin")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LIST_PAGING_LISTENER_PLUGIN_ERROR)
    public Result listListenerPluginPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @RequestParam(value = "searchVal", required = false) String searchVal,
                                           @RequestParam("pageNo") Integer pageNo,
                                           @RequestParam("pageSize") Integer pageSize) {
        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        return listenerPluginService.listPluginPaging(searchVal, pageNo, pageSize);
    }

    @Operation(summary = "listListenerPlugin", description = "LIST_LISTENER_PLUGINNOTES")
    @GetMapping("/plugin/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LIST_LISTENER_PLUGIN_ERROR)
    public Result listListenerPluginPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        return listenerPluginService.listPluginList();
    }

    @Operation(summary = "verifyListenerInstanceName", description = "VERIFY_LISTENER_INSTANCE_NAME_NOTES")
    @Parameters({
            @Parameter(name = "listenerInstanceName", description = "LISTENER_INSTANCE_NAME", required = true, schema = @Schema(implementation = String.class)),
    })
    @GetMapping(value = "/instance/verify-name")
    @ResponseStatus(HttpStatus.OK)
    public Result verifyGroupName(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam(value = "instanceName") String instanceName) {

        boolean exist = listenerPluginService.checkExistPluginInstanceName(instanceName);
        if (exist) {
            log.error("listener plugin instance {} has exist, can't create again.", instanceName);
            return Result.error(Status.PLUGIN_INSTANCE_ALREADY_EXISTS);
        } else {
            return Result.success();
        }
    }

    @Operation(summary = "createListenerPluginInstance", description = "CREATE_LISTENER_PLUGIN_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "pluginDefineId", description = "PLUGIN_DEFINED_ID", required = true, schema = @Schema(implementation = Integer.class, example = "1")),
            @Parameter(name = "instanceName", description = "PLUGIN_INSTANCE_NAME", required = true, schema = @Schema(implementation = String.class, example = "testListener")),
            @Parameter(name = "InstanceParams", description = "PLUGIN_INSTANCE_PARAMS", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "listenEventTypes", description = "INSTANCE_LISTEN_EVENT_TYPES", required = true, schema = @Schema(implementation = List.class))

    })
    @PostMapping("/instance")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_LISTENER_PLUGIN_INSTANCE_ERROR)
    public Result createPluginInstance(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam(value = "pluginDefineId") int pluginDefineId,
                                       @RequestParam(value = "instanceName") String instanceName,
                                       @RequestParam(value = "instanceParams") String pluginInstanceParams,
                                       @RequestParam(value = "listenerEventTypes") List<ListenerEventType> listenerEventTypes) {
        return listenerPluginService.createListenerInstance(loginUser, pluginDefineId, instanceName,
                pluginInstanceParams,
                listenerEventTypes);
    }

    @Operation(summary = "updateListenerPluginInstance", description = "UPDATE_LISTENER_PLUGIN_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "PLUGIN_INSTANCE_ID", required = true, schema = @Schema(implementation = Integer.class, example = "1")),
            @Parameter(name = "instanceName", description = "PLUGIN_INSTANCE_NAME", required = true, schema = @Schema(implementation = String.class, example = "testListener")),
            @Parameter(name = "instanceParams", description = "PLUGIN_INSTANCE_PARAMS", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "listenEventTypes", description = "INSTANCE_LISTEN_EVENT_TYPES", required = true, schema = @Schema(implementation = List.class))

    })
    @PutMapping("/instance/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_LISTENER_PLUGIN_INSTANCE_ERROR)
    public Result updatePluginInstance(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @PathVariable(value = "id") int id,
                                       @RequestParam(value = "instanceName") String instanceName,
                                       @RequestParam(value = "instanceParams") String pluginInstanceParams,
                                       @RequestParam(value = "listenerEventTypes") List<ListenerEventType> listenerEventTypes) {
        return listenerPluginService.updateListenerInstance(loginUser, id, instanceName, pluginInstanceParams,
                listenerEventTypes);
    }

    @Operation(summary = "removeListenerPluginInstance", description = "REMOVE_LISTENER_PLUGIN_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "PLUGIN_INSTANCE_ID", required = true, schema = @Schema(implementation = Integer.class, example = "1"))
    })
    @DeleteMapping("/instance/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_LISTENER_PLUGIN_INSTANCE_ERROR)
    public Result removePluginInstance(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @PathVariable(value = "id") int id) {
        return listenerPluginService.removeListenerInstance(loginUser, id);
    }

    @Operation(summary = "queryListenerPluginInstancePaging", description = "QUERY_LISTENER_PLUGIN_INSTANCE_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "20"))
    })
    @GetMapping("/instance")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LIST_PAGING_LISTENER_PLUGIN_INSTANCE_ERROR)
    public Result listListenerInstancePaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @RequestParam(value = "searchVal", required = false) String searchVal,
                                             @RequestParam("pageNo") Integer pageNo,
                                             @RequestParam("pageSize") Integer pageSize) {
        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        return listenerPluginService.listInstancePaging(searchVal, pageNo, pageSize);
    }
}
