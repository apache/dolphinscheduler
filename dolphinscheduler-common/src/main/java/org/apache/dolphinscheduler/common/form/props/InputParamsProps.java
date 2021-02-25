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

package org.apache.dolphinscheduler.common.form.props;

import org.apache.dolphinscheduler.common.enums.dq.PropsType;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * the props field in form-create`s json rule
 */
public class InputParamsProps {

    private PropsType propsType;

    private String placeholder;

    private int rows;

    private boolean disabled;

    private String size;

    @JsonProperty("size")
    public String getSize() {
        return size;
    }

    public InputParamsProps setSize(String size) {
        this.size = size;
        return this;
    }

    @JsonProperty("type")
    public PropsType getPropsType() {
        return propsType;
    }

    public InputParamsProps setPropsType(PropsType propsType) {
        this.propsType = propsType;
        return this;
    }

    @JsonProperty("placeholder")
    public String getPlaceholder() {
        return placeholder;
    }

    public InputParamsProps setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    @JsonProperty("rows")
    public int getRows() {
        return rows;
    }

    public InputParamsProps setRows(int rows) {
        this.rows = rows;
        return this;
    }

    @JsonProperty("disabled")
    public boolean getDisabled() {
        return disabled;
    }

    public InputParamsProps setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }
}
