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

import org.apache.dolphinscheduler.api.dto.resources.UpdateFileDto;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.TenantDao;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.io.Files;

@Component
public class UpdateFileDtoValidator extends AbstractResourceValidator<UpdateFileDto> {

    public UpdateFileDtoValidator(StorageOperator storageOperator, TenantDao tenantDao) {
        super(storageOperator, tenantDao);
    }

    @Override
    public void validate(UpdateFileDto updateFileDto) {
        String fileAbsolutePath = updateFileDto.getFileAbsolutePath();
        User loginUser = updateFileDto.getLoginUser();
        MultipartFile file = updateFileDto.getFile();

        if (!Objects.equals(
                Files.getFileExtension(
                        file.getOriginalFilename() == null ? file.getName() : file.getOriginalFilename()),
                Files.getFileExtension(updateFileDto.getFileAbsolutePath()))) {
            throw new ServiceException("file extension cannot not change");
        }

        exceptionResourceAbsolutePathInvalidated(fileAbsolutePath);
        exceptionResourceNotExists(fileAbsolutePath);
        exceptionResourceIsNotFile(fileAbsolutePath);
        exceptionUserNoResourcePermission(loginUser, fileAbsolutePath);
        exceptionFileInvalidated(file);
    }
}
