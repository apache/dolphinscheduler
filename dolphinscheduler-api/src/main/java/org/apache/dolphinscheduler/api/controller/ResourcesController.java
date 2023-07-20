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
import org.apache.dolphinscheduler.api.dto.resources.DeleteDataTransferResponse;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ResourcesService;
import org.apache.dolphinscheduler.api.service.UdfFuncService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.common.enums.UdfType;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * resources controller
 */
@Tag(name = "RESOURCES_TAG")
@RestController
@RequestMapping("resources")
@Slf4j
public class ResourcesController extends BaseController {

    @Autowired
    private ResourcesService resourceService;
    @Autowired
    private UdfFuncService udfFuncService;

    /**
     * @param loginUser login user
     * @param type type
     * @param alias alias
     * @param pid parent id
     * @param currentDir current directory
     * @return create result code
     */
    @Operation(summary = "createDirectory", description = "CREATE_RESOURCE_NOTES")
    @Parameters({
            @Parameter(name = "type", description = "RESOURCE_TYPE", required = true, schema = @Schema(implementation = ResourceType.class)),
            @Parameter(name = "name", description = "RESOURCE_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "pid", description = "RESOURCE_PID", required = true, schema = @Schema(implementation = int.class, example = "10")),
            @Parameter(name = "currentDir", description = "RESOURCE_CURRENT_DIR", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/directory")
    @ApiException(CREATE_RESOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> createDirectory(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @RequestParam(value = "type") ResourceType type,
                                          @RequestParam(value = "name") String alias,
                                          @RequestParam(value = "pid") int pid,
                                          @RequestParam(value = "currentDir") String currentDir) {
        // todo verify the directory name
        return resourceService.createDirectory(loginUser, alias, type, pid, currentDir);
    }

    /**
     * create resource
     *
     * @return create result code
     */
    @Operation(summary = "createResource", description = "CREATE_RESOURCE_NOTES")
    @Parameters({
            @Parameter(name = "type", description = "RESOURCE_TYPE", required = true, schema = @Schema(implementation = ResourceType.class)),
            @Parameter(name = "name", description = "RESOURCE_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "file", description = "RESOURCE_FILE", required = true, schema = @Schema(implementation = MultipartFile.class)),
            @Parameter(name = "currentDir", description = "RESOURCE_CURRENT_DIR", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping()
    @ApiException(CREATE_RESOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> createResource(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @RequestParam(value = "type") ResourceType type,
                                         @RequestParam(value = "name") String alias,
                                         @RequestParam("file") MultipartFile file,
                                         @RequestParam(value = "currentDir") String currentDir) {
        // todo verify the file name
        return resourceService.createResource(loginUser, alias, type, file, currentDir);
    }

    /**
     * update resource
     *
     * @param loginUser login user
     * @param alias alias
     * @param type resource type
     * @param file resource file
     * @return update result code
     */
    @Operation(summary = "updateResource", description = "UPDATE_RESOURCE_NOTES")
    @Parameters({
            @Parameter(name = "fullName", description = "RESOURCE_FULLNAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "tenantCode", description = "TENANT_CODE", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "type", description = "RESOURCE_TYPE", required = true, schema = @Schema(implementation = ResourceType.class)),
            @Parameter(name = "name", description = "RESOURCE_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "file", description = "RESOURCE_FILE", required = true, schema = @Schema(implementation = MultipartFile.class))
    })
    @PutMapping()
    @ApiException(UPDATE_RESOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> updateResource(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @RequestParam(value = "fullName") String fullName,
                                         @RequestParam(value = "tenantCode", required = false) String tenantCode,
                                         @RequestParam(value = "type") ResourceType type,
                                         @RequestParam(value = "name") String alias,
                                         @RequestParam(value = "file", required = false) MultipartFile file) {
        return resourceService.updateResource(loginUser, fullName, tenantCode, alias, type, file);
    }

    /**
     * query resources list
     *
     * @param loginUser login user
     * @param type resource type
     * @return resource list
     */
    @Operation(summary = "queryResourceList", description = "QUERY_RESOURCE_LIST_NOTES")
    @Parameters({
            @Parameter(name = "type", description = "RESOURCE_TYPE", required = true, schema = @Schema(implementation = ResourceType.class)),
            @Parameter(name = "fullName", description = "RESOURCE_FULLNAME", required = true, schema = @Schema(implementation = String.class))
    })
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_RESOURCES_LIST_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> queryResourceList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestParam(value = "type") ResourceType type,
                                            @RequestParam(value = "fullName") String fullName) {
        Map<String, Object> result = resourceService.queryResourceList(loginUser, type, fullName);
        return returnDataList(result);
    }

    /**
     * query resources list paging
     *
     * @param loginUser login user
     * @param type resource type
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return resource list page
     */
    @Operation(summary = "queryResourceListPaging", description = "QUERY_RESOURCE_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "type", description = "RESOURCE_TYPE", required = true, schema = @Schema(implementation = ResourceType.class)),
            @Parameter(name = "fullName", description = "RESOURCE_FULLNAME", required = true, schema = @Schema(implementation = String.class, example = "bucket_name/tenant_name/type/ds")),
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "20"))
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_RESOURCES_LIST_PAGING)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<PageInfo<StorageEntity>> queryResourceListPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                   @RequestParam(value = "fullName") String fullName,
                                                                   @RequestParam(value = "tenantCode") String tenantCode,
                                                                   @RequestParam(value = "type") ResourceType type,
                                                                   @RequestParam("pageNo") Integer pageNo,
                                                                   @RequestParam(value = "searchVal", required = false) String searchVal,
                                                                   @RequestParam("pageSize") Integer pageSize) {
        Result<PageInfo<StorageEntity>> result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }

        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = resourceService.queryResourceListPaging(loginUser, fullName, tenantCode, type, searchVal, pageNo,
                pageSize);
        return result;
    }

    /**
     * delete resource
     *
     * @param loginUser login user
     * @return delete result code
     */
    @Operation(summary = "deleteResource", description = "DELETE_RESOURCE_BY_ID_NOTES")
    @Parameters({
            @Parameter(name = "fullName", description = "RESOURCE_FULLNAME", required = true, schema = @Schema(implementation = String.class, example = "test/"))
    })
    @DeleteMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_RESOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> deleteResource(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @RequestParam(value = "fullName") String fullName,
                                         @RequestParam(value = "tenantCode", required = false) String tenantCode) throws Exception {
        return resourceService.delete(loginUser, fullName, tenantCode);
    }

    /**
     * delete DATA_TRANSFER data
     *
     * @param loginUser login user
     * @return delete result code
     */
    @Operation(summary = "deleteDataTransferData", description = "Delete the N days ago data of DATA_TRANSFER ")
    @Parameters({
            @Parameter(name = "days", description = "N days ago", required = true, schema = @Schema(implementation = Integer.class))
    })
    @DeleteMapping(value = "/data-transfer")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_RESOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public DeleteDataTransferResponse deleteDataTransferData(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                             @RequestParam(value = "days") Integer days) {
        return resourceService.deleteDataTransferData(loginUser, days);
    }

    /**
     * verify resource by alias and type
     *
     * @param loginUser login user
     * @param fullName resource full name
     * @param type resource type
     * @return true if the resource name not exists, otherwise return false
     */
    @Operation(summary = "verifyResourceName", description = "VERIFY_RESOURCE_NAME_NOTES")
    @Parameters({
            @Parameter(name = "type", description = "RESOURCE_TYPE", required = true, schema = @Schema(implementation = ResourceType.class)),
            @Parameter(name = "fullName", description = "RESOURCE_FULL_NAME", required = true, schema = @Schema(implementation = String.class))
    })
    @GetMapping(value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_RESOURCE_BY_NAME_AND_TYPE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> verifyResourceName(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @RequestParam(value = "fullName") String fullName,
                                             @RequestParam(value = "type") ResourceType type) {
        return resourceService.verifyResourceName(fullName, type, loginUser);
    }

    /**
     * query resources by type
     *
     * @param loginUser login user
     * @param type resource type
     * @return resource list
     */
    @Operation(summary = "queryResourceByProgramType", description = "QUERY_RESOURCE_LIST_NOTES")
    @Parameters({
            @Parameter(name = "type", description = "RESOURCE_TYPE", required = true, schema = @Schema(implementation = ResourceType.class))
    })
    @GetMapping(value = "/query-by-type")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_RESOURCES_LIST_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> queryResourceJarList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @RequestParam(value = "type") ResourceType type,
                                               @RequestParam(value = "programType", required = false) ProgramType programType) {
        return resourceService.queryResourceByProgramType(loginUser, type, programType);
    }

    /**
     * query resource by file name and type
     *
     * @param loginUser login user
     * @param fileName resource full name
     * @param tenantCode tenantcode of the owner of the resource
     * @param type resource type
     * @return true if the resource name not exists, otherwise return false
     */
    @Operation(summary = "queryResourceByFileName", description = "QUERY_BY_RESOURCE_FILE_NAME")
    @Parameters({
            @Parameter(name = "type", description = "RESOURCE_TYPE", required = true, schema = @Schema(implementation = ResourceType.class)),
            @Parameter(name = "fileName", description = "RESOURCE_FILE_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "tenantCode", description = "TENANT_CODE", required = true, schema = @Schema(implementation = String.class)),
    })
    @GetMapping(value = "/query-file-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(RESOURCE_NOT_EXIST)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> queryResourceByFileName(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @RequestParam(value = "fileName", required = false) String fileName,
                                                  @RequestParam(value = "tenantCode", required = false) String tenantCode,
                                                  @RequestParam(value = "type") ResourceType type) {

        return resourceService.queryResourceByFileName(loginUser, fileName, type, tenantCode);
    }

    /**
     * view resource file online
     *
     * @param loginUser login user
     * @param skipLineNum skip line number
     * @param limit limit
     * @return resource content
     */
    @Operation(summary = "viewResource", description = "VIEW_RESOURCE_BY_ID_NOTES")
    @Parameters({
            @Parameter(name = "fullName", description = "RESOURCE_FULL_NAME", required = true, schema = @Schema(implementation = String.class, example = "tenant/1.png")),
            @Parameter(name = "tenantCode", description = "TENANT_CODE", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "skipLineNum", description = "SKIP_LINE_NUM", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "limit", description = "LIMIT", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/view")
    @ApiException(VIEW_RESOURCE_FILE_ON_LINE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result viewResource(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                               @RequestParam(value = "skipLineNum") int skipLineNum,
                               @RequestParam(value = "limit") int limit,
                               @RequestParam(value = "fullName") String fullName,
                               @RequestParam(value = "tenantCode") String tenantCode) {
        return resourceService.readResource(loginUser, fullName, tenantCode, skipLineNum, limit);
    }

    /**
     * create resource file online
     *
     * @return create result code
     */
    @Operation(summary = "onlineCreateResource", description = "ONLINE_CREATE_RESOURCE_NOTES")
    @Parameters({
            @Parameter(name = "type", description = "RESOURCE_TYPE", required = true, schema = @Schema(implementation = ResourceType.class)),
            @Parameter(name = "fileName", description = "RESOURCE_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "suffix", description = "SUFFIX", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "description", description = "RESOURCE_DESC", schema = @Schema(implementation = String.class)),
            @Parameter(name = "content", description = "CONTENT", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "currentDir", description = "RESOURCE_CURRENTDIR", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping(value = "/online-create")
    @ApiException(CREATE_RESOURCE_FILE_ON_LINE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result onlineCreateResource(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam(value = "type") ResourceType type,
                                       @RequestParam(value = "fileName") String fileName,
                                       @RequestParam(value = "suffix") String fileSuffix,
                                       @RequestParam(value = "content") String content,
                                       @RequestParam(value = "currentDir") String currentDir) {
        if (StringUtils.isEmpty(content)) {
            log.error("resource file contents are not allowed to be empty");
            return error(RESOURCE_FILE_IS_EMPTY.getCode(), RESOURCE_FILE_IS_EMPTY.getMsg());
        }
        return resourceService.onlineCreateResource(loginUser, type, fileName, fileSuffix, content, currentDir);
    }

    /**
     * edit resource file online
     *
     * @param loginUser login user
     * @param content content
     * @return update result code
     */
    @Operation(summary = "updateResourceContent", description = "UPDATE_RESOURCE_NOTES")
    @Parameters({
            @Parameter(name = "content", description = "CONTENT", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "fullName", description = "FULL_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "tenantCode", description = "TENANT_CODE", required = true, schema = @Schema(implementation = String.class))
    })
    @PutMapping(value = "/update-content")
    @ApiException(EDIT_RESOURCE_FILE_ON_LINE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateResourceContent(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @RequestParam(value = "fullName") String fullName,
                                        @RequestParam(value = "tenantCode") String tenantCode,
                                        @RequestParam(value = "content") String content) {
        if (StringUtils.isEmpty(content)) {
            log.error("The resource file contents are not allowed to be empty");
            return error(RESOURCE_FILE_IS_EMPTY.getCode(), RESOURCE_FILE_IS_EMPTY.getMsg());
        }
        return resourceService.updateResourceContent(loginUser, fullName, tenantCode, content);
    }

    /**
     * download resource file
     *
     * @param loginUser login user
     * @return resource content
     */
    @Operation(summary = "downloadResource", description = "DOWNLOAD_RESOURCE_NOTES")
    @Parameters({
            @Parameter(name = "fullName", description = "RESOURCE_FULLNAME", required = true, schema = @Schema(implementation = String.class, example = "test/"))
    })
    @GetMapping(value = "/download")
    @ResponseBody
    @ApiException(DOWNLOAD_RESOURCE_FILE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public ResponseEntity downloadResource(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @RequestParam(value = "fullName") String fullName) throws Exception {
        Resource file = resourceService.downloadResource(loginUser, fullName);
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
     * @param loginUser login user
     * @param type udf type
     * @param funcName function name
     * @param argTypes argument types
     * @param database database
     * @param description description
     * @param className class name
     * @return create result code
     */
    @Operation(summary = "createUdfFunc", description = "CREATE_UDF_FUNCTION_NOTES")
    @Parameters({
            @Parameter(name = "type", description = "UDF_TYPE", required = true, schema = @Schema(implementation = UdfType.class)),
            @Parameter(name = "funcName", description = "FUNC_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "className", description = "CLASS_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "argTypes", description = "ARG_TYPES", schema = @Schema(implementation = String.class)),
            @Parameter(name = "database", description = "DATABASE_NAME", schema = @Schema(implementation = String.class)),
            @Parameter(name = "description", description = "UDF_DESC", schema = @Schema(implementation = String.class)),
            @Parameter(name = "resourceId", description = "RESOURCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))

    })
    @PostMapping(value = "/udf-func")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_UDF_FUNCTION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createUdfFunc(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam(value = "type") UdfType type,
                                @RequestParam(value = "funcName") String funcName,
                                @RequestParam(value = "className") String className,
                                @RequestParam(value = "fullName") String fullName,
                                @RequestParam(value = "argTypes", required = false) String argTypes,
                                @RequestParam(value = "database", required = false) String database,
                                @RequestParam(value = "description", required = false) String description) {
        // todo verify the sourceName
        return udfFuncService.createUdfFunction(loginUser, funcName, className, fullName,
                argTypes, database, description, type);
    }

    /**
     * view udf function
     *
     * @param loginUser login user
     * @param id udf function id
     * @return udf function detail
     */
    @Operation(summary = "viewUIUdfFunction", description = "VIEW_UDF_FUNCTION_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "RESOURCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))

    })
    @GetMapping(value = "/{id}/udf-func")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VIEW_UDF_FUNCTION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result viewUIUdfFunction(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @PathVariable("id") int id) {
        return udfFuncService.queryUdfFuncDetail(loginUser, id);
    }

    /**
     * update udf function
     *
     * @param loginUser login user
     * @param type resource type
     * @param funcName function name
     * @param argTypes argument types
     * @param database data base
     * @param description description
     * @param className class name
     * @param udfFuncId udf function id
     * @return update result code
     */
    @Operation(summary = "updateUdfFunc", description = "UPDATE_UDF_FUNCTION_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "UDF_ID", required = true, schema = @Schema(implementation = int.class)),
            @Parameter(name = "type", description = "UDF_TYPE", required = true, schema = @Schema(implementation = UdfType.class)),
            @Parameter(name = "funcName", description = "FUNC_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "className", description = "CLASS_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "argTypes", description = "ARG_TYPES", schema = @Schema(implementation = String.class)),
            @Parameter(name = "database", description = "DATABASE_NAME", schema = @Schema(implementation = String.class)),
            @Parameter(name = "description", description = "UDF_DESC", schema = @Schema(implementation = String.class))
    })
    @PutMapping(value = "/udf-func/{id}")
    @ApiException(UPDATE_UDF_FUNCTION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateUdfFunc(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @PathVariable(value = "id") int udfFuncId,
                                @RequestParam(value = "type") UdfType type,
                                @RequestParam(value = "funcName") String funcName,
                                @RequestParam(value = "className") String className,
                                @RequestParam(value = "argTypes", required = false) String argTypes,
                                @RequestParam(value = "database", required = false) String database,
                                @RequestParam(value = "description", required = false) String description,
                                @RequestParam(value = "fullName") String fullName) {
        return udfFuncService.updateUdfFunc(loginUser, udfFuncId, funcName, className,
                argTypes, database, description, type, fullName);
    }

    /**
     * query udf function list paging
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return udf function list page
     */
    @Operation(summary = "queryUdfFuncListPaging", description = "QUERY_UDF_FUNCTION_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "20"))
    })
    @GetMapping(value = "/udf-func")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_UDF_FUNCTION_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> queryUdfFuncListPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                 @RequestParam("pageNo") Integer pageNo,
                                                 @RequestParam(value = "searchVal", required = false) String searchVal,
                                                 @RequestParam("pageSize") Integer pageSize) {
        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        return udfFuncService.queryUdfFuncListPaging(loginUser, searchVal, pageNo, pageSize);
    }

    /**
     * query udf func list by type
     *
     * @param loginUser login user
     * @param type resource type
     * @return resource list
     */
    @Operation(summary = "queryUdfFuncList", description = "QUERY_UDF_FUNC_LIST_NOTES")
    @Parameters({
            @Parameter(name = "type", description = "UDF_TYPE", required = true, schema = @Schema(implementation = UdfType.class))
    })
    @GetMapping(value = "/udf-func/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DATASOURCE_BY_TYPE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> queryUdfFuncList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @RequestParam("type") UdfType type) {
        return udfFuncService.queryUdfFuncList(loginUser, type.ordinal());
    }

    /**
     * verify udf function name can use or not
     *
     * @param loginUser login user
     * @param name name
     * @return true if the name can user, otherwise return false
     */
    @Operation(summary = "verifyUdfFuncName", description = "VERIFY_UDF_FUNCTION_NAME_NOTES")
    @Parameters({
            @Parameter(name = "name", description = "FUNC_NAME", required = true, schema = @Schema(implementation = String.class))

    })
    @GetMapping(value = "/udf-func/verify-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_UDF_FUNCTION_NAME_ERROR)
    @AccessLogAnnotation
    public Result verifyUdfFuncName(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
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
    @Operation(summary = "deleteUdfFunc", description = "DELETE_UDF_FUNCTION_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "UDF_FUNC_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @DeleteMapping(value = "/udf-func/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_UDF_FUNCTION_ERROR)
    @AccessLogAnnotation
    public Result deleteUdfFunc(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @PathVariable(value = "id") int udfFuncId) {
        return udfFuncService.delete(loginUser, udfFuncId);
    }

    /**
     * authorized file resource list
     *
     * @param loginUser login user
     * @param userId user id
     * @return authorized result
     */
    @Operation(summary = "authorizedFile", description = "AUTHORIZED_FILE_NOTES")
    @Parameters({
            @Parameter(name = "userId", description = "USER_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/authed-file")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(AUTHORIZED_FILE_RESOURCE_ERROR)
    @AccessLogAnnotation
    public Result authorizedFile(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam("userId") Integer userId) {
        Map<String, Object> result = resourceService.authorizedFile(loginUser, userId);
        return returnDataList(result);
    }

    /**
     * unauthorized file resource list
     *
     * @param loginUser login user
     * @param userId user id
     * @return unauthorized result code
     */
    @Operation(summary = "authorizeResourceTree", description = "AUTHORIZE_RESOURCE_TREE_NOTES")
    @Parameters({
            @Parameter(name = "userId", description = "USER_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/authed-resource-tree")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(AUTHORIZE_RESOURCE_TREE)
    @AccessLogAnnotation
    public Result authorizeResourceTree(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @RequestParam("userId") Integer userId) {
        Map<String, Object> result = resourceService.authorizeResourceTree(loginUser, userId);
        return returnDataList(result);
    }

    /**
     * unauthorized udf function
     *
     * @param loginUser login user
     * @param userId user id
     * @return unauthorized result code
     */
    @Operation(summary = "unauthUDFFunc", description = "UNAUTHORIZED_UDF_FUNC_NOTES")
    @Parameters({
            @Parameter(name = "userId", description = "USER_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/unauth-udf-func")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(UNAUTHORIZED_UDF_FUNCTION_ERROR)
    @AccessLogAnnotation
    public Result unauthUDFFunc(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam("userId") Integer userId) {

        Map<String, Object> result = resourceService.unauthorizedUDFFunction(loginUser, userId);
        return returnDataList(result);
    }

    /**
     * authorized udf function
     *
     * @param loginUser login user
     * @param userId user id
     * @return authorized result code
     */
    @Operation(summary = "authUDFFunc", description = "AUTHORIZED_UDF_FUNC_NOTES")
    @Parameters({
            @Parameter(name = "userId", description = "USER_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/authed-udf-func")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(AUTHORIZED_UDF_FUNCTION_ERROR)
    @AccessLogAnnotation
    public Result authorizedUDFFunction(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @RequestParam("userId") Integer userId) {
        Map<String, Object> result = resourceService.authorizedUDFFunction(loginUser, userId);
        return returnDataList(result);
    }

    /**
     * query a resource by resource full name
     *
     * @param loginUser login user
     * @param fullName resource full name
     * @return resource
     */
    @Operation(summary = "queryResourceByFullName", description = "QUERY_BY_RESOURCE_FULL_NAME")
    @Parameters({
            @Parameter(name = "type", description = "RESOURCE_TYPE", required = true, schema = @Schema(implementation = ResourceType.class)),
            @Parameter(name = "fullName", description = "RESOURCE_FULL_NAME", required = true, schema = @Schema(implementation = String.class)),
    })
    @GetMapping(value = "/query-full-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(RESOURCE_NOT_EXIST)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryResourceByFullName(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @RequestParam(value = "type") ResourceType type,
                                          @RequestParam(value = "fullName") String fullName,
                                          @RequestParam(value = "tenantCode") String tenantCode) throws IOException {

        return resourceService.queryResourceByFullName(loginUser, fullName, tenantCode, type);
    }

    @Operation(summary = "queryResourceBaseDir", description = "QUERY_RESOURCE_BASE_DIR")
    @Parameters({
            @Parameter(name = "type", description = "RESOURCE_TYPE", required = true, schema = @Schema(implementation = ResourceType.class))
    })
    @GetMapping(value = "/base-dir")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(RESOURCE_NOT_EXIST)
    @AccessLogAnnotation
    public Result<Object> queryResourceBaseDir(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @RequestParam(value = "type") ResourceType type) {
        return resourceService.queryResourceBaseDir(loginUser, type);
    }
}
