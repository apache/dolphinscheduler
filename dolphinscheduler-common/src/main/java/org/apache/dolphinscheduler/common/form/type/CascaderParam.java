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

package org.apache.dolphinscheduler.common.form.type;

import org.apache.dolphinscheduler.common.enums.dq.FormType;
import org.apache.dolphinscheduler.common.form.CascaderParamsOptions;
import org.apache.dolphinscheduler.common.form.PluginParams;
import org.apache.dolphinscheduler.common.form.Validate;
import org.apache.dolphinscheduler.common.form.props.CascaderParamsProps;

import java.util.ArrayList;
import java.util.List;

/**
 * CascaderParam
 */
public class CascaderParam extends PluginParams {

    private CascaderParam(Builder builder) {
        super(builder);
    }

    public static Builder newBuilder(String field, String title) {
        return new Builder(field, title);
    }

    public static class Builder extends PluginParams.Builder {

        public Builder(String field, String title) {
            super(field, FormType.CASCADER, title);
        }

        public Builder addValidate(Validate validate) {
            if (this.validateList == null) {
                this.validateList = new ArrayList<>();
            }
            this.validateList.add(validate);
            return this;
        }

        public Builder setField(String field) {
            this.field = field;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
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

        public Builder setProps(CascaderParamsProps props) {
            this.props = props;
            return this;
        }

        public Builder setParamsOptionsList(List<CascaderParamsOptions> paramsOptionsList) {
            if (this.props == null) {
                this.setProps(new CascaderParamsProps());
            }

            ((CascaderParamsProps)this.props).setOptions(paramsOptionsList);
            return this;
        }

        public Builder addParamsOptions(CascaderParamsOptions paramsOptions) {
            if (this.props == null) {
                this.setProps(new CascaderParamsProps());
            }

            if (((CascaderParamsProps)this.props).getOptions() == null) {
                ((CascaderParamsProps)this.props).setOptions(new ArrayList<CascaderParamsOptions>());
            }

            ((CascaderParamsProps)this.props).setOption(paramsOptions);
            return this;
        }

        public Builder setSize(String size) {
            if (this.props == null) {
                this.setProps(new CascaderParamsProps());
            }

            ((CascaderParamsProps)this.props).setSize(size);
            return this;
        }

        @Override
        public CascaderParam build() {
            return new CascaderParam(this);
        }
    }

}
