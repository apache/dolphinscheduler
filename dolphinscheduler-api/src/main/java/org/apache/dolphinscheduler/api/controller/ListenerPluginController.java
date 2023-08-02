package org.apache.dolphinscheduler.api.controller;

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_ALERT_PLUGIN_INSTANCE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ListenerPluginService;
import org.apache.dolphinscheduler.api.utils.Result;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * @author wxn
 * @date 2023/8/1
 */
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
    @ApiException(CREATE_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result registerPlugin(@RequestParam("pluginJar") MultipartFile file,
                                 @RequestParam("classPath") String classPath) {
        return listenerPluginService.registerListenerPlugin(file, classPath);
    }

    @Operation(summary = "updateListenerPlugin", description = "UPDATE_LISTENER_PLUGIN_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "PLUGIN_DEFINED_ID", required = true, schema = @Schema(implementation = Integer.class, example = "1")),
            @Parameter(name = "pluginJar", description = "PLUGIN_JAR_FILE", required = true, schema = @Schema(implementation = MultipartFile.class)),
            @Parameter(name = "classPath", description = "PLUGIN_CLASS_PATH", required = true, schema = @Schema(implementation = String.class, example = "com.example.ListenerPlugin"))
    })
    @PutMapping("/plugin/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(CREATE_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updatePlugin(@PathVariable("id") int id,
                               @RequestParam("pluginJar") MultipartFile file,
                               @RequestParam("classPath") String classPath) {
        return listenerPluginService.updateListenerPlugin(id, file, classPath);
    }

    @Operation(summary = "REMOVEListenerPlugin", description = "REMOVE_LISTENER_PLUGIN_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "PLUGIN_DEFINED_ID", required = true, schema = @Schema(implementation = Integer.class, example = "1"))
    })
    @DeleteMapping("/plugin/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(CREATE_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result removePlugin(@PathVariable("id") int id) {
        return listenerPluginService.removeListenerPlugin(id);
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
    @ApiException(CREATE_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createPluginInstance(@RequestParam(value = "pluginDefineId") int pluginDefineId,
                                       @RequestParam(value = "instanceName") String instanceName,
                                       @RequestParam(value = "instanceParams") String pluginInstanceParams,
                                       @RequestParam(value = "listenEventTypes") List<Integer> listenerEventTypes) {
        return listenerPluginService.createListenerInstance(pluginDefineId, instanceName, pluginInstanceParams, listenerEventTypes);
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
    @ApiException(CREATE_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updatePluginInstance(@PathVariable(value = "id") int id,
                                       @RequestParam(value = "instanceName") String instanceName,
                                       @RequestParam(value = "instanceParams") String pluginInstanceParams,
                                       @RequestParam(value = "listenEventTypes") List<Integer> listenerEventTypes) {
        return listenerPluginService.updateListenerInstance(id, instanceName, pluginInstanceParams, listenerEventTypes);
    }

    @Operation(summary = "removeListenerPluginInstance", description = "REMOVE_LISTENER_PLUGIN_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "PLUGIN_INSTANCE_ID", required = true, schema = @Schema(implementation = Integer.class, example = "1"))
    })
    @DeleteMapping("/instance/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(CREATE_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result removePluginInstance(@PathVariable(value = "id") int id) {
        return listenerPluginService.removeListenerInstance(id);
    }
}
