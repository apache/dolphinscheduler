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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_CLUSTER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_CLUSTER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_CLUSTER_BY_CODE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_CLUSTER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_CLUSTER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_CLUSTER_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ClusterService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
 * cluster controller
 */
@Tag(name = "CLUSTER_TAG")
@RestController
@RequestMapping("cluster")
public class ClusterController extends BaseController {

    @Autowired
    private ClusterService clusterService;

    /**
     * create cluster
     *
     * @param loginUser   login user
     * @param name        cluster name
     * @param config      config
     * @param description description
     * @return returns an error if it exists
     */
    @Operation(summary = "createCluster", description = "CREATE_CLUSTER_NOTES")
    @Parameters({
            @Parameter(name = "name", description = "CLUSTER_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "config", description = "CLUSTER_CONFIG", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "description", description = "CLUSTER_DESC", schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_CLUSTER_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createProject(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam("name") String name,
                                @RequestParam("config") String config,
                                @RequestParam(value = "description", required = false) String description) {

        Map<String, Object> result = clusterService.createCluster(loginUser, name, config, description);
        return returnDataList(result);
    }

    /**
     * update cluster
     *
     * @param loginUser   login user
     * @param code        cluster code
     * @param name        cluster name
     * @param config      cluster config
     * @param description description
     * @return update result code
     */
    @Operation(summary = "updateCluster", description = "UPDATE_CLUSTER_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "CLUSTER_CODE", required = true, schema = @Schema(implementation = long.class, example = "100")),
            @Parameter(name = "name", description = "CLUSTER_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "config", description = "CLUSTER_CONFIG", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "description", description = "CLUSTER_DESC", schema = @Schema(implementation = String.class)),
    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_CLUSTER_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateCluster(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam("code") Long code,
                                @RequestParam("name") String name,
                                @RequestParam("config") String config,
                                @RequestParam(value = "description", required = false) String description) {
        Map<String, Object> result = clusterService.updateClusterByCode(loginUser, code, name, config, description);
        return returnDataList(result);
    }

    /**
     * query cluster details by code
     *
     * @param clusterCode cluster code
     * @return cluster detail information
     */
    @Operation(summary = "queryClusterByCode", description = "QUERY_CLUSTER_BY_CODE_NOTES")
    @Parameters({
            @Parameter(name = "clusterCode", description = "CLUSTER_CODE", required = true, schema = @Schema(implementation = long.class, example = "100"))
    })
    @GetMapping(value = "/query-by-code")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_CLUSTER_BY_CODE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryClusterByCode(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                     @RequestParam("clusterCode") Long clusterCode) {

        Map<String, Object> result = clusterService.queryClusterByCode(clusterCode);
        return returnDataList(result);
    }

    /**
     * query cluster list paging
     *
     * @param searchVal search value
     * @param pageSize  page size
     * @param pageNo    page number
     * @return cluster list which the login user have permission to see
     */
    @Operation(summary = "queryClusterListPaging", description = "QUERY_CLUSTER_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "20")),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1"))
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_CLUSTER_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryClusterListPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @RequestParam(value = "searchVal", required = false) String searchVal,
                                         @RequestParam("pageSize") Integer pageSize,
                                         @RequestParam("pageNo") Integer pageNo) {

        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = clusterService.queryClusterListPaging(pageNo, pageSize, searchVal);
        return result;
    }

    /**
     * delete cluster by code
     *
     * @param loginUser   login user
     * @param clusterCode cluster code
     * @return delete result code
     */
    @Operation(summary = "deleteClusterByCode", description = "DELETE_CLUSTER_BY_CODE_NOTES")
    @Parameters({
            @Parameter(name = "clusterCode", description = "CLUSTER_CODE", required = true, schema = @Schema(implementation = long.class, example = "100"))
    })
    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_CLUSTER_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteCluster(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam("clusterCode") Long clusterCode) {

        Map<String, Object> result = clusterService.deleteClusterByCode(loginUser, clusterCode);
        return returnDataList(result);
    }

    /**
     * query all cluster list
     *
     * @param loginUser login user
     * @return all cluster list
     */
    @Operation(summary = "queryAllClusterList", description = "QUERY_ALL_CLUSTER_LIST_NOTES")
    @GetMapping(value = "/query-cluster-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_CLUSTER_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAllClusterList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        Map<String, Object> result = clusterService.queryAllClusterList();
        return returnDataList(result);
    }

    /**
     * verify cluster and cluster name
     *
     * @param loginUser   login user
     * @param clusterName cluster name
     * @return true if the cluster name not exists, otherwise return false
     */
    @Operation(summary = "verifyCluster", description = "VERIFY_CLUSTER_NOTES")
    @Parameters({
            @Parameter(name = "clusterName", description = "CLUSTER_NAME", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/verify-cluster")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_CLUSTER_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result verifyCluster(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam(value = "clusterName") String clusterName) {
        Map<String, Object> result = clusterService.verifyCluster(clusterName);
        return returnDataList(result);
    }
}
