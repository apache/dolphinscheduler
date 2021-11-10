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

package org.apache.dolphinscheduler.spi.params.select;

import static org.apache.dolphinscheduler.spi.params.base.FormType.SELECT;

import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * front-end select component
 */
@JsonDeserialize(builder = SelectParam.Builder.class)
public class SelectParam extends PluginParams {

    private List<ParamsOptions> options;

    private SelectParamProps props;

    private SelectParam(Builder builder) {
        super(builder);
    }

    public static Builder newBuilder(String name, String title) {
        return new Builder(name, title);
    }

    public static class Builder extends PluginParams.Builder {

        public Builder(String name, String title) {
            super(name, SELECT, title);
        }

        private List<ParamsOptions> options;

        private SelectParamProps props;

        @JsonCreator
        public Builder(@JsonProperty("field") String name,
                       @JsonProperty("title") String title,
                       @JsonProperty("props") SelectParamProps props,
                       @JsonProperty("value") Object value,
                       @JsonProperty("name") String fieldName,
                       @JsonProperty("validate") List<Validate> validateList,
                       @JsonProperty("info") String info,
                       @JsonProperty("hidden") Boolean hidden,
                       @JsonProperty("display") Boolean display
        ) {
            super(name, SELECT, title, props, value, fieldName, validateList, info, hidden, display);
            this.props = props;
        }

        public Builder setOptions(List<ParamsOptions> options) {
            this.options = options;
            return this;
        }

        public Builder addOptions(ParamsOptions paramsOptions) {
            if (this.options == null) {
                this.options = new LinkedList<>();
            }

            this.options.add(paramsOptions);
            return this;
        }

        public Builder setProps(SelectParamProps props) {
            this.props = props;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setInfo(String info) {
            this.info = info;
            return this;
        }

        public Builder setValue(Object value) {
            this.value = value;
            return this;
        }

        public Builder setValidateList(List<Validate> validateList) {
            this.validateList = validateList;
            return this;
        }

        public Builder addValidate(Validate validate) {
            if (this.validateList == null) {
                this.validateList = new LinkedList<>();
            }
            this.validateList.add(validate);
            return this;
        }

        public Builder setHidden(Boolean hidden) {
            this.hidden = hidden;
            return this;
        }

        public Builder setDisplay(Boolean display) {
            this.display = display;
            return this;
        }

        @Override
        public SelectParam build() {
            return new SelectParam(this);
        }
    }

    public List<ParamsOptions> getOptions() {
        return options;
    }

    @Override
    public SelectParamProps getProps() {
        return props;
    }
}
