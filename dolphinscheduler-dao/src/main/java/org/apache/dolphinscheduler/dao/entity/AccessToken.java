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

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("t_ds_access_token")
public class AccessToken {

    /**
     * primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * user_id
     */
    @TableField(value = "user_id")
    private int userId;
    /**
     * token
     */
    @TableField(value = "token")
    private String token;
    /**
     * expire_time
     */
    @TableField(value = "expire_time")
    private Date expireTime;
    /**
     * create_time
     */
    @TableField(value = "create_time")
    private Date createTime;
    /**
     * update_time
     */
    @TableField(value = "update_time")
    private Date updateTime;
    @TableField(exist = false)
    private String userName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccessToken that = (AccessToken) o;

        if (id != that.id) {
            return false;
        }
        if (userId != that.userId) {
            return false;
        }
        if (userName != null && !userName.equals(that.userName)) {
            return false;
        }
        if (token != null && !token.equals(that.token)) {
            return false;
        }
        if (expireTime != null && !expireTime.equals(that.expireTime)) {
            return false;
        }
        if (createTime != null && !createTime.equals(that.createTime)) {
            return false;
        }
        if (updateTime != null && !updateTime.equals(that.updateTime)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + userId;
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (expireTime != null ? expireTime.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (updateTime != null ? updateTime.hashCode() : 0);
        return result;
    }
}
