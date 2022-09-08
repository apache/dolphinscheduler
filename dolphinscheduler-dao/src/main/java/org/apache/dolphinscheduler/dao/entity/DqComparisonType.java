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

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("t_ds_dq_comparison_type")
public class DqComparisonType implements Serializable {

    /**
     * primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * type
     */
    @TableField(value = "type")
    private String type;
    /**
     * execute sql
     */
    @TableField(value = "execute_sql")
    private String executeSql;
    /**
     * output table
     */
    @TableField(value = "output_table")
    private String outputTable;
    /**
     * comparison name
     */
    @TableField(value = "name")
    private String name;
    /**
     * is inner source
     */
    @TableField(value = "is_inner_source")
    private Boolean isInnerSource;
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
}
