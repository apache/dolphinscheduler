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

import static java.util.Objects.requireNonNull;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_PLUGIN_PARAM_EMIT;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_PLUGIN_PARAM_FIELD;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_PLUGIN_PARAM_NAME;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_PLUGIN_PARAM_PROPS;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_PLUGIN_PARAM_TITLE;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_PLUGIN_PARAM_TYPE;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_PLUGIN_PARAM_VALIDATE;
import static org.apache.dolphinscheduler.common.constants.Constants.STRING_PLUGIN_PARAM_VALUE;

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
    @JsonProperty(STRING_PLUGIN_PARAM_FIELD)
    protected String name;

    /**
     * param name
     */
    @JsonProperty(STRING_PLUGIN_PARAM_NAME)
    protected String fieldName;

    @JsonProperty(STRING_PLUGIN_PARAM_PROPS)
    protected ParamsProps props;

    @JsonProperty(STRING_PLUGIN_PARAM_TYPE)
    protected String formType;

    /**
     * Name displayed on the page
     */
    @JsonProperty(STRING_PLUGIN_PARAM_TITLE)
    protected String title;

    /**
     * prompt information
     */
    protected String info;

    /**
     * default value or value input by user in the page
     */
    @JsonProperty(STRING_PLUGIN_PARAM_VALUE)
    protected Object value;

    @JsonProperty(STRING_PLUGIN_PARAM_VALIDATE)
    protected List<Validate> validateList;

    @JsonProperty(STRING_PLUGIN_PARAM_EMIT)
    protected List<String> emit;

    /**
     * whether to hide, the default value is false
     */
    protected Boolean hidden;

    /**
     * whether to display, the default value is true
     */
    protected Boolean display;

    protected PluginParams(Builder builder) {

        requireNonNull(builder, "builder is null");
        requireNonNull(builder.name, "name is null");
        requireNonNull(builder.formType, "formType is null");
        requireNonNull(builder.title, "title is null");

        this.name = builder.name;
        this.formType = builder.formType.getFormType();
        this.title = builder.title;

        if (null == builder.props) {
            builder.props = new ParamsProps();
        }
        this.fieldName = builder.title;
        this.props = builder.props;
        this.value = builder.value;
        this.validateList = builder.validateList;
        this.info = builder.info;
        this.display = builder.display;
        this.hidden = builder.hidden;
        this.emit = builder.emit;
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "set")
    public static class Builder {

        // Must have
        protected String name;

        protected FormType formType;

        protected String title;

        protected String fieldName;

        // option params
        protected ParamsProps props;

        protected Object value;

        protected String info;

        protected List<Validate> validateList;

        protected List<String> emit;

        protected Boolean hidden;

        protected Boolean display;

        public Builder(String name,
                       FormType formType,
                       String title) {
            requireNonNull(name, "name is null");
            requireNonNull(formType, "formType is null");
            requireNonNull(title, "title is null");
            this.name = name;
            this.formType = formType;
            this.title = title;
            this.fieldName = title;
        }

        // for json deserialize to POJO
        @JsonCreator
        public Builder(@JsonProperty("field") String name,
                       @JsonProperty("type") FormType formType,
                       @JsonProperty("title") String title,
                       @JsonProperty("props") ParamsProps props,
                       @JsonProperty("value") Object value,
                       @JsonProperty("name") String fieldName,
                       @JsonProperty("validate") List<Validate> validateList,
                       @JsonProperty("emit") List<String> emit,
                       @JsonProperty("info") String info,
                       @JsonProperty("hidden") Boolean hidden,
                       @JsonProperty("display") Boolean display) {
            requireNonNull(name, "name is null");
            requireNonNull(formType, "formType is null");
            requireNonNull(title, "title is null");
            this.name = name;
            this.formType = formType;
            this.title = title;
            this.props = props;
            this.value = value;
            this.validateList = validateList;
            this.fieldName = fieldName;
            this.emit = emit;
            this.info = info;
            this.hidden = hidden;
            this.display = display;
        }

        public PluginParams build() {
            return new PluginParams(this);
        }
    }

    public String getName() {
        return name;
    }

    public ParamsProps getProps() {
        return props;
    }

    public String getFormType() {
        return formType;
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

    public void setValue(Object value) {
        this.value = value;
    }

    public List<String> getEmit() {
        return emit;
    }

}
