/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.dolphinscheduler.plugin.storage.api;

import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.util.Date;

import lombok.Data;

// StorageEneity is an entity representing a resource in the third-part storage service.
// It is only stored in t_ds_relation_resources_task if the resource is used by a task.
// It is not put in the model module because it has more attributes than corresponding objects stored
//  in table t_ds_relation_resources_task.

@Data
public class StorageEntity {

    /**
     * exist only if it is stored in t_ds_relation_resources_task.
     *
     */
    private int id;
    /**
     * fullname is in a format of basepath + tenantCode + res/udf + filename
     */
    private String fullName;
    /**
     * filename is in a format of possible parent folders + alias
     */
    private String fileName;
    /**
     * the name of the file
     */
    private String alias;
    /**
     * parent folder time
     */
    private String pfullName;
    private boolean isDirectory;
    private int userId;
    private String userName;
    private ResourceType type;
    private long size;
    private Date createTime;
    private Date updateTime;
}
