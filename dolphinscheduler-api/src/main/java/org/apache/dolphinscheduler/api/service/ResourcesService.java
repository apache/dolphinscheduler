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

package org.apache.dolphinscheduler.api.service;

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
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.vo.ResourceItemVO;
import org.apache.dolphinscheduler.api.vo.resources.FetchFileContentResponse;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

public interface ResourcesService {

    /**
     * Create a new directory in the resource storage, if the directory already exists will throw exception
     */
    void createDirectory(CreateDirectoryRequest createDirectoryRequest);

    /**
     * Rename the directory in the resource storage, if the origin directory not exists or the new directory already exists will throw exception.
     * <p> If the origin directory is empty will only update the directory name.
     * <p> If the origin directory is not empty will move all the files and directories to the new directory.
     * <p> After update the origin directory will be deleted.
     */
    void renameDirectory(RenameDirectoryRequest renameDirectoryRequest);

    /**
     * Upload a new file to the resource storage, if the file already exists will throw exception
     */
    void createFile(CreateFileRequest createFileRequest);

    /**
     * Update the file in the resource storage, if the origin file not exists or the new file already exists will throw exception.
     * <p> If the new file is empty will only update the file name.
     * <p> If the new file is not empty will update the file content and name.
     * <p> After update the origin file will be deleted.
     */
    void updateFile(UpdateFileRequest updateFileRequest);

    /**
     * Rename the file in the resource storage, if the origin file not exists or the new file already exists will throw exception.
     */
    void renameFile(RenameFileRequest renameFileRequest);

    /**
     * Create a new file in the resource storage, if the file already exists will throw exception.
     * Different with {@link ResourcesService#createFile(CreateFileRequest)} this method will create a new file with the given content.
     */
    void createFileFromContent(CreateFileFromContentRequest createFileFromContentRequest);

    /**
     * Update the file content.
     */
    void updateFileFromContent(UpdateFileFromContentRequest updateFileContentRequest);

    /**
     * Paging query resource items.
     * <p>If the login user is not admin will only query the resource items that under the user's tenant.
     * <p>If the login user is admin and {@link PagingResourceItemRequest##resourceAbsolutePath} is null will return all the resource items.
     */
    PageInfo<ResourceItemVO> pagingResourceItem(PagingResourceItemRequest pagingResourceItemRequest);

    /**
     * Query the resource file items by the given resource type and program type.
     */
    List<ResourceComponent> queryResourceFiles(User loginUser, ResourceType type);

    /**
     * Delete the resource item.
     * <p>If the resource item is a directory will delete all the files and directories under the directory.
     * <p>If the resource item is a file will delete the file.
     * <p>If the resource item not exists will throw exception.
     */
    void delete(DeleteResourceRequest deleteResourceRequest);

    /**
     * Fetch the file content.
     */
    FetchFileContentResponse fetchResourceFileContent(FetchFileContentRequest fetchFileContentRequest);

    void downloadResource(HttpServletResponse response, DownloadFileRequest downloadFileRequest);

    /**
     * Get resource by given resource type and file name.
     * Useful in Python API create task which need workflowDefinition information.
     *
     * @param userName user who query resource
     * @param fileName file name of the resource
     */
    StorageEntity queryFileStatus(String userName, String fileName) throws Exception;

    String queryResourceBaseDir(User loginUser, ResourceType type);

}
