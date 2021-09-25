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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.common.enums.AuditModuleType;
import org.apache.dolphinscheduler.common.enums.AuditOperationType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.AuditService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

import static org.apache.dolphinscheduler.api.enums.Status.QUERY_AUDIT_LOG_LIST_PAGING;

@Api(tags = "AUDIT_LOG_TAG")
@RestController
@RequestMapping("projects/audit")
public class AuditLogController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogController.class);

    @Autowired
    AuditService auditService;

    /**
     * query audit log list paging
     *
     * @param loginUser         login user
     * @param pageNo            page number
     * @param moduleType        module type
     * @param operationType     operation type
     * @param startDate         start time
     * @param endDate           end time
     * @param userName          user name
     * @param pageSize          page size
     * @return      audit log content
     */
    @ApiOperation(value = "queryAuditLogListPaging", notes = "QUERY_AUDIT_LOG")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "START_DATE", type = "String"),
            @ApiImplicitParam(name = "endDate", value = "END_DATE", type = "String"),
            @ApiImplicitParam(name = "moduleType", value = "MODULE_TYPE", type = "String"),
            @ApiImplicitParam(name = "operationType", value = "OPERATION_TYPE", type = "String"),
            @ApiImplicitParam(name = "userName", value = "USER_NAME", type = "String"),
            @ApiImplicitParam(name = "projectName", value = "PROJECT_NAME", type = "String"),
            @ApiImplicitParam(name = "processName", value = "PROCESS_NAME", type = "String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataType = "Int", example = "20")
    })
    @GetMapping(value = "/audit-log-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_AUDIT_LOG_LIST_PAGING)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAuditLogListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @RequestParam("pageNo") Integer pageNo,
                                          @RequestParam("pageSize") Integer pageSize,
                                          @RequestParam(value = "moduleType", required = false) AuditModuleType moduleType,
                                          @RequestParam(value = "operationType", required = false) AuditOperationType operationType,
                                          @RequestParam(value = "startDate", required = false) String startDate,
                                          @RequestParam(value = "endDate", required = false) String endDate,
                                          @RequestParam(value = "userName", required = false) String userName,
                                          @RequestParam(value = "projectName", required = false) String projectName,
                                          @RequestParam(value = "processName", required = false) String processName) {
        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        result = auditService.queryLogListPaging(loginUser, moduleType, operationType, startDate,
                                                endDate, userName, projectName, processName, pageNo, pageSize);
        return result;
    }
}
