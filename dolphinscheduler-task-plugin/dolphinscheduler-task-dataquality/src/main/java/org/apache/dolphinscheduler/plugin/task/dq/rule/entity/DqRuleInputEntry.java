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

package org.apache.dolphinscheduler.plugin.task.dq.rule.entity;

import org.apache.dolphinscheduler.plugin.task.api.enums.dp.InputType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.OptionSourceType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.ValueType;

import java.io.Serializable;
import java.util.Date;

/**
 * RuleInputEntry
 */
public class DqRuleInputEntry implements Serializable {
    /**
     * primary key
     */
    private int id;
    /**
     * form field name
     */
    private String field;
    /**
     * form type
      */
    private String type;
    /**
     * form title
     */
    private String title;
    /**
     * default value，can be null
     */
    private String value;
    /**
     * default options，can be null
     *  [{label:"",value:""}]
     */
    private String options;
    /**
     * ${field}
     */
    private String placeholder;
    /**
     * the source type of options，use default options or other
     */
    private int optionSourceType = OptionSourceType.DEFAULT.getCode();
    /**
     * input entry type: string，array，number .etc
     */
    private int valueType = ValueType.NUMBER.getCode();
    /**
     * input entry type: default,statistics,comparison
     */
    private int inputType = InputType.DEFAULT.getCode();
    /**
     * whether to display on the front end
     */
    private Boolean isShow;
    /**
     * whether to edit on the front end
     */
    private Boolean canEdit;
    /**
     * is emit event
     */
    private Boolean isEmit;
    /**
     * is validate
     */
    private Boolean isValidate;
    /**
     * values map
     */
    private String valuesMap;
    /**
     * values map
     */
    private Integer index;
    /**
     * create_time
     */
    private Date createTime;
    /**
     * update_time
     */
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