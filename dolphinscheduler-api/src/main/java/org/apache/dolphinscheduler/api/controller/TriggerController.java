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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.api.audit.OperatorLog;
import org.apache.dolphinscheduler.api.audit.enums.AuditType;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.TriggerDefinitionService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.*;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static org.apache.dolphinscheduler.api.enums.Status.*;

/**
 * trigger definition controller
 */
@Tag(name = "TRIGGER_DEFINITION_TAG")
@RestController
@RequestMapping("projects/{projectCode}/trigger-definition")
@Slf4j
public class TriggerController extends BaseController {

    @Autowired
    private ExecutorService execService;

    @Autowired
    private TriggerDefinitionService triggerDefinitionService;

    /**
     * create trigger definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param triggerDefinitionJson trigger definition json
     * @return create result code
     */
    @Operation(summary = "save", description = "CREATE_TRIGGER_DEFINITION_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true, schema = @Schema(implementation = long.class)),
            @Parameter(name = "triggerDefinitionJson", description = "TRIGGER_DEFINITION_JSON", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    // change error code
    @ApiException(CREATE_TASK_DEFINITION_ERROR)
    // change audit type
    @OperatorLog(auditType = AuditType.TASK_CREATE)
    public Result createTriggerDefinition(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                       @RequestParam(value = "triggerDefinitionJson", required = true) String triggerDefinitionJson) {
        Map<String, Object> result =
                triggerDefinitionService.createTriggerDefinition(loginUser, projectCode, triggerDefinitionJson);
        return returnDataList(result);
    }

    /**
     * query task definition list paging
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param searchTriggerName searchTaskName
     * @param triggerType taskType
     * @param pageNo page number
     * @param pageSize page size
     * @return task definition page
     */
    @Operation(summary = "queryTaskDefinitionListPaging", description = "QUERY_TASK_DEFINITION_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = false, schema = @Schema(implementation = long.class)),
            @Parameter(name = "searchWorkflowName", description = "SEARCH_WORKFLOW_NAME", required = false, schema = @Schema(implementation = String.class)),
            @Parameter(name = "searchTaskName", description = "SEARCH_TASK_NAME", required = false, schema = @Schema(implementation = String.class)),
            @Parameter(name = "taskType", description = "TASK_TYPE", required = false, schema = @Schema(implementation = String.class, example = "SHELL")),
            @Parameter(name = "taskExecuteType", description = "TASK_EXECUTE_TYPE", required = false, schema = @Schema(implementation = TaskExecuteType.class, example = "STREAM")),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "10"))
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_DEFINITION_LIST_PAGING_ERROR)
    public Result queryTaskDefinitionListPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                @RequestParam(value = "searchTriggerName", required = false) String searchTriggerName,
                                                @RequestParam(value = "triggerType", required = false) String triggerType,
                                                @RequestParam("pageNo") Integer pageNo,
                                                @RequestParam("pageSize") Integer pageSize) {
        checkPageParams(pageNo, pageSize);
        searchTriggerName = ParameterUtils.handleEscapes(searchTriggerName);
        return triggerDefinitionService.queryTriggerDefinitionListPaging(loginUser, projectCode,
                searchTriggerName, triggerType, pageNo, pageSize);
    }
}

