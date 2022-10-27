package org.apache.dolphinscheduler.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.dto.FavTaskDto;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static org.apache.dolphinscheduler.api.enums.Status.LIST_TASK_TYPE_ERROR;

/**
 * dynamic task type controller
 */
@Tag(name = "DYNAMIC_TASK_TYPE")
@RestController
@RequestMapping("/dynamic")
public class DynamicTaskTypeController extends BaseController {

    @Resource


    /**
     * get dynamic task category list
     *
     * @param loginUser login user
     * @return dynamic task category list
     */
    @Operation(summary = "listTaskCates", description = "QUERY_TASK_TYPE_CATES")
    @GetMapping(value = "/taskTypes")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LIST_TASK_TYPE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result listDynamicTaskCategories(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        List<FavTaskDto> favTaskList = favTaskService.getFavTaskList(loginUser);
        return success(Status.SUCCESS.getMsg(), favTaskList);
    }

}
