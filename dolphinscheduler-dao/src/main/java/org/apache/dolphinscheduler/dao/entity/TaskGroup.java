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

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_ds_task_group")
public class TaskGroup implements Serializable {

    /**
     * key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * task_group name
     */
    private String name;

    private String description;
    /**
     * 作业组大小
     */
    private int groupSize;
    /**
     * 已使用作业组大小
     */
    private int useSize;
    /**
     * creator id
     */
    private int userId;
    /**
     * 0 not available, 1 available
     */
    private Integer status;
    /**
     * create time
     */
    private Date createTime;
    /**
     * update time
     */
    private Date updateTime;
    /**
     * project code
     */
    private long projectCode;

}
