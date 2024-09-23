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

import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// StorageEntity is an entity representing a resource in the third-part storage service.
// It is only stored in t_ds_relation_resources_task if the resource is used by a task.
// It is not put in the model module because it has more attributes than corresponding objects stored
//  in table t_ds_relation_resources_task.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageEntity {

    private String fullName;

    private String fileName;
    private String pfullName;
    private boolean isDirectory;
    private ResourceType type;
    private long size;
    private Date createTime;
    private Date updateTime;
}
