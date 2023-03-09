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

import org.apache.dolphinscheduler.plugin.task.api.enums.dp.InputType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.OptionSourceType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.ValueType;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("t_ds_dq_rule_input_entry")
public class DqRuleInputEntry implements Serializable {

    /**
     * primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * form field name
     */
    @TableField(value = "field")
    private String field;
    /**
     * form type
      */
    @TableField(value = "type")
    private String type;
    /**
     * form title
     */
    @TableField(value = "title")
    private String title;
    /**
     * default value，can be null
     */
    @TableField(value = "value")
    private String value;
    /**
     * default options，can be null
     *  [{label:"",value:""}]
     */
    @TableField(value = "options")
    private String options;
    /**
     * ${field}
     */
    @TableField(value = "placeholder")
    private String placeholder;
    /**
     * the source type of options，use default options or other
     */
    @TableField(value = "option_source_type")
    private int optionSourceType = OptionSourceType.DEFAULT.getCode();
    /**
     * input entry type: string，array，number .etc
     */
    @TableField(value = "value_type")
    private int valueType = ValueType.NUMBER.getCode();
    /**
     * input entry type: default,statistics,comparison
     */
    @TableField(value = "input_type")
    private int inputType = InputType.DEFAULT.getCode();
    /**
     * whether to display on the front end
     */
    @TableField(value = "is_show")
    private Boolean isShow;
    /**
     * whether to edit on the front end
     */
    @TableField(value = "can_edit")
    private Boolean canEdit;
    /**
     * is emit event
     */
    @TableField(value = "is_emit")
    private Boolean isEmit;
    /**
     * is validate
     */
    @TableField(value = "is_validate")
    private Boolean isValidate;
    /**
     * values map
     */
    @TableField(exist = false)
    private String valuesMap;

    /**
     * values map
     */
    @TableField(exist = false)
    private Integer index;
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
