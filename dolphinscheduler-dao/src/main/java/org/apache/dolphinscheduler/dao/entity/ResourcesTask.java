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

package org.apache.dolphinscheduler.dao.entity;

import org.apache.dolphinscheduler.spi.enums.ResourceType;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("t_ds_relation_resources_task")
public class ResourcesTask {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String fullName;

    private int taskId;

    private ResourceType type;

    public ResourcesTask(int id, String fullName, int taskId, ResourceType type) {
        this.id = id;
        this.fullName = fullName;
        this.taskId = taskId;
        this.type = type;
    }

    public ResourcesTask(int taskId, String fullName, ResourceType type) {
        this.taskId = taskId;
        this.fullName = fullName;
        this.type = type;
    }
}
