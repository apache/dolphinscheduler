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
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import springfox.documentation.annotations.ApiIgnore;

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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * cluster controller
 */
@Api(tags = "CLUSTER_TAG")
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
    @ApiOperation(value = "createCluster", notes = "CREATE_CLUSTER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "CLUSTER_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "config", value = "CONFIG", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "description", value = "CLUSTER_DESC", dataTypeClass = String.class)
    })
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_CLUSTER_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createProject(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
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
    @ApiOperation(value = "updateCluster", notes = "UPDATE_CLUSTER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "CLUSTER_CODE", required = true, dataTypeClass = long.class, example = "100"),
            @ApiImplicitParam(name = "name", value = "CLUSTER_NAME", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "config", value = "CLUSTER_CONFIG", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "description", value = "CLUSTER_DESC", dataTypeClass = String.class),
    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_CLUSTER_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateCluster(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
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
    @ApiOperation(value = "queryClusterByCode", notes = "QUERY_CLUSTER_BY_CODE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clusterCode", value = "CLUSTER_CODE", required = true, dataTypeClass = long.class, example = "100")
    })
    @GetMapping(value = "/query-by-code")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_CLUSTER_BY_CODE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryClusterByCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
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
    @ApiOperation(value = "queryClusterListPaging", notes = "QUERY_CLUSTER_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataTypeClass = String.class),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataTypeClass = int.class, example = "20"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataTypeClass = int.class, example = "1")
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_CLUSTER_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryClusterListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
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
    @ApiOperation(value = "deleteClusterByCode", notes = "DELETE_CLUSTER_BY_CODE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clusterCode", value = "CLUSTER_CODE", required = true, dataTypeClass = long.class, example = "100")
    })
    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_CLUSTER_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result deleteCluster(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
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
    @ApiOperation(value = "queryAllClusterList", notes = "QUERY_ALL_CLUSTER_LIST_NOTES")
    @GetMapping(value = "/query-cluster-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_CLUSTER_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryAllClusterList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
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
    @ApiOperation(value = "verifyCluster", notes = "VERIFY_CLUSTER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clusterName", value = "CLUSTER_NAME", required = true, dataTypeClass = String.class)
    })
    @PostMapping(value = "/verify-cluster")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_CLUSTER_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result verifyCluster(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam(value = "clusterName") String clusterName) {
        Map<String, Object> result = clusterService.verifyCluster(clusterName);
        return returnDataList(result);
    }
}
