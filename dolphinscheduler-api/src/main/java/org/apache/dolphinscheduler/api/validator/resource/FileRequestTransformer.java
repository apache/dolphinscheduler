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

import org.apache.dolphinscheduler.api.dto.resources.CreateFileDto;
import org.apache.dolphinscheduler.api.dto.resources.CreateFileRequest;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.dao.repository.TenantDao;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;

import org.springframework.stereotype.Component;

@Component
public class FileRequestTransformer extends AbstractResourceTransformer<CreateFileRequest, CreateFileDto> {

    public FileRequestTransformer(TenantDao tenantDao, StorageOperator storageOperator) {
        super(tenantDao, storageOperator);
    }

    @Override
    public CreateFileDto transform(CreateFileRequest createFileRequest) {
        validateCreateFileRequest(createFileRequest);
        return doTransform(createFileRequest);
    }

    private void validateCreateFileRequest(CreateFileRequest createFileRequest) {
        checkNotNull(createFileRequest.getLoginUser(), "loginUser is null");
        checkNotNull(createFileRequest.getType(), "resource type is null");
        checkNotNull(createFileRequest.getFileName(), "file name is null");
        checkNotNull(createFileRequest.getParentAbsoluteDirectory(), "parent directory is null");
        checkNotNull(createFileRequest.getFile(), "file is null");
    }

    private CreateFileDto doTransform(CreateFileRequest createFileRequest) {
        String fileAbsolutePath = getFileAbsolutePath(createFileRequest);
        return CreateFileDto.builder()
                .loginUser(createFileRequest.getLoginUser())
                .file(createFileRequest.getFile())
                .fileAbsolutePath(fileAbsolutePath)
                .build();

    }

    private String getFileAbsolutePath(CreateFileRequest createFileRequest) {
        String parentDirectoryAbsolutePath = getParentDirectoryAbsolutePath(
                createFileRequest.getLoginUser(),
                createFileRequest.getParentAbsoluteDirectory(),
                createFileRequest.getType());
        return FileUtils.concatFilePath(parentDirectoryAbsolutePath, createFileRequest.getFileName());
    }
}
