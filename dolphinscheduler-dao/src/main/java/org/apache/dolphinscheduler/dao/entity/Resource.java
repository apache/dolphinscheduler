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


import org.apache.dolphinscheduler.common.enums.ResourceType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@TableName("t_ds_resources")
public class Resource {
  /**
   * id
   */
  @TableId(value="id", type=IdType.AUTO)
  private int id;

  /**
   * parent id
   */
  private int pid;

  /**
   * resource alias
   */
  private String alias;

  /**
   * full name
   */
  private String fullName;

  /**
   * is directory
   */
  private boolean isDirectory=false;

  /**
   * description
   */
  private String description;

  /**
   * file alias
   */
  private String fileName;

  /**
   * user id
   */
  private int userId;

  /**
   * resource type
   */
  private ResourceType type;

  /**
   * resource size
   */
  private long size;

  /**
   * create time
   */
  private Date createTime;

  /**
   * update time
   */
  private Date updateTime;

  public Resource() {
  }

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

  public Resource(int id, int pid, String alias, String fullName, boolean isDirectory) {
    this.id = id;
    this.pid = pid;
    this.alias = alias;
    this.fullName = fullName;
    this.isDirectory = isDirectory;
  }

  /*public Resource(String alias, String fileName, String description, int userId, ResourceType type, long size, Date createTime, Date updateTime) {
    this.alias = alias;
    this.fileName = fileName;
    this.description = description;
    this.userId = userId;
    this.type = type;
    this.size = size;
    this.createTime = createTime;
    this.updateTime = updateTime;
  }*/

  public Resource(int pid, String alias, String fullName, boolean isDirectory, String description, String fileName, int userId, ResourceType type, long size, Date createTime, Date updateTime) {
    this.pid = pid;
    this.alias = alias;
    this.fullName = fullName;
    this.isDirectory = isDirectory;
    this.description = description;
    this.fileName = fileName;
    this.userId = userId;
    this.type = type;
    this.size = size;
    this.createTime = createTime;
    this.updateTime = updateTime;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public int getPid() {
    return pid;
  }

  public void setPid(int pid) {
    this.pid = pid;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public boolean isDirectory() {
    return isDirectory;
  }

  public void setDirectory(boolean directory) {
    isDirectory = directory;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }


  public ResourceType getType() {
    return type;
  }

  public void setType(ResourceType type) {
    this.type = type;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
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

  @Override
  public String toString() {
    return "Resource{" +
            "id=" + id +
            ", pid=" + pid +
            ", alias='" + alias + '\'' +
            ", fullName='" + fullName + '\'' +
            ", isDirectory=" + isDirectory +
            ", description='" + description + '\'' +
            ", fileName='" + fileName + '\'' +
            ", userId=" + userId +
            ", type=" + type +
            ", size=" + size +
            ", createTime=" + createTime +
            ", updateTime=" + updateTime +
            '}';
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
