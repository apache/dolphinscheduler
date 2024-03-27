package org.apache.dolphinscheduler.api.controller.v2;

import org.apache.dolphinscheduler.api.controller.BaseController;
import org.apache.dolphinscheduler.api.dto.schedule.ScheduleCreateRequest;
import org.apache.dolphinscheduler.api.dto.schedule.ScheduleFilterRequest;
import org.apache.dolphinscheduler.api.dto.schedule.ScheduleUpdateRequest;
import org.apache.dolphinscheduler.api.dto.taskInstance.TaskInstanceQueryRequest;
import org.apache.dolphinscheduler.api.dto.taskInstance.TaskInstanceSuccessResponse;
import org.apache.dolphinscheduler.api.dto.trigger.TriggerCreateRequest;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.SchedulerService;
import org.apache.dolphinscheduler.api.service.TaskInstanceService;
import org.apache.dolphinscheduler.api.service.TriggerService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import static org.apache.dolphinscheduler.api.enums.Status.*;

/**
 * Trigger instance controller
 */
@Tag(name = "TRIGGER_TAG_V2")
@RestController
@RequestMapping("/v2/trigger")
public class TriggerV2Controller extends BaseController {

    @Autowired
    private TriggerService triggerService;

    /**
     * Create resource trigger
     *
     * @param loginUser             login user
     * @param scheduleCreateRequest the new schedule object will be created
     * @return ResourceResponse object created
     */
    @Operation(summary = "create", description = "CREATE_TRIGGER_NOTES")
    @PostMapping(consumes = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_SCHEDULE_ERROR)
    public Result<Schedule> createTrigger(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @RequestBody TriggerCreateRequest scheduleCreateRequest) {

    }


}
