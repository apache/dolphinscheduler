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

public class Validate {

    private boolean required = false;

    private String message;

    private String type = DataType.STRING.getDataType();

    private String trigger = TriggerType.BLUR.getTriggerType();

    private Double min;

    private Double max;

    public static Validate buildValidate() {
        return new Validate();
    }

    @JsonProperty("required")
    public boolean isRequired() {
        return required;
    }

    public Validate setRequired(boolean required) {
        this.required = required;
        return this;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public Validate setMessage(String message) {
        this.message = message;
        return this;
    }

    @JsonProperty("trigger")
    public String getTrigger() {
        return trigger;
    }

    public Validate setTrigger(String trigger) {
        this.trigger = trigger;
        return this;
    }

    @JsonProperty("min")
    public Double getMin() {
        return min;
    }

    public Validate setMin(Double min) {
        this.min = min;
        return this;
    }

    @JsonProperty("max")
    public Double getMax() {
        return max;
    }

    public Validate setMax(Double max) {
        this.max = max;
        return this;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public Validate setType(String type) {
        this.type = type;
        return this;
    }
}
