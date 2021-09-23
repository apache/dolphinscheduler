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

/**
 * project
 */
@TableName("t_ds_project")
public class Project {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    /**
     * user id
     */
    @TableField("user_id")
    private int userId;

    /**
     * user name
     */
    @TableField(exist = false)
    private String userName;

    /**
     * project code
     */
    private long code;

    /**
     * project name
     */
    private String name;

    /**
     * project description
     */
    private String description;

    /**
     * create time
     */
    private Date createTime;

    /**
     * update time
     */
    private Date updateTime;

    /**
     * permission
     */
    @TableField(exist = false)
    private int perm;

    /**
     * process define count
     */
    @TableField(exist = false)
    private int defCount;

    /**
     * process instance running count
     */
    @TableField(exist = false)
    private int instRunningCount;

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public int getDefCount() {
        return defCount;
    }

    public void setDefCount(int defCount) {
        this.defCount = defCount;
    }

    public int getInstRunningCount() {
        return instRunningCount;
    }

    public void setInstRunningCount(int instRunningCount) {
        this.instRunningCount = instRunningCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getPerm() {
        return perm;
    }

    public void setPerm(int perm) {
        this.perm = perm;
    }

    @Override
    public String toString() {
        return "Project{"
                + "id=" + id
                + ", userId=" + userId
                + ", userName='" + userName + '\''
                + ", code=" + code
                + ", name='" + name + '\''
                + ", description='" + description + '\''
                + ", createTime=" + createTime
                + ", updateTime=" + updateTime
                + ", perm=" + perm
                + ", defCount=" + defCount
                + ", instRunningCount=" + instRunningCount
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Project project = (Project) o;

        if (id != project.id) {
            return false;
        }
        return name.equals(project.name);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        return result;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private int id;
        private int userId;
        private String userName;
        private long code;
        private String name;
        private String description;
        private Date createTime;
        private Date updateTime;
        private int perm;
        private int defCount;
        private int instRunningCount;

        private Builder() {
        }

        public Builder code(long code) {
            this.code = code;
            return this;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder userId(int userId) {
            this.userId = userId;
            return this;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder createTime(Date createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder updateTime(Date updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public Builder perm(int perm) {
            this.perm = perm;
            return this;
        }

        public Builder defCount(int defCount) {
            this.defCount = defCount;
            return this;
        }

        public Builder instRunningCount(int instRunningCount) {
            this.instRunningCount = instRunningCount;
            return this;
        }

        public Project build() {
            Project project = new Project();
            project.setId(id);
            project.setUserId(userId);
            project.setCode(code);
            project.setUserName(userName);
            project.setName(name);
            project.setDescription(description);
            project.setCreateTime(createTime);
            project.setUpdateTime(updateTime);
            project.setPerm(perm);
            project.setDefCount(defCount);
            project.setInstRunningCount(instRunningCount);
            return project;
        }
    }
}
