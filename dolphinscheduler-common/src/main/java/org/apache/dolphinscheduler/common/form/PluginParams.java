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

package org.apache.dolphinscheduler.common.form;

import static java.util.Objects.requireNonNull;

import org.apache.dolphinscheduler.common.enums.dq.FormType;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * plugin params
 */
@JsonDeserialize(builder = PluginParams.Builder.class)
public class PluginParams {

    /**
     * param name
     */
    @JsonProperty("field")
    protected String field;

    @JsonProperty("props")
    protected Object props;

    @JsonProperty("type")
    protected String type;

    /**
     * Name displayed on the page
     */
    @JsonProperty("title")
    protected String title;

    /**
     * default value or value input by user in the page
     */
    @JsonProperty("value")
    protected Object value;

    @JsonProperty("validate")
    protected List<Validate> validateList;

    protected PluginParams(Builder builder) {

        requireNonNull(builder, "builder is null");
        requireNonNull(builder.field, "field is null");
        requireNonNull(builder.type, "type is null");
        requireNonNull(builder.title, "title is null");

        this.field = builder.field;
        this.type = builder.type.getDescription();
        this.title = builder.title;
        this.props = builder.props;
        this.value = builder.value;
        this.validateList = builder.validateList;

    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "set")
    public static class Builder {
        //Must have
        protected String field;

        protected FormType type;

        protected String title;

        protected Object props;

        protected Object value;

        protected List<Validate> validateList;

        public Builder(String field,
                       FormType type,
                       String title) {
            requireNonNull(field, "field is null");
            requireNonNull(type, "type is null");
            requireNonNull(title, "title is null");
            this.field = field;
            this.type = type;
            this.title = title;
        }

        //for json deserialize to POJO
        @JsonCreator
        public Builder(@JsonProperty("field") String field,
                       @JsonProperty("type") FormType type,
                       @JsonProperty("title") String title,
                       @JsonProperty("props") Object props,
                       @JsonProperty("value") Object value,
                       @JsonProperty("validate") List<Validate> validateList
        ) {
            requireNonNull(field, "field is null");
            requireNonNull(type, "type is null");
            requireNonNull(title, "title is null");
            this.field = field;
            this.type = type;
            this.title = title;
            this.props = props;
            this.value = value;
            this.validateList = validateList;
        }

        public PluginParams build() {
            return new PluginParams(this);
        }

    }

    public String getField() {
        return field;
    }

    public Object getProps() {
        return props;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public Object getValue() {
        return value;
    }

    public List<Validate> getValidateList() {
        return validateList;
    }
}


