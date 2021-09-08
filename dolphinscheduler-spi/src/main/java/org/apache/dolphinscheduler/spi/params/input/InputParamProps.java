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

package org.apache.dolphinscheduler.spi.params.input;

import org.apache.dolphinscheduler.spi.params.base.ParamsProps;
import org.apache.dolphinscheduler.spi.params.base.ResizeType;

/**
 * front-end input component props attributes
 */
public class InputParamProps extends ParamsProps {

    /**
     * input type
     */
    private String type;

    /**
     * maximum input length
     */
    private Integer maxlength;

    /**
     * minimum input length
     */
    private Integer minlength;

    /**
     * whether it can be cleared, the default value is false
     */
    private Boolean clearable;

    /**
     * input box head icon
     */
    private String prefixIcon;

    /**
     * input box end icon
     */
    private String suffixIcon;

    /**
     * number of lines in the input box, only valid for type="textarea"
     */
    private Integer rows;

    /**
     * adaptive content height, only valid for type="textarea", objects can be passed in, such as {minRows: 2, maxRows: 6}
     */
    private Object autosize;

    /**
     * autocomplete attribute:on, off
     */
    private String autocomplete;

    /**
     * name attribute
     */
    private String name;

    /**
     * whether it is read-only, the default value is false
     */
    private Boolean readonly;

    /**
     * set maximum
     */
    private Integer max;

    /**
     * set minimum
     */
    private Integer min;

    /**
     * set the legal number interval of the input field
     */
    private Integer step;

    /**
     * control whether it can be zoomed by the user, the value is none, both, horizontal, vertical
     */
    private ResizeType resize;

    /**
     * get focus automatically, the default value is false
     */
    private Boolean autofocus;

    private String form;

    /**
     * the label text associated with the input box
     */
    private String label;

    /**
     * tabindex of the input box
     */
    private String tabindex;

    /**
     * whether to trigger the verification of the form during input, the default value is true
     */
    private Boolean validateEvent;

    /**
     * whether to display the switch password icon
     */
    private Boolean showPassword;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getMaxlength() {
        return maxlength;
    }

    public void setMaxlength(Integer maxlength) {
        this.maxlength = maxlength;
    }

    public Integer getMinlength() {
        return minlength;
    }

    public void setMinlength(Integer minlength) {
        this.minlength = minlength;
    }

    public Boolean getClearable() {
        return clearable;
    }

    public void setClearable(Boolean clearable) {
        this.clearable = clearable;
    }

    public String getPrefixIcon() {
        return prefixIcon;
    }

    public void setPrefixIcon(String prefixIcon) {
        this.prefixIcon = prefixIcon;
    }

    public String getSuffixIcon() {
        return suffixIcon;
    }

    public void setSuffixIcon(String suffixIcon) {
        this.suffixIcon = suffixIcon;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public Object getAutosize() {
        return autosize;
    }

    public void setAutosize(Object autosize) {
        this.autosize = autosize;
    }

    public String getAutocomplete() {
        return autocomplete;
    }

    public void setAutocomplete(String autocomplete) {
        this.autocomplete = autocomplete;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getReadonly() {
        return readonly;
    }

    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public ResizeType getResize() {
        return resize;
    }

    public void setResize(ResizeType resize) {
        this.resize = resize;
    }

    public Boolean getAutofocus() {
        return autofocus;
    }

    public void setAutofocus(Boolean autofocus) {
        this.autofocus = autofocus;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTabindex() {
        return tabindex;
    }

    public void setTabindex(String tabindex) {
        this.tabindex = tabindex;
    }

    public Boolean getValidateEvent() {
        return validateEvent;
    }

    public void setValidateEvent(Boolean validateEvent) {
        this.validateEvent = validateEvent;
    }

    public Boolean getShowPassword() {
        return showPassword;
    }

    public void setShowPassword(Boolean showPassword) {
        this.showPassword = showPassword;
    }
}
