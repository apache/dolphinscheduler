package org.apache.dolphinscheduler.api.controller;

import com.azure.resourcemanager.datafactory.models.Factory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.dto.FavTaskDto;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.CloudService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static org.apache.dolphinscheduler.api.enums.Status.LIST_TASK_TYPE_ERROR;

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
    @ApiException(LIST_TASK_TYPE_ERROR)
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
    @ApiException(LIST_TASK_TYPE_ERROR)
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
    @ApiException(LIST_TASK_TYPE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result listPipeline(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                               @RequestParam("factoryName") String factoryName,
                               @RequestParam("resourceGroupName") String resourceGroupName) {
        List<String> pipelineNames = cloudService.listPipeline(loginUser, factoryName, resourceGroupName);
        return success(Status.SUCCESS.getMsg(), pipelineNames);
    }
}
