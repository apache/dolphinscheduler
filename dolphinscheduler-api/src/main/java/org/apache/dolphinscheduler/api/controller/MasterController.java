package org.apache.dolphinscheduler.api.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.MasterRestfulService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.remote.dto.MasterTaskInstanceDispatchingDto;
import org.apache.dolphinscheduler.remote.dto.MasterWorkflowInstanceExecutingListingDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.apache.dolphinscheduler.api.enums.Status.QUERY_EXECUTING_WORKFLOW_ERROR;

@Api(tags = "MASTER_TAG")
@RestController
@RequestMapping("/monitor/master")
public class MasterController {

    @Autowired
    private MasterRestfulService masterRestfulService;

    @ApiOperation(value = "listingExecutingWorkflows", notes = "LISTING_EXECUTING_WORKFLOWS_DATA")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "masterAddress", value = "MASTER_ADDRESS", required = true, dataTypeClass = String.class, example = "127.0.0.1:5679")
    })
    @GetMapping(value = "/{masterAddress}/listingExecutingWorkflows")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_EXECUTING_WORKFLOW_ERROR)
    public Result<List<MasterWorkflowInstanceExecutingListingDto>> listingExecutingWorkflowsByMasterAddress(@PathVariable("masterAddress") String masterAddress) {
        List<MasterWorkflowInstanceExecutingListingDto> masterWorkflowInstanceExecutingListingDtos =
                masterRestfulService.listingExecutingWorkflowsByMasterAddress(masterAddress);
        return Result.success(masterWorkflowInstanceExecutingListingDtos);
    }

    @ApiOperation(value = "listingDispatchingTaskInstances", notes = "LISTING_DISPATCHING_TASK_INSTANCES_DATA")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "masterAddress", value = "MASTER_ADDRESS", required = true, dataTypeClass = String.class, example = "127.0.0.1:5679")
    })
    @GetMapping(value = "/{masterAddress}/listingDispatchingTasks")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_EXECUTING_WORKFLOW_ERROR)
    public Result<List<MasterTaskInstanceDispatchingDto>> listingTaskInstanceDispatchingDtoByMasterAddress(@PathVariable("masterAddress") String masterAddress) {
        List<MasterTaskInstanceDispatchingDto> masterTaskInstanceDispatchingDtos =
                masterRestfulService.listingDispatchingTaskInstanceByMasterAddress(masterAddress);
        return Result.success(masterTaskInstanceDispatchingDtos);
    }

}
