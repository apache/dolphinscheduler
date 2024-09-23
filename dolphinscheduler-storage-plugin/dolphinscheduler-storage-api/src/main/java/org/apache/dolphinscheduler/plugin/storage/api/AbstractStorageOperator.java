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

package org.apache.dolphinscheduler.plugin.storage.api;

import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.storage.api.constants.StorageConstants;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;

public abstract class AbstractStorageOperator implements StorageOperator {

    private static final Logger log = LoggerFactory.getLogger(AbstractStorageOperator.class);
    protected final String resourceBaseAbsolutePath;

    public AbstractStorageOperator(String resourceBaseAbsolutePath) {
        Preconditions.checkNotNull(resourceBaseAbsolutePath, "Resource upload path should not be null");
        this.resourceBaseAbsolutePath = resourceBaseAbsolutePath;
    }

    @Override
    public ResourceMetadata getResourceMetaData(String resourceAbsolutePath) {
        String storageBaseDirectory = getStorageBaseDirectory();
        String resourceSegment = StringUtils.substringAfter(resourceAbsolutePath, storageBaseDirectory);
        String[] segments = StringUtils.split(resourceSegment, File.separator, 3);
        if (segments.length == 0) {
            throw new IllegalArgumentException("Invalid resource path: " + resourceAbsolutePath);
        }
        return ResourceMetadata.builder()
                .resourceAbsolutePath(resourceAbsolutePath)
                .resourceBaseDirectory(storageBaseDirectory)
                .isDirectory(Files.getFileExtension(resourceAbsolutePath).isEmpty())
                .tenant(segments[0])
                .resourceType(ResourceType.FILE)
                .resourceRelativePath(segments.length == 2 ? "/" : segments[2])
                .resourceParentAbsolutePath(StringUtils.substringBeforeLast(resourceAbsolutePath, File.separator))
                .build();
    }

    @Override
    public String getStorageBaseDirectory() {
        // All directory should end with File.separator
        return PropertyUtils.getString(StorageConstants.RESOURCE_UPLOAD_PATH, "/tmp/dolphinscheduler");
    }

    @Override
    public String getStorageBaseDirectory(String tenantCode) {
        if (StringUtils.isEmpty(tenantCode)) {
            throw new IllegalArgumentException("Tenant code should not be empty");
        }
        // All directory should end with File.separator
        return FileUtils.concatFilePath(getStorageBaseDirectory(), tenantCode);
    }

    @Override
    public String getStorageBaseDirectory(String tenantCode, ResourceType resourceType) {
        String tenantBaseDirectory = getStorageBaseDirectory(tenantCode);
        if (resourceType == null) {
            throw new IllegalArgumentException("Resource type should not be null");
        }
        String resourceBaseDirectory;
        switch (resourceType) {
            case FILE:
                resourceBaseDirectory = FileUtils.concatFilePath(tenantBaseDirectory, FILE_FOLDER_NAME);
                break;
            case ALL:
                resourceBaseDirectory = tenantBaseDirectory;
                break;
            default:
                throw new IllegalArgumentException("Resource type: " + resourceType + " not supported");
        }
        // All directory should end with File.separator
        return resourceBaseDirectory;
    }

    @Override
    public String getStorageFileAbsolutePath(String tenantCode, String fileName) {
        return FileUtils.concatFilePath(getStorageBaseDirectory(tenantCode, ResourceType.FILE), fileName);
    }

    protected void exceptionIfPathEmpty(String resourceAbsolutePath) {
        if (StringUtils.isEmpty(resourceAbsolutePath)) {
            throw new IllegalArgumentException("Resource path should not be empty");
        }
    }

}
