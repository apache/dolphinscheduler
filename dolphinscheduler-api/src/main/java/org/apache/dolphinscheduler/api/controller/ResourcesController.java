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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_RESOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.CREATE_RESOURCE_FILE_ON_LINE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_RESOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DOWNLOAD_RESOURCE_FILE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.EDIT_RESOURCE_FILE_ON_LINE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_RESOURCES_LIST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_RESOURCES_LIST_PAGING;
import static org.apache.dolphinscheduler.api.enums.Status.RESOURCE_NOT_EXIST;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_RESOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VIEW_RESOURCE_FILE_ON_LINE_ERROR;

import org.apache.dolphinscheduler.api.audit.OperatorLog;
import org.apache.dolphinscheduler.api.audit.enums.AuditType;
import org.apache.dolphinscheduler.api.dto.resources.CreateDirectoryRequest;
import org.apache.dolphinscheduler.api.dto.resources.CreateFileFromContentRequest;
import org.apache.dolphinscheduler.api.dto.resources.CreateFileRequest;
import org.apache.dolphinscheduler.api.dto.resources.DeleteResourceRequest;
import org.apache.dolphinscheduler.api.dto.resources.DownloadFileRequest;
import org.apache.dolphinscheduler.api.dto.resources.FetchFileContentRequest;
import org.apache.dolphinscheduler.api.dto.resources.PagingResourceItemRequest;
import org.apache.dolphinscheduler.api.dto.resources.RenameDirectoryRequest;
import org.apache.dolphinscheduler.api.dto.resources.RenameFileRequest;
import org.apache.dolphinscheduler.api.dto.resources.ResourceComponent;
import org.apache.dolphinscheduler.api.dto.resources.UpdateFileFromContentRequest;
import org.apache.dolphinscheduler.api.dto.resources.UpdateFileRequest;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ResourcesService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.ResourceItemVO;
import org.apache.dolphinscheduler.api.vo.resources.FetchFileContentResponse;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.io.Files;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "RESOURCES_TAG")
@RestController
@RequestMapping("resources")
@Slf4j
public class ResourcesController extends BaseController {

    @Autowired
    private ResourcesService resourceService;

