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

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;


/**
 * process definition version
 */
@TableName("t_ds_process_definition_version")
public class ProcessDefinitionVersion {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    /**
     * process definition id
     */
    private int processDefinitionId;

    /**
     * version
     */
    private long version;

    /**
     * definition json string
     */
    private String processDefinitionJson;

    /**
     * description
     */
    private String description;

    /**
     * process warning time out. unit: minute
     */
    private int timeout;

    /**
     * resource ids
     */
    private String resourceIds;

    /**
     * create time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * user defined parameters
     */
    private String globalParams;

    /**
     * locations array for web
     */
    private String locations;

    /**
     * connects array for web
     */
    private String connects;


    /**
     * warningGroupId
     */
    @TableField(exist = false)
    private int warningGroupId;

    public String getGlobalParams() {
        return globalParams;
    }

    public void setGlobalParams(String globalParams) {
        this.globalParams = globalParams;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(int processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getProcessDefinitionJson() {
        return processDefinitionJson;
    }

    public void setProcessDefinitionJson(String processDefinitionJson) {
        this.processDefinitionJson = processDefinitionJson;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getLocations() {
        return locations;
    }

    public void setLocations(String locations) {
        this.locations = locations;
    }

    public String getConnects() {
        return connects;
    }

    public void setConnects(String connects) {
        this.connects = connects;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(String resourceIds) {
        this.resourceIds = resourceIds;
    }

    public int getWarningGroupId() {
        return warningGroupId;
    }

    public void setWarningGroupId(int warningGroupId) {
        this.warningGroupId = warningGroupId;
    }

    @Override
    public String toString() {
        return "ProcessDefinitionVersion{"
            + "id=" + id
            + ", processDefinitionId=" + processDefinitionId
            + ", version=" + version
            + ", processDefinitionJson='" + processDefinitionJson + '\''
            + ", description='" + description + '\''
            + ", globalParams='" + globalParams + '\''
            + ", createTime=" + createTime
            + ", locations='" + locations + '\''
            + ", connects='" + connects + '\''
            + ", timeout=" + timeout
            + ", warningGroupId=" + warningGroupId
            + ", resourceIds='" + resourceIds + '\''
            + '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private int id;
        private int processDefinitionId;
        private long version;
        private String processDefinitionJson;
        private String description;
        private String globalParams;
        private Date createTime;
        private String locations;
        private String connects;
        private int timeout;
        private int warningGroupId;
        private String resourceIds;

        private Builder() {
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder processDefinitionId(int processDefinitionId) {
            this.processDefinitionId = processDefinitionId;
            return this;
        }

        public Builder version(long version) {
            this.version = version;
            return this;
        }

        public Builder processDefinitionJson(String processDefinitionJson) {
            this.processDefinitionJson = processDefinitionJson;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder globalParams(String globalParams) {
            this.globalParams = globalParams;
            return this;
        }

        public Builder createTime(Date createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder locations(String locations) {
            this.locations = locations;
            return this;
        }

        public Builder connects(String connects) {
            this.connects = connects;
            return this;
        }

        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder warningGroupId(int warningGroupId) {
            this.warningGroupId = warningGroupId;
            return this;
        }

        public Builder resourceIds(String resourceIds) {
            this.resourceIds = resourceIds;
            return this;
        }

        public ProcessDefinitionVersion build() {
            ProcessDefinitionVersion processDefinitionVersion = new ProcessDefinitionVersion();
            processDefinitionVersion.setId(id);
            processDefinitionVersion.setProcessDefinitionId(processDefinitionId);
            processDefinitionVersion.setVersion(version);
            processDefinitionVersion.setProcessDefinitionJson(processDefinitionJson);
            processDefinitionVersion.setDescription(description);
            processDefinitionVersion.setGlobalParams(globalParams);
            processDefinitionVersion.setCreateTime(createTime);
            processDefinitionVersion.setLocations(locations);
            processDefinitionVersion.setConnects(connects);
            processDefinitionVersion.setTimeout(timeout);
            processDefinitionVersion.setWarningGroupId(warningGroupId);
            processDefinitionVersion.setResourceIds(resourceIds);
            return processDefinitionVersion;
        }
    }
}
