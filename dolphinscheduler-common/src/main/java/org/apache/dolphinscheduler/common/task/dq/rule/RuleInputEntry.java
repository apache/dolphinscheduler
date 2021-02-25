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

package org.apache.dolphinscheduler.common.task.dq.rule;

import org.apache.dolphinscheduler.common.enums.dq.FormType;
import org.apache.dolphinscheduler.common.enums.dq.InputType;
import org.apache.dolphinscheduler.common.enums.dq.OptionSourceType;
import org.apache.dolphinscheduler.common.enums.dq.ValueType;

/**
 * RuleInputEntry
 */
public class RuleInputEntry {

    /**
     * form field name
     */
    private String field;

    /**
     * form type
      */
    private FormType type;

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
    private OptionSourceType optionSourceType = OptionSourceType.DEFAULT;

    /**
     * input entry type: string，array，number .etc
     */
    private ValueType valueType = ValueType.NUMBER;

    /**
     * whether to display on the front end
     */
    private Boolean isShow;

    /**
     * input entry type: default,statistics,comparison
     */
    private InputType inputType = InputType.DEFAULT;

    /**
     * whether to edit on the front end
     */
    private Boolean canEdit;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public FormType getType() {
        return type;
    }

    public void setType(FormType type) {
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

    public OptionSourceType getOptionSourceType() {
        return optionSourceType;
    }

    public void setOptionSourceType(OptionSourceType optionSourceType) {
        this.optionSourceType = optionSourceType;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    public Boolean getShow() {
        return isShow;
    }

    public void setShow(Boolean show) {
        isShow = show;
    }

    public InputType getInputType() {
        return inputType;
    }

    public void setInputType(InputType inputType) {
        this.inputType = inputType;
    }

    public Boolean getCanEdit() {
        return canEdit;
    }

    public void setCanEdit(Boolean canEdit) {
        this.canEdit = canEdit;
    }
}