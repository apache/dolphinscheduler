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

import org.apache.dolphinscheduler.spi.task.dq.enums.InputType;
import org.apache.dolphinscheduler.spi.task.dq.enums.OptionSourceType;
import org.apache.dolphinscheduler.spi.task.dq.enums.ValueType;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * RuleInputEntry
 */
@TableName("t_ds_dq_rule_input_entry")
public class DqRuleInputEntry implements Serializable {
    /**
     * primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private int id;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /**
     * update_time
     */
    @TableField(value = "update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public int getOptionSourceType() {
        return optionSourceType;
    }

    public void setOptionSourceType(int optionSourceType) {
        this.optionSourceType = optionSourceType;
    }

    public int getValueType() {
        return valueType;
    }

    public void setValueType(int valueType) {
        this.valueType = valueType;
    }

    public int getInputType() {
        return inputType;
    }

    public void setInputType(int inputType) {
        this.inputType = inputType;
    }

    public Boolean getShow() {
        return isShow;
    }

    public void setShow(Boolean show) {
        isShow = show;
    }

    public Boolean getCanEdit() {
        return canEdit;
    }

    public void setCanEdit(Boolean canEdit) {
        this.canEdit = canEdit;
    }

    public Boolean getEmit() {
        return isEmit;
    }

    public void setEmit(Boolean emit) {
        isEmit = emit;
    }

    public Boolean getValidate() {
        return isValidate;
    }

    public void setValidate(Boolean validate) {
        isValidate = validate;
    }

    public String getValuesMap() {
        return valuesMap;
    }

    public void setValuesMap(String valuesMap) {
        this.valuesMap = valuesMap;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
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
        return "DqRuleInputEntry{"
                + "id=" + id
                + ", field='" + field + '\''
                + ", type=" + type
                + ", title='" + title + '\''
                + ", value='" + value + '\''
                + ", options='" + options + '\''
                + ", placeholder='" + placeholder + '\''
                + ", optionSourceType=" + optionSourceType
                + ", valueType=" + valueType
                + ", inputType=" + inputType
                + ", isShow=" + isShow
                + ", canEdit=" + canEdit
                + ", isEmit=" + isEmit
                + ", isValidate=" + isValidate
                + ", valuesMap='" + valuesMap + '\''
                + ", index=" + index
                + ", createTime=" + createTime
                + ", updateTime=" + updateTime
                + '}';
    }
}