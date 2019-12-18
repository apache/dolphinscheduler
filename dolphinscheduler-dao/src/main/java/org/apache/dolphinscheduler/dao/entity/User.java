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


import lombok.ToString;
import org.apache.dolphinscheduler.common.enums.UserType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * user
 */
@Data
@ToString
@TableName("t_ds_user")
@ApiModel(description = "UserModelDesc")
public class User {
    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    @ApiModelProperty(name = "userName", notes = "USER_NAME", dataType = "String", required = true)
    private String userName;
    @ApiModelProperty(name = "userPassword", notes = "USER_PASSWORD", dataType = "String", required = true)
    private String userPassword;
    private String email;
    private String phone;
    private UserType userType;
    private int tenantId;
    @TableField(exist = false)
    private String tenantCode;
    @TableField(exist = false)
    private String tenantName;
    @TableField(exist = false)
    private String queueName;
    @TableField(exist = false)
    private String alertGroup;
    private String queue;
    private Date createTime;
    private Date updateTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User user = (User) o;

        if (id != user.id) {
            return false;
        }
        return userName.equals(user.userName);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + userName.hashCode();
        return result;
    }
}
