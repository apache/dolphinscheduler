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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_K8S_NAMESPACE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_K8S_NAMESPACE_BY_ID_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_K8S_NAMESPACE_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_K8S_NAMESPACE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_K8S_NAMESPACE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.K8sNameSpaceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * k8s namespace controller
 */
@Api(tags = "K8S_NAMESPACE_TAG")
@RestController
@RequestMapping("/k8s-namespace")
public class K8sNamespaceController extends BaseController {

    @Autowired
    private K8sNameSpaceService k8sNameSpaceService;

    /**
     * query namespace list paging
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageSize  page size
     * @param pageNo    page number
     * @return namespace list which the login user have permission to see
     */
    @ApiOperation(value = "queryNamespaceListPaging", notes = "QUERY_NAMESPACE_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataType = "String"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataType = "Int", example = "10"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataType = "Int", example = "1")
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_K8S_NAMESPACE_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryProjectListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @RequestParam(value = "searchVal", required = false) String searchVal,
                                         @RequestParam("pageSize") Integer pageSize,
                                         @RequestParam("pageNo") Integer pageNo
    ) {

        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = k8sNameSpaceService.queryListPaging(loginUser, searchVal, pageNo, pageSize);
        return result;
    }


    /**
     * create namespace,if not exist on k8s,will create,if exist only register in db
     *
     * @param loginUser
     * @param namespace    k8s namespace
     * @param owner        owner
     * @param tag          Which type of job is available, can be empty, all available
     * @param k8s          k8s name
     * @param limitsCpu    max cpu
     * @param limitsMemory max memory
     * @return
     */
    @ApiOperation(value = "createK8sNamespace", notes = "CREATE_NAMESPACE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "namespace", value = "NAMESPACE", required = true, dataType = "String"),
            @ApiImplicitParam(name = "owner", value = "OWNER", required = false, dataType = "String"),
            @ApiImplicitParam(name = "tag", value = "TAG", required = false, dataType = "String"),
            @ApiImplicitParam(name = "k8s", value = "K8S", required = true, dataType = "String"),
            @ApiImplicitParam(name = "limits_cpu", value = "LIMITS_CPU", required = false, dataType = "Double"),
            @ApiImplicitParam(name = "limits_memory", value = "LIMITS_MEMORY", required = false, dataType = "Integer")
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_K8S_NAMESPACE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createNamespace(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam(value = "namespace") String namespace,
                                  @RequestParam(value = "k8s") String k8s,
                                  @RequestParam(value = "owner", required = false) String owner,
                                  @RequestParam(value = "tag", required = false) String tag,
                                  @RequestParam(value = "limitsCpu", required = false) Double limitsCpu,
                                  @RequestParam(value = "limitsMemory", required = false) Integer limitsMemory
    ) {
        Map<String, Object> result =  k8sNameSpaceService.createK8sNamespace(loginUser, namespace, k8s, owner, tag, limitsCpu, limitsMemory);
        return returnDataList(result);
    }

    /**
     * update namespace,namespace and k8s not allowed update, because may create on k8s,can delete and create new instead
     *
     * @param loginUser
     * @param owner        owner
     * @param tag          Which type of job is available,such as flink,means only flink job can use, can be empty, all available
     * @param limitsCpu    max cpu
     * @param limitsMemory max memory
     * @return
     */
    @ApiOperation(value = "updateK8sNamespace", notes = "UPDATE_NAMESPACE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "K8S_NAMESPACE_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "owner", value = "OWNER", required = false, dataType = "String"),
            @ApiImplicitParam(name = "tag", value = "TAG", required = false, dataType = "String"),
            @ApiImplicitParam(name = "limitsCpu", value = "LIMITS_CPU", required = false, dataType = "Double"),
            @ApiImplicitParam(name = "limitsMemory", value = "LIMITS_MEMORY", required = false, dataType = "Integer")})
    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(UPDATE_K8S_NAMESPACE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateNamespace(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @PathVariable(value = "id") int id,
                                  @RequestParam(value = "owner", required = false) String owner,
                                  @RequestParam(value = "tag", required = false) String tag,
                                  @RequestParam(value = "limitsCpu", required = false) Double limitsCpu,
                                  @RequestParam(value = "limitsMemory", required = false) Integer limitsMemory) {
        Map<String, Object> result = k8sNameSpaceService.updateK8sNamespace(loginUser, id, owner, tag, limitsCpu, limitsMemory);
        return returnDataList(result);
    }

    /**
     * verify namespace and k8s,one k8s namespace is unique
     *
     * @param loginUser login user
     * @param namespace namespace
     * @param k8s       k8s
     * @return true if the k8s and namespace not exists, otherwise return false
     */
    @ApiOperation(value = "verifyNamespaceK8s", notes = "VERIFY_NAMESPACE_K8S_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "namespace", value = "NAMESPACE", required = true, dataType = "String"),
            @ApiImplicitParam(name = "k8s", value = "K8S", required = true, dataType = "String")
    })
    @PostMapping(value = "/verify")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_K8S_NAMESPACE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result verifyNamespace(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam(value = "namespace") String namespace,
                                  @RequestParam(value = "k8s") String k8s
    ) {

        return k8sNameSpaceService.verifyNamespaceK8s(namespace, k8s);
    }


    /**
     * delete namespace by id
     *
     * @param loginUser login user
     * @param id        namespace id
     * @return delete result code
     */
    @ApiOperation(value = "delNamespaceById", notes = "DELETE_NAMESPACE_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "NAMESPACE_ID", required = true, dataType = "Int", example = "100")
    })
    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_K8S_NAMESPACE_BY_ID_ERROR)
    @AccessLogAnnotation
    public Result delNamespaceById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam(value = "id") int id) {
        Map<String, Object> result = k8sNameSpaceService.deleteNamespaceById(loginUser, id);
        return returnDataList(result);
    }
}
