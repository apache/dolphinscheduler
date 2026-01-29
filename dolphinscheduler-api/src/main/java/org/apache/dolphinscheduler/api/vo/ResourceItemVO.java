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

package org.apache.dolphinscheduler.api.vo;

import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceItemVO {

    // todo: remove this field, directly use fileName
    private String alias;

    // todo: use tenantName instead of userName
    private String userName;

    private String fileName;

    private String fullName;

    private boolean isDirectory;

    private ResourceType type;

    private long size;

    private Date createTime;

    private Date updateTime;

    public ResourceItemVO(StorageEntity storageEntity) {
        this.isDirectory = storageEntity.isDirectory();
        this.alias = storageEntity.getFileName();
        this.fileName = storageEntity.getFileName();
        this.fullName = storageEntity.getFullName();
        this.type = storageEntity.getType();
        this.size = storageEntity.getSize();
        this.createTime = storageEntity.getCreateTime();
        this.updateTime = storageEntity.getUpdateTime();

        if (isDirectory) {
            alias = StringUtils.removeEndIgnoreCase(alias, File.separator);
            fileName = StringUtils.removeEndIgnoreCase(fileName, File.separator);
            fullName = StringUtils.removeEndIgnoreCase(fullName, File.separator);
        }
    }

}
