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
package cn.escheduler.api.dto.resources;

import cn.escheduler.common.enums.ResourceType;
import com.alibaba.fastjson.annotation.JSONType;

import java.util.ArrayList;
import java.util.List;

/**
 * Resource View Dto
 */
@JSONType(orders={"id","pid","alias","type","children"})
public class ResourceViewDto {
    private int id;
    private int pid;
    private String alias;
    private ResourceType type;

    public ResourceViewDto() {
    }

    public ResourceViewDto(int id, int pid, String alias, ResourceType type) {
        this.id = id;
        this.pid = pid;
        this.alias = alias;
        this.type = type;
    }

    /**
     * children
     */
    private List<ResourceViewDto> children = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public List<ResourceViewDto> getChildren() {
        return children;
    }

    public void setChildren(List<ResourceViewDto> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "ResourceViewDto{" +
                "id=" + id +
                ", pid=" + pid +
                ", alias='" + alias + '\'' +
                ", type=" + type +
                ", children=" + children +
                '}';
    }
}
