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

import static org.apache.dolphinscheduler.api.enums.Status.AUTHORIZED_FILE_RESOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.AUTHORIZED_UDF_FUNCTION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.AUTHORIZE_RESOURCE_TREE;
import static org.apache.dolphinscheduler.api.enums.Status.CREATE_RESOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.CREATE_RESOURCE_FILE_ON_LINE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.CREATE_UDF_FUNCTION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_RESOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_UDF_FUNCTION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DOWNLOAD_RESOURCE_FILE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.EDIT_RESOURCE_FILE_ON_LINE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_DATASOURCE_BY_TYPE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_RESOURCES_LIST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_RESOURCES_LIST_PAGING;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_UDF_FUNCTION_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.RESOURCE_FILE_IS_EMPTY;
import static org.apache.dolphinscheduler.api.enums.Status.RESOURCE_NOT_EXIST;
import static org.apache.dolphinscheduler.api.enums.Status.UNAUTHORIZED_UDF_FUNCTION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_RESOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_UDF_FUNCTION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_RESOURCE_BY_NAME_AND_TYPE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_UDF_FUNCTION_NAME_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VIEW_RESOURCE_FILE_ON_LINE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VIEW_UDF_FUNCTION_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.dto.resources.CreateDirectoryRequest;
import org.apache.dolphinscheduler.api.dto.resources.CreateResourceRequest;
import org.apache.dolphinscheduler.api.dto.resources.CreateResourceResponse;
import org.apache.dolphinscheduler.api.dto.resources.CreateUdfRequest;
import org.apache.dolphinscheduler.api.dto.resources.ResourceContentResponse;
import org.apache.dolphinscheduler.api.dto.resources.ResourceListResponse;
import org.apache.dolphinscheduler.api.dto.resources.ResourcePageResponse;
import org.apache.dolphinscheduler.api.dto.resources.ResourceResponse;
import org.apache.dolphinscheduler.api.dto.resources.UdfFuncListResponse;
import org.apache.dolphinscheduler.api.dto.resources.UdfFuncPageResponse;
import org.apache.dolphinscheduler.api.dto.resources.UdfFuncResponse;
import org.apache.dolphinscheduler.api.dto.resources.UpdateResourceContentRequest;
import org.apache.dolphinscheduler.api.dto.resources.UpdateResourceResponse;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ResourcesService;
import org.apache.dolphinscheduler.api.service.UdfFuncService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.common.enums.UdfType;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * resources controller
 */
