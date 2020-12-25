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

import org.apache.dolphinscheduler.common.enums.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@TableName("t_ds_datasource")
public class DataSource {
  /**
   * id
   */
  @TableId(value="id", type=IdType.AUTO)
  private int id;

  /**
   * user id
   */
  private int userId;

  /**
   * user name
   */
  @TableField(exist = false)
  private String userName;

  /**
   * data source name
   */
  private String name;

  /**
   * note
   */
  private String note;

  /**
   * data source type
   */
  private DbType type;

  /**
   * connection parameters
   */
  private String connectionParams;

  /**
   * create time
   */
  private Date createTime;

  /**
   * update time
   */
  private Date updateTime;

  public DataSource() {
  }


  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public DbType getType() {
    return type;
  }

  public void setType(DbType type) {
    this.type = type;
  }

  public String getConnectionParams() {
    return connectionParams;
  }

  public void setConnectionParams(String connectionParams) {
    this.connectionParams = connectionParams;
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
    return "DataSource{" +
            "id=" + id +
            ", userId=" + userId +
            ", userName='" + userName + '\'' +
            ", name='" + name + '\'' +
            ", note='" + note + '\'' +
            ", type=" + type +
            ", connectionParams='" + connectionParams + '\'' +
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

    DataSource that = (DataSource) o;

    if (id != that.id) {
        return false;
    }
    return name.equals(that.name);

  }

  @Override
  public int hashCode() {
    int result = id;
    result = 31 * result + name.hashCode();
    return result;
  }
}
