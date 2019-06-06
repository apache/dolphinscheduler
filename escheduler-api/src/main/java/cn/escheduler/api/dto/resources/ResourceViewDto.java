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
import cn.escheduler.dao.model.Resource;
import com.alibaba.fastjson.annotation.JSONType;

import java.util.ArrayList;
import java.util.List;

/**
 * Resource View Dto
 */
@JSONType(orders={"id","pid","alias","type","permission","children"})
public class ResourceViewDto implements Comparable<ResourceViewDto>{
    /**
     * id
     */
    private int id;
    /**
     * parent id
     */
    private int pid;
    /**
     * resource aliases are used for uploading, downloading, and so on
     */
    private String alias;
    /**
     * resource type
     */
    private ResourceType type;
    /**
     * 1 has permission;0 no permission
     */
    private int permission;

    public ResourceViewDto() {
    }

    public ResourceViewDto(Resource resource) {
        if (resource != null) {
            this.setAlias(resource.getAlias());
            this.setId(resource.getId());
            this.setPid(resource.getPid());
            this.setType(resource.getType());
        }
    }

    public ResourceViewDto(Resource resource,int permission) {
        if (resource != null) {
            this.setAlias(resource.getAlias());
            this.setId(resource.getId());
            this.setPid(resource.getPid());
            this.setType(resource.getType());
            this.setPermission(permission);
        }
    }

    public ResourceViewDto(int id, int pid, String alias, ResourceType type) {
        this.id = id;
        this.pid = pid;
        this.alias = alias;
        this.type = type;
    }

    public ResourceViewDto(int id, int pid, String alias, ResourceType type, int permission) {
        this.id = id;
        this.pid = pid;
        this.alias = alias;
        this.type = type;
        this.permission = permission;
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

    public int getPermission() {
        return permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
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
                ", permission=" + permission +
                ", children=" + children +
                '}';
    }


    @Override
    public int compareTo(ResourceViewDto o) {
        //return o.getType().compareTo(this.type);
        return this.type.compareTo(o.getType());
    }
}
