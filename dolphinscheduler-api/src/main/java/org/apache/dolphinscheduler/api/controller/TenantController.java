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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_TENANT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_TENANT_BY_ID_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TENANT_LIST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TENANT_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_TENANT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_OS_TENANT_CODE_ERROR;

import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.TenantService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
 * tenant controller
 */
@Tag(name = "TENANT_TAG")
@RestController
@RequestMapping("/tenants")
public class TenantController extends BaseController {

    @Autowired
    private TenantService tenantService;

    /**
     * create tenant
     *
     * @param loginUser login user
     * @param tenantCode tenant code
     * @param queueId queue id
     * @param description description
     * @return create result code
     */
    @Operation(summary = "createTenant", description = "CREATE_TENANT_NOTES")
    @Parameters({
            @Parameter(name = "tenantCode", description = "TENANT_CODE", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "queueId", description = "QUEUE_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "description", description = "TENANT_DESC", schema = @Schema(implementation = String.class))
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_TENANT_ERROR)
    public Result<Tenant> createTenant(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam(value = "tenantCode") String tenantCode,
                                       @RequestParam(value = "queueId") int queueId,
                                       @RequestParam(value = "description", required = false) String description) throws Exception {

        Tenant tenant = tenantService.createTenant(loginUser, tenantCode, queueId, description);
        return Result.success(tenant);
    }

    /**
     * query tenant list paging
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return tenant list page
     */
    @Operation(summary = "queryTenantListPaging", description = "QUERY_TENANT_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "20"))
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TENANT_LIST_PAGING_ERROR)
    public Result<PageInfo<Tenant>> queryTenantListPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                          @RequestParam(value = "searchVal", required = false) String searchVal,
                                                          @RequestParam("pageNo") Integer pageNo,
                                                          @RequestParam("pageSize") Integer pageSize) {
        checkPageParams(pageNo, pageSize);
        searchVal = ParameterUtils.handleEscapes(searchVal);
        PageInfo<Tenant> tenantPageInfo = tenantService.queryTenantList(loginUser, searchVal, pageNo, pageSize);
        return Result.success(tenantPageInfo);
    }

    /**
     * tenant list
     *
     * @param loginUser login user
     * @return tenant list
     */
    @Operation(summary = "queryTenantList", description = "QUERY_TENANT_LIST_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TENANT_LIST_ERROR)
    public Result<List<Tenant>> queryTenantList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        List<Tenant> tenants = tenantService.queryTenantList(loginUser);
        return Result.success(tenants);
    }

    /**
     * update tenant
     *
     * @param loginUser login user
     * @param id tenant id
     * @param tenantCode tenant code
     * @param queueId queue id
     * @param description description
     * @return update result code
     */
    @Operation(summary = "updateTenant", description = "UPDATE_TENANT_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "TENANT_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "tenantCode", description = "TENANT_CODE", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "queueId", description = "QUEUE_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "description", description = "TENANT_DESC", schema = @Schema(implementation = String.class))
    })
    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_TENANT_ERROR)
    public Result<Boolean> updateTenant(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @PathVariable(value = "id") int id,
                                        @RequestParam(value = "tenantCode") String tenantCode,
                                        @RequestParam(value = "queueId") int queueId,
                                        @RequestParam(value = "description", required = false) String description) throws Exception {

        tenantService.updateTenant(loginUser, id, tenantCode, queueId, description);
        return Result.success(true);
    }

    /**
     * delete tenant by id
     *
     * @param loginUser login user
     * @param id tenant id
     * @return delete result code
     */
    @Operation(summary = "deleteTenantById", description = "DELETE_TENANT_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "TENANT_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_TENANT_BY_ID_ERROR)
    public Result<Boolean> deleteTenantById(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @PathVariable(value = "id") int id) throws Exception {
        tenantService.deleteTenantById(loginUser, id);
        return Result.success(true);
    }

    /**
     * verify tenant code
     *
     * @param loginUser login user
     * @param tenantCode tenant code
     * @return true if tenant code can use, otherwise return false
     */
    @Operation(summary = "verifyTenantCode", description = "VERIFY_TENANT_CODE_NOTES")
    @Parameters({
            @Parameter(name = "tenantCode", description = "TENANT_CODE", required = true, schema = @Schema(implementation = String.class))
    })
    @GetMapping(value = "/verify-code")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_OS_TENANT_CODE_ERROR)
    public Result<Boolean> verifyTenantCode(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestParam(value = "tenantCode") String tenantCode) {
        tenantService.verifyTenantCode(tenantCode);
        return Result.success(true);
    }

}
