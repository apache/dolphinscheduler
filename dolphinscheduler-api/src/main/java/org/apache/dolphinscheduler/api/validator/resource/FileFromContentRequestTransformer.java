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

package org.apache.dolphinscheduler.api.validator.resource;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.api.dto.resources.CreateFileFromContentDto;
import org.apache.dolphinscheduler.api.dto.resources.CreateFileFromContentRequest;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.dao.repository.TenantDao;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;

import org.springframework.stereotype.Component;

@Component
public class FileFromContentRequestTransformer
        extends
            AbstractResourceTransformer<CreateFileFromContentRequest, CreateFileFromContentDto> {

    public FileFromContentRequestTransformer(TenantDao tenantDao, StorageOperator storageOperator) {
        super(tenantDao, storageOperator);
    }

    @Override
    public CreateFileFromContentDto transform(CreateFileFromContentRequest createFileFromContentRequest) {
        validateCreateFileRequest(createFileFromContentRequest);
        return doTransform(createFileFromContentRequest);
    }

    private void validateCreateFileRequest(CreateFileFromContentRequest createFileFromContentRequest) {
        checkNotNull(createFileFromContentRequest.getLoginUser(), "loginUser is null");
        checkNotNull(createFileFromContentRequest.getType(), "resource type is null");
        checkNotNull(createFileFromContentRequest.getFileName(), "file name is null");
        checkNotNull(createFileFromContentRequest.getParentAbsoluteDirectory(), "parent directory is null");
        checkNotNull(createFileFromContentRequest.getFileContent(), "file content is null");
    }

    private CreateFileFromContentDto doTransform(CreateFileFromContentRequest createFileFromContentRequest) {
        String fileAbsolutePath = getFileAbsolutePath(createFileFromContentRequest);
        return CreateFileFromContentDto.builder()
                .loginUser(createFileFromContentRequest.getLoginUser())
                .fileAbsolutePath(fileAbsolutePath)
                .fileContent(createFileFromContentRequest.getFileContent())
                .build();

    }

    private String getFileAbsolutePath(CreateFileFromContentRequest createFileFromContentRequest) {
        String parentDirectoryAbsolutePath = getParentDirectoryAbsolutePath(
                createFileFromContentRequest.getLoginUser(),
                createFileFromContentRequest.getParentAbsoluteDirectory(),
                createFileFromContentRequest.getType());
        return FileUtils.concatFilePath(parentDirectoryAbsolutePath, createFileFromContentRequest.getFileName());
    }

}
