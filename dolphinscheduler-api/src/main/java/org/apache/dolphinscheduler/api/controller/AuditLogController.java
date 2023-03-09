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

import static org.apache.dolphinscheduler.api.enums.Status.QUERY_AUDIT_LOG_LIST_PAGING;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.AuditService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuditOperationType;
import org.apache.dolphinscheduler.common.enums.AuditResourceType;
import org.apache.dolphinscheduler.dao.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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

@Tag(name = "AUDIT_LOG_TAG")
@RestController
@RequestMapping("projects/audit")
public class AuditLogController extends BaseController {

    @Autowired
    AuditService auditService;

    /**
     * query audit log list paging
     *
     * @param loginUser         login user
     * @param pageNo            page number
     * @param resourceType     resource type
     * @param operationType     operation type
     * @param startDate         start time
     * @param endDate           end time
     * @param userName          user name
     * @param pageSize          page size
     * @return      audit log content
     */
    @Operation(summary = "queryAuditLogListPaging", description = "QUERY_AUDIT_LOG")
    @Parameters({
            @Parameter(name = "startDate", description = "START_DATE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "endDate", description = "END_DATE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "resourceType", description = "RESOURCE_TYPE", schema = @Schema(implementation = AuditResourceType.class)),
            @Parameter(name = "operationType", description = "OPERATION_TYPE", schema = @Schema(implementation = AuditOperationType.class)),
            @Parameter(name = "userName", description = "USER_NAME", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "20"))
    })
    @GetMapping(value = "/audit-log-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_AUDIT_LOG_LIST_PAGING)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAuditLogListPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @RequestParam("pageNo") Integer pageNo,
                                          @RequestParam("pageSize") Integer pageSize,
                                          @RequestParam(value = "resourceType", required = false) AuditResourceType resourceType,
                                          @RequestParam(value = "operationType", required = false) AuditOperationType operationType,
                                          @RequestParam(value = "startDate", required = false) String startDate,
                                          @RequestParam(value = "endDate", required = false) String endDate,
                                          @RequestParam(value = "userName", required = false) String userName) {
        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        result = auditService.queryLogListPaging(loginUser, resourceType, operationType, startDate, endDate, userName,
                pageNo, pageSize);
        return result;
    }
}
