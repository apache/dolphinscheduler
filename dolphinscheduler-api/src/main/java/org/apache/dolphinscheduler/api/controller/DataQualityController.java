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

import static org.apache.dolphinscheduler.api.enums.Status.GET_DATASOURCE_OPTIONS_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GET_RULE_FORM_CREATE_JSON_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_EXECUTE_RESULT_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_RULE_LIST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_RULE_LIST_PAGING_ERROR;

import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.DqExecuteResultService;
import org.apache.dolphinscheduler.api.service.DqRuleService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import java.util.Map;

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

/**
 * data quality controller
 */
@Tag(name = "DATA_QUALITY_TAG")
@RestController
@RequestMapping("/data-quality")
public class DataQualityController extends BaseController {

    @Autowired
    private DqRuleService dqRuleService;

    @Autowired
    private DqExecuteResultService dqExecuteResultService;

    /**
     * get rule from-create json
     * @param ruleId ruleId
     * @return from-create json
     */
    @Operation(summary = "getRuleFormCreateJson", description = "GET_RULE_FORM_CREATE_JSON_NOTES")
    @Parameters({
            @Parameter(name = "ruleId", description = "RULE_ID", schema = @Schema(implementation = int.class, example = "1"))
    })
    @GetMapping(value = "/getRuleFormCreateJson")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_RULE_FORM_CREATE_JSON_ERROR)
    public Result getRuleFormCreateJsonById(@RequestParam(value = "ruleId") int ruleId) {
        Map<String, Object> result = dqRuleService.getRuleFormCreateJsonById(ruleId);
        return returnDataList(result);
    }

    /**
     * query rule list paging
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return rule page
     */
    @Operation(summary = "queryRuleListPaging", description = "QUERY_RULE_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "ruleType", description = "RULE_TYPE", schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "startDate", description = "START_DATE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "endDate", description = "END_DATE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageNo", description = "PAGE_NO", schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", schema = @Schema(implementation = int.class, example = "10"))
    })
    @GetMapping(value = "/rule/page")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_RULE_LIST_PAGING_ERROR)
    public Result queryRuleListPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                      @RequestParam(value = "searchVal", required = false) String searchVal,
                                      @RequestParam(value = "ruleType", required = false) Integer ruleType,
                                      @RequestParam(value = "startDate", required = false) String startTime,
                                      @RequestParam(value = "endDate", required = false) String endTime,
                                      @RequestParam("pageNo") Integer pageNo,
                                      @RequestParam("pageSize") Integer pageSize) {
        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);

        return dqRuleService.queryRuleListPaging(loginUser, searchVal, ruleType, startTime, endTime, pageNo, pageSize);
    }

    /**
     * query all rule list
     * @return rule list
     */
    @Operation(summary = "queryRuleList", description = "QUERY_RULE_LIST_NOTES")
    @GetMapping(value = "/ruleList")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_RULE_LIST_ERROR)
    public Result queryRuleList() {
        Map<String, Object> result = dqRuleService.queryAllRuleList();
        return returnDataList(result);
    }

    /**
     * query task execute result list paging
     *
     * @param loginUser loginUser
     * @param searchVal searchVal
     * @param ruleType ruleType
     * @param state state
     * @param startTime startTime
     * @param endTime endTime
     * @param pageNo pageNo
     * @param pageSize pageSize
     * @return
     */
    @Operation(summary = "queryExecuteResultListPaging", description = "QUERY_EXECUTE_RESULT_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "ruleType", description = "RULE_TYPE", schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "state", description = "STATE", schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "startDate", description = "START_DATE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "endDate", description = "END_DATE", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageNo", description = "PAGE_NO", schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", schema = @Schema(implementation = int.class, example = "10"))
    })
    @GetMapping(value = "/result/page")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_EXECUTE_RESULT_LIST_PAGING_ERROR)
    public Result queryExecuteResultListPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @RequestParam(value = "searchVal", required = false) String searchVal,
                                               @RequestParam(value = "ruleType", required = false) Integer ruleType,
                                               @RequestParam(value = "state", required = false) Integer state,
                                               @RequestParam(value = "startDate", required = false) String startTime,
                                               @RequestParam(value = "endDate", required = false) String endTime,
                                               @RequestParam("pageNo") Integer pageNo,
                                               @RequestParam("pageSize") Integer pageSize) {

        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);

        return dqExecuteResultService.queryResultListPaging(loginUser, searchVal, state, ruleType, startTime, endTime,
                pageNo, pageSize);
    }

    /**
     * get datasource options by id
     * @param datasourceId datasourceId
     * @return result
     */
    @Operation(summary = "getDatasourceOptionsById", description = "GET_DATASOURCE_OPTIONS_NOTES")
    @Parameters({
            @Parameter(name = "datasourceId", description = "DATA_SOURCE_ID", schema = @Schema(implementation = int.class, example = "1"))
    })
    @GetMapping(value = "/getDatasourceOptionsById")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_DATASOURCE_OPTIONS_ERROR)
    public Result getDatasourceOptionsById(@RequestParam(value = "datasourceId") int datasourceId) {
        Map<String, Object> result = dqRuleService.getDatasourceOptionsById(datasourceId);
        return returnDataList(result);
    }
}
