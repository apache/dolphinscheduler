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
package org.apache.dolphinscheduler.server.entity;

import org.apache.dolphinscheduler.common.enums.ResourceType;
import org.apache.dolphinscheduler.dao.entity.Resource;

import java.util.Date;

/**
 * master/worker task transport
 */
public class TaskResourceDownloadContext {

    /**
     * resource type
     */
    private ResourceType resourceType;

    /**
     * download relative id
     */
    private Integer id;

    /**
     * download relative name
     */
    private String name;

    /**
     * resource full name
     */
    private String fullName;

    /**
     * resource tenant code
     */
    private String tenantCode;

    /**
     * update time
     */
    private Date updateTime;

    public TaskResourceDownloadContext() {
    }

    public TaskResourceDownloadContext(Resource resource) {
        this.resourceType = resource.getType();
        this.id = resource.getId();
        this.name = resource.getAlias();
        this.fullName = resource.getFullName();
        this.tenantCode = resource.getTenantCode();
        this.updateTime = resource.getUpdateTime();
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
