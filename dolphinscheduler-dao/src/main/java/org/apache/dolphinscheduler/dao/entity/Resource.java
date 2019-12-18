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
package org.apache.dolphinscheduler.dao.entity;


import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.dolphinscheduler.common.enums.ResourceType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@NoArgsConstructor
@ToString
@TableName("t_ds_resources")
public class Resource {
    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    private String alias;
    private String description;
    private String fileName;
    private int userId;
    private ResourceType type;
    private long size;
    private Date createTime;
    private Date updateTime;

    public Resource(int id, String alias, String fileName, String description, int userId,
                    ResourceType type, long size,
                    Date createTime, Date updateTime) {
        this.id = id;
        this.alias = alias;
        this.fileName = fileName;
        this.description = description;
        this.userId = userId;
        this.type = type;
        this.size = size;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public Resource(String alias, String fileName, String description, int userId, ResourceType type, long size, Date createTime, Date updateTime) {
        this.alias = alias;
        this.fileName = fileName;
        this.description = description;
        this.userId = userId;
        this.type = type;
        this.size = size;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Resource resource = (Resource) o;

        if (id != resource.id) {
            return false;
        }
        return alias.equals(resource.alias);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + alias.hashCode();
        return result;
    }
}
