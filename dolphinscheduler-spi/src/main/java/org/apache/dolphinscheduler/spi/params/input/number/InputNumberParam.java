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

package org.apache.dolphinscheduler.spi.params.input.number;

import static org.apache.dolphinscheduler.spi.params.base.FormType.INPUTNUMBER;

import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;

import java.util.ArrayList;
import java.util.List;

/**
 * front-end input number component
 */
public class InputNumberParam extends PluginParams {

    private final InputNumberParamProps props;

    private InputNumberParam(Builder builder) {
        super(builder);
        this.props = builder.props;
    }

    public static Builder newBuilder(String name, String title) {
        return new Builder(name, title);
    }

    public static class Builder extends PluginParams.Builder {

        public Builder(String name, String title) {
            super(name, INPUTNUMBER, title);
        }

        private InputNumberParamProps props;

        public Builder setProps(InputNumberParamProps props) {
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
                this.validateList = new ArrayList<>();
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
        public InputNumberParam build() {
            return new InputNumberParam(this);
        }
    }

    @Override
    public InputNumberParamProps getProps() {
        return props;
    }
}
