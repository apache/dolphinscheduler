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

import static org.apache.dolphinscheduler.api.enums.Status.QUERY_WORKFLOW_LINEAGE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.TASK_WITH_DEPENDENT_ERROR;
import static org.apache.dolphinscheduler.common.Constants.SESSION_USER;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.WorkFlowLineageService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;

import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * work flow lineage controller
 */
@Api(tags = "WORK_FLOW_LINEAGE_TAG")
@RestController
@RequestMapping("projects/{projectCode}/lineages")
public class WorkFlowLineageController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(WorkFlowLineageController.class);

    @Autowired
    private WorkFlowLineageService workFlowLineageService;

    @ApiOperation(value = "queryLineageByWorkFlowName", notes = "QUERY_WORKFLOW_LINEAGE_BY_NAME_NOTES")
    @GetMapping(value = "/query-by-name")
    @ResponseStatus(HttpStatus.OK)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<List<WorkFlowLineage>> queryWorkFlowLineageByName(@ApiIgnore @RequestAttribute(value = SESSION_USER) User loginUser,
                                                                    @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                    @RequestParam(value = "workFlowName", required = false) String workFlowName) {
        try {
            workFlowName = ParameterUtils.handleEscapes(workFlowName);
            Map<String, Object> result = workFlowLineageService.queryWorkFlowLineageByName(projectCode, workFlowName);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(QUERY_WORKFLOW_LINEAGE_ERROR.getMsg(), e);
            return error(QUERY_WORKFLOW_LINEAGE_ERROR.getCode(), QUERY_WORKFLOW_LINEAGE_ERROR.getMsg());
        }
    }

    @ApiOperation(value = "queryLineageByWorkFlowCode", notes = "QUERY_WORKFLOW_LINEAGE_BY_CODES_NOTES")
    @GetMapping(value = "/{workFlowCode}")
    @ResponseStatus(HttpStatus.OK)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Map<String, Object>> queryWorkFlowLineageByCode(@ApiIgnore @RequestAttribute(value = SESSION_USER) User loginUser,
                                                                  @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                                  @PathVariable(value = "workFlowCode", required = true) long workFlowCode) {
        try {
            Map<String, Object> result = workFlowLineageService.queryWorkFlowLineageByCode(projectCode, workFlowCode);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(QUERY_WORKFLOW_LINEAGE_ERROR.getMsg(), e);
            return error(QUERY_WORKFLOW_LINEAGE_ERROR.getCode(), QUERY_WORKFLOW_LINEAGE_ERROR.getMsg());
        }
    }

    @ApiOperation(value = "queryWorkFlowList", notes = "QUERY_WORKFLOW_LINEAGE_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Map<String, Object>> queryWorkFlowLineage(@ApiIgnore @RequestAttribute(value = SESSION_USER) User loginUser,
                                                            @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode) {
        try {
            Map<String, Object> result = workFlowLineageService.queryWorkFlowLineage(projectCode);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(QUERY_WORKFLOW_LINEAGE_ERROR.getMsg(), e);
            return error(QUERY_WORKFLOW_LINEAGE_ERROR.getCode(), QUERY_WORKFLOW_LINEAGE_ERROR.getMsg());
        }
    }

    /**
     * Whether task can be deleted or not, avoiding task depend on other task of process definition delete by accident.
     *
     * @param loginUser login user
     * @param projectCode project codes which taskCode belong
     * @param processDefinitionCode project code which taskCode belong
     * @param taskCode task definition code
     * @return Result of task can be delete or not
     */
    @ApiOperation(value = "verifyTaskCanDelete", notes = "VERIFY_TASK_CAN_DELETE")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectCode", value = "PROCESS_DEFINITION_NAME", required = true, dataTypeClass = long.class),
            @ApiImplicitParam(name = "processDefinitionCode", value = "PROCESS_DEFINITION_CODE", required = true, dataTypeClass = long.class),
            @ApiImplicitParam(name = "taskCode", value = "TASK_DEFINITION_CODE", required = true, dataTypeClass = long.class, example = "123456789"),
    })
    @PostMapping(value = "/tasks/verify-delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(TASK_WITH_DEPENDENT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result verifyTaskCanDelete(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                      @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                      @RequestParam(value = "processDefinitionCode", required = true) long processDefinitionCode,
                                      @RequestParam(value = "taskCode", required = true) long taskCode) {
        Result result = new Result();
        Optional<String> taskDepMsg =
                workFlowLineageService.taskDepOnTaskMsg(projectCode, processDefinitionCode, taskCode);
        if (taskDepMsg.isPresent()) {
            throw new ServiceException(taskDepMsg.get());
        }
        putMsg(result, Status.SUCCESS);
        return result;
    }
}