    @Operation(summary = "queryResourceList", description = "QUERY_RESOURCE_LIST_NOTES")
    @Parameter(name = "type", description = "RESOURCE_TYPE", required = true, schema = @Schema(implementation = ResourceType.class))
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_RESOURCES_LIST_ERROR)
    public Result<List<ResourceComponent>> queryResourceList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                             @RequestParam(value = "type") ResourceType type) {
        return Result.success(resourceService.queryResourceFiles(loginUser, type));
    }

    @Operation(summary = "createDirectory", description = "CREATE_RESOURCE_NOTES")
    @Parameters({
            @Parameter(name = "type", description = "RESOURCE_TYPE", required = true, schema = @Schema(implementation = ResourceType.class)),
            @Parameter(name = "name", description = "RESOURCE_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "currentDir", description = "RESOURCE_CURRENT_DIR", required = true, schema = @Schema(implementation = String.class))})
    @PostMapping(value = "/directory")
    @ApiException(CREATE_RESOURCE_ERROR)
    @OperatorLog(auditType = AuditType.FOLDER_CREATE)
    public Result<Void> createDirectory(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @RequestParam(value = "type") ResourceType type,
                                        @RequestParam(value = "name") String directoryName,
                                        @RequestParam(value = "currentDir") String parentDirectory) {

        CreateDirectoryRequest createDirectoryRequest = CreateDirectoryRequest.builder()
                .loginUser(loginUser)
                .directoryName(directoryName)
                .type(type)
                .parentAbsoluteDirectory(parentDirectory)
                .build();
        resourceService.createDirectory(createDirectoryRequest);
        return Result.success(null);
    }

    @Operation(summary = "uploadFile", description = "CREATE_FILE")
    @Parameters({
            @Parameter(name = "type", description = "RESOURCE_TYPE", required = true, schema = @Schema(implementation = ResourceType.class)),
            @Parameter(name = "name", description = "RESOURCE_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "file", description = "RESOURCE_FILE", required = true, schema = @Schema(implementation = MultipartFile.class)),
            @Parameter(name = "currentDir", description = "RESOURCE_CURRENT_DIR", required = true, schema = @Schema(implementation = String.class))})
    @PostMapping()
    @ApiException(CREATE_RESOURCE_ERROR)
    @OperatorLog(auditType = AuditType.FILE_CREATE)
    public Result<Void> createFile(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam(value = "type") ResourceType type,
                                   @RequestParam(value = "name") String fileName,
                                   @RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "currentDir") String parentDirectoryAbsolutePath) {

        CreateFileRequest uploadFileRequest = CreateFileRequest.builder()
                .loginUser(loginUser)
                .fileName(fileName)
                .file(file)
                .type(type)
                .parentAbsoluteDirectory(parentDirectoryAbsolutePath)
                .build();
        resourceService.createFile(uploadFileRequest);
        return Result.success();
    }

    @Operation(summary = "createFileFromContent", description = "ONLINE_CREATE_RESOURCE_NOTES")
    @Parameters({
            @Parameter(name = "type", description = "RESOURCE_TYPE", required = true, schema = @Schema(implementation = ResourceType.class)),
            @Parameter(name = "fileName", description = "RESOURCE_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "suffix", description = "SUFFIX", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "description", description = "RESOURCE_DESC", schema = @Schema(implementation = String.class)),
            @Parameter(name = "content", description = "CONTENT", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "currentDir", description = "RESOURCE_CURRENTDIR", required = true, schema = @Schema(implementation = String.class))})
    @PostMapping(value = "/online-create")
    @ApiException(CREATE_RESOURCE_FILE_ON_LINE_ERROR)
    @OperatorLog(auditType = AuditType.FILE_CREATE)
    public Result<Void> createFileFromContent(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @RequestParam(value = "type") ResourceType type,
                                              @RequestParam(value = "fileName") String fileName,
                                              @RequestParam(value = "suffix") String fileSuffix,
                                              @RequestParam(value = "content") String fileContent,
                                              @RequestParam(value = "currentDir") String fileParentDirectoryAbsolutePath) {
        CreateFileFromContentRequest createFileFromContentRequest = CreateFileFromContentRequest.builder()
                .loginUser(loginUser)
                .fileName(fileName + "." + fileSuffix)
                .fileContent(fileContent)
                .type(type)
                .parentAbsoluteDirectory(fileParentDirectoryAbsolutePath)
                .build();
        resourceService.createFileFromContent(createFileFromContentRequest);
        return Result.success();
    }

    @Operation(summary = "updateFileContent", description = "UPDATE_RESOURCE_NOTES")
    @Parameters({
            @Parameter(name = "content", description = "CONTENT", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "fullName", description = "FULL_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "tenantCode", description = "TENANT_CODE", required = true, schema = @Schema(implementation = String.class))})
    @PutMapping(value = "/update-content")
    @ApiException(EDIT_RESOURCE_FILE_ON_LINE_ERROR)
    @OperatorLog(auditType = AuditType.FILE_UPDATE)
    public Result<Void> updateFileContent(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @RequestParam(value = "fullName") String fileAbsolutePath,
                                          @RequestParam(value = "content") String fileContent) {
        UpdateFileFromContentRequest updateFileContentRequest = UpdateFileFromContentRequest.builder()
                .loginUser(loginUser)
                .fileContent(fileContent)
                .fileAbsolutePath(fileAbsolutePath)
                .build();
        resourceService.updateFileFromContent(updateFileContentRequest);
        return Result.success();
    }

    @Operation(summary = "updateResource", description = "UPDATE_RESOURCE_NOTES")
    @Parameters({
            @Parameter(name = "fullName", description = "RESOURCE_FULLNAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "tenantCode", description = "TENANT_CODE", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "type", description = "RESOURCE_TYPE", required = true, schema = @Schema(implementation = ResourceType.class)),
            @Parameter(name = "name", description = "RESOURCE_NAME", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "file", description = "RESOURCE_FILE", required = true, schema = @Schema(implementation = MultipartFile.class))})
    @PutMapping()
    @ApiException(UPDATE_RESOURCE_ERROR)
    @OperatorLog(auditType = AuditType.FILE_UPDATE)
    public Result<Void> updateResource(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam(value = "fullName") String resourceAbsolutePath,
                                       @RequestParam(value = "name") String resourceName,
                                       @RequestParam(value = "file", required = false) MultipartFile file) {
        if (StringUtils.isEmpty(Files.getFileExtension(resourceName))) {
            RenameDirectoryRequest renameDirectoryRequest = RenameDirectoryRequest.builder()
                    .loginUser(loginUser)
                    .directoryAbsolutePath(resourceAbsolutePath)
                    .newDirectoryName(resourceName)
                    .build();
            resourceService.renameDirectory(renameDirectoryRequest);
            return Result.success();
        }

        if (file == null) {
            RenameFileRequest renameFileRequest = RenameFileRequest.builder()
                    .loginUser(loginUser)
                    .fileAbsolutePath(resourceAbsolutePath)
                    .newFileName(resourceName)
                    .build();
            resourceService.renameFile(renameFileRequest);
            return Result.success();
        }
        UpdateFileRequest updateFileRequest = UpdateFileRequest.builder()
                .loginUser(loginUser)
                .fileAbsolutePath(resourceAbsolutePath)
                .file(file)
                .build();
        resourceService.updateFile(updateFileRequest);
        return Result.success();
    }

    @Operation(summary = "pagingResourceItemRequest", description = "PAGING_RESOURCE_ITEM_LIST")
    @Parameters({
            @Parameter(name = "type", description = "RESOURCE_TYPE", required = true, schema = @Schema(implementation = ResourceType.class)),
            @Parameter(name = "fullName", description = "RESOURCE_FULLNAME", required = true, schema = @Schema(implementation = String.class, example = "bucket_name/tenant_name/type/ds")),
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "20"))})
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_RESOURCES_LIST_PAGING)
    public Result<PageInfo<ResourceItemVO>> pagingResourceItemRequest(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                      @RequestParam(value = "fullName") String resourceAbsolutePath,
                                                                      @RequestParam(value = "type") ResourceType resourceType,
                                                                      @RequestParam(value = "searchVal", required = false) String resourceNameKeyWord,
                                                                      @RequestParam("pageNo") Integer pageNo,
                                                                      @RequestParam("pageSize") Integer pageSize) {
        PagingResourceItemRequest pagingResourceItemRequest = PagingResourceItemRequest.builder()
                .loginUser(loginUser)
                .resourceAbsolutePath(resourceAbsolutePath)
                .resourceType(resourceType)
                .resourceNameKeyWord(StringUtils.trim(ParameterUtils.handleEscapes(resourceNameKeyWord)))
                .pageNo(pageNo)
                .pageSize(pageSize)
                .build();
        pagingResourceItemRequest.checkPageNoAndPageSize();

        return Result.success(resourceService.pagingResourceItem(pagingResourceItemRequest));
    }

    @Operation(summary = "deleteResource", description = "DELETE_RESOURCE_BY_ID_NOTES")
    @Parameters({
            @Parameter(name = "fullName", description = "RESOURCE_FULLNAME", required = true, schema = @Schema(implementation = String.class, example = "file:////tmp/dolphinscheduler/storage/default/resources/demo.sql"))
    })
    @DeleteMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_RESOURCE_ERROR)
    @OperatorLog(auditType = AuditType.FILE_DELETE)
    public Result<Void> deleteResource(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam(value = "fullName") String resourceAbsolutePath) {
        DeleteResourceRequest deleteResourceRequest = DeleteResourceRequest.builder()
                .loginUser(loginUser)
                .resourceAbsolutePath(resourceAbsolutePath)
                .build();
        resourceService.delete(deleteResourceRequest);
        return Result.success();
    }

    @Operation(summary = "queryResourceFileList", description = "QUERY_RESOURCE_FILE_LIST_NOTES")
    @Parameters({
            @Parameter(name = "type", description = "RESOURCE_TYPE", required = true, schema = @Schema(implementation = ResourceType.class))})
    @GetMapping(value = "/query-by-type")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_RESOURCES_LIST_ERROR)
    public Result<List<ResourceComponent>> queryResourceFileList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                 @RequestParam(value = "type") ResourceType type) {
        return Result.success(resourceService.queryResourceFiles(loginUser, type));
    }

    @Operation(summary = "viewResource", description = "VIEW_RESOURCE_BY_ID_NOTES")
    @Parameters({
            @Parameter(name = "fullName", description = "RESOURCE_FULL_NAME", required = true, schema = @Schema(implementation = String.class, example = "tenant/1.png")),
            @Parameter(name = "tenantCode", description = "TENANT_CODE", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "skipLineNum", description = "SKIP_LINE_NUM", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "limit", description = "LIMIT", required = true, schema = @Schema(implementation = int.class, example = "100"))})
    @GetMapping(value = "/view")
    @ApiException(VIEW_RESOURCE_FILE_ON_LINE_ERROR)
    public Result<FetchFileContentResponse> viewResource(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                         @RequestParam(value = "fullName") String resourceAbsoluteFilePath,
                                                         @RequestParam(value = "skipLineNum") int skipLineNum,
                                                         @RequestParam(value = "limit") int limit) {
        FetchFileContentRequest fetchFileContentRequest = FetchFileContentRequest.builder()
                .loginUser(loginUser)
                .resourceFileAbsolutePath(resourceAbsoluteFilePath)
                .limit(limit == -1 ? Integer.MAX_VALUE : skipLineNum)
                .skipLineNum(skipLineNum)
                .build();
        return Result.success(resourceService.fetchResourceFileContent(fetchFileContentRequest));
    }

    @Operation(summary = "downloadResource", description = "DOWNLOAD_RESOURCE_NOTES")
    @Parameters({
            @Parameter(name = "fullName", description = "RESOURCE_FULLNAME", required = true, schema = @Schema(implementation = String.class, example = "test/"))})
    @GetMapping(value = "/download")
    @ResponseBody
    @ApiException(DOWNLOAD_RESOURCE_FILE_ERROR)
    public void downloadResource(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 HttpServletResponse response,
                                 @RequestParam(value = "fullName") String fileAbsolutePath) {

        DownloadFileRequest downloadFileRequest = DownloadFileRequest.builder()
                .loginUser(loginUser)
                .fileAbsolutePath(fileAbsolutePath)
                .build();

        resourceService.downloadResource(response, downloadFileRequest);
    }

    @Operation(summary = "queryResourceBaseDir", description = "QUERY_RESOURCE_BASE_DIR")
    @Parameters({
            @Parameter(name = "type", description = "RESOURCE_TYPE", required = true, schema = @Schema(implementation = ResourceType.class))})
    @GetMapping(value = "/base-dir")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(RESOURCE_NOT_EXIST)
    public Result<String> queryResourceBaseDir(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @RequestParam(value = "type") ResourceType type) {
        return Result.success(resourceService.queryResourceBaseDir(loginUser, type));
    }
}
