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

package org.apache.dolphinscheduler.spi.params.base;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The options field in form-create`s json rule
 * Set radio, select, checkbox and other component option options
 */
public class ParamsOptions {

    private String label;

    private Object value;

    /**
     * is can be select
     */
    private boolean disabled;

    public ParamsOptions(@JsonProperty("label") String label, 
                         @JsonProperty("value") Object value,
                         @JsonProperty("disabled") boolean disabled) {
        this.label = label;
        this.value = value;
        this.disabled = disabled;
    }

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    public ParamsOptions setLabel(String label) {
        this.label = label;
        return this;
    }

    @JsonProperty("value")
    public Object getValue() {
        return value;
    }

    public ParamsOptions setValue(Object value) {
        this.value = value;
        return this;
    }

    @JsonProperty("disabled")
    public boolean isDisabled() {
        return disabled;
    }

    public ParamsOptions setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final ParamsOptions other = (ParamsOptions) obj;
        if ((this.label == null) ? (other.label != null) : !this.label.equals(other.label)) {
            return false;
        }
        if ((this.value == null) ? (other.value != null) : !this.value.equals(other.value)) {
            return false;
        }
        return this.disabled == other.disabled;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