@Api(tags = "RESOURCES_TAG")
@RestController
@RequestMapping("/v2/resources")
public class ResourcesV2Controller extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ResourcesV2Controller.class);

    @Autowired
    private ResourcesService resourceService;
    @Autowired
    private UdfFuncService udfFuncService;

    /**
     * create resource director
     *
     * @param loginUser              login user
     * @param createDirectoryRequest create directory request
     * @return create result code
     */
    @ApiOperation(value = "createDirectory", notes = "CREATE_RESOURCE_NOTES")
    @PostMapping(value = "/directory", consumes = "application/json")
    @ApiException(CREATE_RESOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public CreateResourceResponse createDirectory(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @RequestBody CreateDirectoryRequest createDirectoryRequest) {
        // todo verify the directory name
        Result<Object> result = resourceService.createDirectory(loginUser,
                createDirectoryRequest.getName(),
                createDirectoryRequest.getDescription(),
                createDirectoryRequest.getType(),
                createDirectoryRequest.getPid(),
                createDirectoryRequest.getCurrentDir());
        return new CreateResourceResponse(result);
    }

    /**
     * update resource
     *
     * @param loginUser   login user
     * @param name        alias
     * @param type        resource type
     * @param description description
     * @param file        resource file
     * @param pid         pid
     * @param currentDir  currentDir
     * @return update result code
     */
    @ApiOperation(value = "createResource", notes = "CREATE_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType = "ResourceType", example = "FILE,UDF"),
            @ApiImplicitParam(name = "name", value = "RESOURCE_NAME", required = true, dataType = "String", example = "startupShell"),
            @ApiImplicitParam(name = "description", value = "RESOURCE_DESC", dataType = "String", example = "startupShell is description"),
            @ApiImplicitParam(name = "file", value = "RESOURCE_FILE", required = true, dataType = "MultipartFile"),
            @ApiImplicitParam(name = "pid", value = "RESOURCE_PID", required = true, dataType = "Int", example = "10"),
            @ApiImplicitParam(name = "currentDir", value = "RESOURCE_CURRENT_DIR", required = true, dataType = "String")
    })
    @PostMapping()
    @ApiException(CREATE_RESOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public CreateResourceResponse createResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                 @RequestParam(value = "type") ResourceType type,
                                                 @RequestParam(value = "name") String name,
                                                 @RequestParam(value = "description", required = false) String description,
                                                 @RequestParam("file") MultipartFile file,
                                                 @RequestParam(value = "pid") int pid,
                                                 @RequestParam(value = "currentDir") String currentDir) {
        // todo verify the file name
        Result<Object> result =
                resourceService.createResource(loginUser, name, description, type, file, pid, currentDir);
        return new CreateResourceResponse(result);
    }

    /**
     * update resource
     *
     * @param loginUser   login user
     * @param name        alias
     * @param resourceId  resource id
     * @param type        resource type
     * @param description description
     * @param file        resource file
     * @return update result code
     */
    @ApiOperation(value = "updateResource", notes = "UPDATE_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType = "ResourceType", example = "FILE,UDF"),
            @ApiImplicitParam(name = "name", value = "RESOURCE_NAME", required = true, dataType = "String", example = "startupShell"),
            @ApiImplicitParam(name = "description", value = "RESOURCE_DESC", dataType = "String", example = "startupShell is description"),
            @ApiImplicitParam(name = "file", value = "RESOURCE_FILE", required = true, dataType = "MultipartFile")
    })
    @PutMapping(value = "/{id}")
    @ApiException(UPDATE_RESOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public UpdateResourceResponse updateResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                 @PathVariable(value = "id") int resourceId,
                                                 @RequestParam(value = "type") ResourceType type,
                                                 @RequestParam(value = "name") String name,
                                                 @RequestParam(value = "description", required = false) String description,
                                                 @RequestParam(value = "file", required = false) MultipartFile file) {
        // todo verify the resource name
        Result<Object> result = resourceService.updateResource(loginUser, resourceId, name, description, type, file);
        return new UpdateResourceResponse(result);
    }

    /**
     * query resources list
     *
     * @param loginUser login user
     * @param type      resource type
     * @return resource list
     */
    @ApiOperation(value = "queryResourceList", notes = "QUERY_RESOURCE_LIST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType = "ResourceType", example = "FILE,UDF")
    })
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_RESOURCES_LIST_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ResourceListResponse queryResourceList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @RequestParam(value = "type") ResourceType type) {
        Map<String, Object> result = resourceService.queryResourceList(loginUser, type);
        return new ResourceListResponse(result);
    }

    /**
     * query resources list paging
     *
     * @param loginUser login user
     * @param type      resource type
     * @param id        resource id
     * @param searchVal search value
     * @param pageNo    page number
     * @param pageSize  page size
     * @return resource list page
     */
    @ApiOperation(value = "queryResourceListPaging", notes = "QUERY_RESOURCE_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType = "ResourceType", example = "FILE,UDF"),
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType = "int", example = "10"),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataType = "String", example = "startupShell"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataType = "Int", example = "20")
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_RESOURCES_LIST_PAGING)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ResourcePageResponse queryResourceListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                        @RequestParam(value = "type") ResourceType type,
                                                        @RequestParam(value = "id") int id,
                                                        @RequestParam("pageNo") Integer pageNo,
                                                        @RequestParam(value = "searchVal", required = false) String searchVal,
                                                        @RequestParam("pageSize") Integer pageSize) {
        Result<PageInfo<Resource>> result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return new ResourcePageResponse(result);
        }

        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = resourceService.queryResourceListPaging(loginUser, id, type, searchVal, pageNo, pageSize);
        return new ResourcePageResponse(result);
    }

    /**
     * delete resource
     *
     * @param loginUser  login user
     * @param resourceId resource id
     * @return delete result code
     * @throws Exception
     */
    @ApiOperation(value = "deleteResource", notes = "DELETE_RESOURCE_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType = "Int", example = "100")
    })
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_RESOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> deleteResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @PathVariable(value = "id") int resourceId) throws Exception {
        return resourceService.delete(loginUser, resourceId);
    }

    /**
     * verify resource by alias and type
     *
     * @param loginUser login user
     * @param fullName  resource full name
     * @param type      resource type
     * @return true if the resource name not exists, otherwise return false
     */
    @ApiOperation(value = "verifyResourceName", notes = "VERIFY_RESOURCE_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType = "ResourceType", example = "FILE,UDF"),
            @ApiImplicitParam(name = "fullName", value = "RESOURCE_FULL_NAME", required = true, dataType = "String")
    })
    @GetMapping(value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_RESOURCE_BY_NAME_AND_TYPE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> verifyResourceName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @RequestParam(value = "fullName") String fullName,
                                             @RequestParam(value = "type") ResourceType type) {
        return resourceService.verifyResourceName(fullName, type, loginUser);
    }

    /**
     * query resources by type
     *
     * @param loginUser login user
     * @param type      resource type
     * @param programType program type
     * @return resource list
     */
    @ApiOperation(value = "queryResourceByProgramType", notes = "QUERY_RESOURCE_LIST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType = "ResourceType", example = "FILE,UDF")
    })
    @GetMapping(value = "/query-by-type")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_RESOURCES_LIST_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ResourceListResponse queryResourceJarList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                     @RequestParam(value = "type") ResourceType type,
                                                     @RequestParam(value = "programType", required = false) ProgramType programType) {
        Result<Object> result = resourceService.queryResourceByProgramType(loginUser, type, programType);
        return new ResourceListResponse(result);
    }

    /**
     * query resource by full name and type
     *
     * @param loginUser login user
     * @param fullName  resource full name
     * @param type      resource type
     * @param id        resource id
     * @return true if the resource name not exists, otherwise return false
     */
    @ApiOperation(value = "queryResource", notes = "QUERY_BY_RESOURCE_NAME")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType = "ResourceType", example = "FILE,UDF"),
            @ApiImplicitParam(name = "fullName", value = "RESOURCE_FULL_NAME", required = true, dataType = "String", example = "startupShell"),
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = false, dataType = "Int", example = "10")
    })
    @GetMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(RESOURCE_NOT_EXIST)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ResourceResponse queryResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @RequestParam(value = "fullName", required = false) String fullName,
                                          @PathVariable(value = "id", required = false) Integer id,
                                          @RequestParam(value = "type") ResourceType type) {
        Result<Object> result = resourceService.queryResource(loginUser, fullName, id, type);
        return new ResourceResponse(result);
    }

    /**
     * view resource file online
     *
     * @param loginUser   login user
     * @param resourceId  resource id
     * @param skipLineNum skip line number
     * @param limit       limit
     * @return resource content
     */
    @ApiOperation(value = "viewResource", notes = "VIEW_RESOURCE_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "skipLineNum", value = "SKIP_LINE_NUM", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "limit", value = "LIMIT", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/{id}/view")
    @ApiException(VIEW_RESOURCE_FILE_ON_LINE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ResourceContentResponse viewResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                @PathVariable(value = "id") int resourceId,
                                                @RequestParam(value = "skipLineNum") int skipLineNum,
                                                @RequestParam(value = "limit") int limit) {
        Result<Object> result = resourceService.readResource(loginUser, resourceId, skipLineNum, limit);
        return new ResourceContentResponse(result);
    }

    /**
     * create resource file online
     *
     * @param loginUser             login user
     * @param createResourceRequest create resource request
     * @return create result code
     */
    @ApiOperation(value = "onlineCreateResource", notes = "ONLINE_CREATE_RESOURCE_NOTES")
    @PostMapping(value = "/online-create", consumes = "application/json")
    @ApiException(CREATE_RESOURCE_FILE_ON_LINE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public CreateResourceResponse onlineCreateResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                       @RequestBody CreateResourceRequest createResourceRequest) {

        if (StringUtils.isEmpty(createResourceRequest.getContent())) {
            logger.error("resource file contents are not allowed to be empty");
            Result<Object> result = error(RESOURCE_FILE_IS_EMPTY.getCode(), RESOURCE_FILE_IS_EMPTY.getMsg());
            return new CreateResourceResponse(result);
        }
        Result<Object> result = resourceService.onlineCreateResource(loginUser, createResourceRequest.getType(),
                createResourceRequest.getFileName(),
                createResourceRequest.getSuffix(),
                createResourceRequest.getDescription(),
                createResourceRequest.getContent(),
                createResourceRequest.getPid(),
                createResourceRequest.getCurrentDir());
        return new CreateResourceResponse(result);
    }

    /**
     * edit resource file online
     *
     * @param loginUser                    login user
     * @param resourceId                   resource id
     * @param updateResourceContentRequest update resource content request
     * @return update result code
     */
    @ApiOperation(value = "updateResourceContent", notes = "UPDATE_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType = "Int", example = "100")
    })
    @PutMapping(value = "/{id}/update-content", consumes = "application/json")
    @ApiException(EDIT_RESOURCE_FILE_ON_LINE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> updateResourceContent(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                @PathVariable(value = "id") int resourceId,
                                                @RequestBody UpdateResourceContentRequest updateResourceContentRequest) {

        if (StringUtils.isEmpty(updateResourceContentRequest.getContent())) {
            logger.error("The resource file contents are not allowed to be empty");
            return error(RESOURCE_FILE_IS_EMPTY.getCode(), RESOURCE_FILE_IS_EMPTY.getMsg());
        }
        return resourceService.updateResourceContent(loginUser, resourceId, updateResourceContentRequest.getContent());
    }

    /**
     * download resource file
     *
     * @param loginUser  login user
     * @param resourceId resource id
     * @throws Exception
     * @return resource content
     */
    @ApiOperation(value = "downloadResource", notes = "DOWNLOAD_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/{id}/download")
    @ResponseBody
    @ApiException(DOWNLOAD_RESOURCE_FILE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ResponseEntity downloadResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @PathVariable(value = "id") int resourceId) throws Exception {
        org.springframework.core.io.Resource file = resourceService.downloadResource(loginUser, resourceId);
        if (file == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RESOURCE_NOT_EXIST.getMsg());
        }
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    /**
     * create udf function
     *
     * @param loginUser        login user
     * @param createUdfRequest udf request
     * @param resourceId       resource id
     * @return create result code
     */
    @ApiOperation(value = "createUdfFunc", notes = "CREATE_UDF_FUNCTION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "resourceId", value = "RESOURCE_ID", required = true, dataType = "Int", example = "100")
    })
    @PostMapping(value = "/{resourceId}/udf-func", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_UDF_FUNCTION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createUdfFunc(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestBody CreateUdfRequest createUdfRequest,
                                @PathVariable(value = "resourceId") int resourceId) {

        // todo verify the sourceName
        return udfFuncService.createUdfFunction(loginUser, createUdfRequest.getFuncName(),
                createUdfRequest.getClassName(),
                createUdfRequest.getArgTypes(),
                createUdfRequest.getDatabase(),
                createUdfRequest.getDescription(),
                createUdfRequest.getType(),
                resourceId);
    }

    /**
     * view udf function
     *
     * @param loginUser login user
     * @param id        resource id
     * @return udf function detail
     */
    @ApiOperation(value = "viewUIUdfFunction", notes = "VIEW_UDF_FUNCTION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType = "Int", example = "100")

    })
    @GetMapping(value = "/{id}/udf-func")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VIEW_UDF_FUNCTION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public UdfFuncResponse viewUIUdfFunction(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @PathVariable("id") int id) {
        Result<Object> result = udfFuncService.queryUdfFuncDetail(loginUser, id);
        return new UdfFuncResponse(result);
    }

    /**
     * update udf function
     *
     * @param loginUser        login user
     * @param resourceId       resource id
     * @param updateUdfRequest udf Request
     * @param udfFuncId        udf function id
     * @return update result code
     */
    @ApiOperation(value = "updateUdfFunc", notes = "UPDATE_UDF_FUNCTION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "UDF_ID", required = true, dataType = "Int"),
            @ApiImplicitParam(name = "resourceId", value = "RESOURCE_ID", required = true, dataType = "Int", example = "100")

    })
    @PutMapping(value = "/{resourceId}/udf-func/{id}")
    @ApiException(UPDATE_UDF_FUNCTION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateUdfFunc(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestBody CreateUdfRequest updateUdfRequest,
                                @PathVariable(value = "id") int udfFuncId,
                                @PathVariable(value = "resourceId") int resourceId) {
        return udfFuncService.updateUdfFunc(loginUser, udfFuncId,
                updateUdfRequest.getFuncName(),
                updateUdfRequest.getClassName(),
                updateUdfRequest.getArgTypes(),
                updateUdfRequest.getDatabase(),
                updateUdfRequest.getDescription(),
                updateUdfRequest.getType(),
                resourceId);
    }

    /**
     * query udf function list paging
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo    page number
     * @param pageSize  page size
     * @return udf function list page
     */
    @ApiOperation(value = "queryUdfFuncListPaging", notes = "QUERY_UDF_FUNCTION_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataType = "String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataType = "Int", example = "20")
    })
    @GetMapping(value = "/udf-func")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_UDF_FUNCTION_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public UdfFuncPageResponse queryUdfFuncListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                      @RequestParam("pageNo") Integer pageNo,
                                                      @RequestParam(value = "searchVal", required = false) String searchVal,
                                                      @RequestParam("pageSize") Integer pageSize) {
        Result<PageInfo<UdfFunc>> result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return new UdfFuncPageResponse(result);
        }
        result = udfFuncService.queryUdfFuncListPaging(loginUser, searchVal, pageNo, pageSize);
        return new UdfFuncPageResponse(result);
    }

    /**
     * query udf func list by type
     *
     * @param loginUser login user
     * @param type      resource type
     * @return resource list
     */
    @ApiOperation(value = "queryUdfFuncList", notes = "QUERY_UDF_FUNC_LIST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "UDF_TYPE", required = true, dataType = "UdfType")
    })
    @GetMapping(value = "/udf-func/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DATASOURCE_BY_TYPE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public UdfFuncListResponse queryUdfFuncList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                @RequestParam("type") UdfType type) {
        Result<Object> result = udfFuncService.queryUdfFuncList(loginUser, type.ordinal());
        return new UdfFuncListResponse(result);
    }

    /**
     * verify udf function name can use or not
     *
     * @param loginUser login user
     * @param name      name
     * @return true if the name can user, otherwise return false
     */
    @ApiOperation(value = "verifyUdfFuncName", notes = "VERIFY_UDF_FUNCTION_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "FUNC_NAME", required = true, dataType = "String")

    })
    @GetMapping(value = "/udf-func/verify-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_UDF_FUNCTION_NAME_ERROR)
    @AccessLogAnnotation
    public Result<Object> verifyUdfFuncName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestParam(value = "name") String name) {
        return udfFuncService.verifyUdfFuncByName(loginUser, name);
    }

    /**
     * delete udf function
     *
     * @param loginUser login user
     * @param udfFuncId udf function id
     * @return delete result code
     */
    @ApiOperation(value = "deleteUdfFunc", notes = "DELETE_UDF_FUNCTION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "UDF_FUNC_ID", required = true, dataType = "Int", example = "100")
    })
    @DeleteMapping(value = "/udf-func/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_UDF_FUNCTION_ERROR)
    @AccessLogAnnotation
    public Result<Object> deleteUdfFunc(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @PathVariable(value = "id") int udfFuncId) {
        return udfFuncService.delete(loginUser, udfFuncId);
    }

    /**
     * authorized file resource list
     *
     * @param loginUser login user
     * @param userId    user id
     * @return authorized result
     */
    @ApiOperation(value = "authorizedFile", notes = "AUTHORIZED_FILE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/authed-file")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(AUTHORIZED_FILE_RESOURCE_ERROR)
    @AccessLogAnnotation
    public ResourceListResponse authorizedFile(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @RequestParam("userId") Integer userId) {
        Map<String, Object> result = resourceService.authorizedFile(loginUser, userId);
        return new ResourceListResponse(result);
    }

    /**
     * unauthorized file resource list
     *
     * @param loginUser login user
     * @param userId    user id
     * @return unauthorized result code
     */
    @ApiOperation(value = "authorizeResourceTree", notes = "AUTHORIZE_RESOURCE_TREE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/authed-resource-tree")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(AUTHORIZE_RESOURCE_TREE)
    @AccessLogAnnotation
    public ResourceListResponse authorizeResourceTree(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                      @RequestParam("userId") Integer userId) {
        Map<String, Object> result = resourceService.authorizeResourceTree(loginUser, userId);
        return new ResourceListResponse(result);
    }

    /**
     * unauthorized udf function
     *
     * @param loginUser login user
     * @param userId    user id
     * @return unauthorized result code
     */
    @ApiOperation(value = "unauthUDFFunc", notes = "UNAUTHORIZED_UDF_FUNC_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/unauth-udf-func")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(UNAUTHORIZED_UDF_FUNCTION_ERROR)
    @AccessLogAnnotation
    public UdfFuncListResponse unauthUDFFunc(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @RequestParam("userId") Integer userId) {

        Map<String, Object> result = resourceService.unauthorizedUDFFunction(loginUser, userId);
        return new UdfFuncListResponse(result);
    }

    /**
     * authorized udf function
     *
     * @param loginUser login user
     * @param userId    user id
     * @return authorized result code
     */
    @ApiOperation(value = "authUDFFunc", notes = "AUTHORIZED_UDF_FUNC_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/authed-udf-func")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(AUTHORIZED_UDF_FUNCTION_ERROR)
    @AccessLogAnnotation
    public UdfFuncListResponse authorizedUDFFunction(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                     @RequestParam("userId") Integer userId) {
        Map<String, Object> result = resourceService.authorizedUDFFunction(loginUser, userId);
        return new UdfFuncListResponse(result);
    }

    /**
     * query resource by resource id
     *
     * @param loginUser login user
     * @param id        resource id
     * @return resource
     */
    @ApiOperation(value = "queryResourceById", notes = "QUERY_BY_RESOURCE_NAME")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType = "Int", example = "10")
    })
    @GetMapping(value = "/{id}/query")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(RESOURCE_NOT_EXIST)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ResourceResponse queryResourceById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @PathVariable(value = "id", required = true) Integer id) {
        Result<Object> result = resourceService.queryResourceById(loginUser, id);
        return new ResourceResponse(result);
    }
}
