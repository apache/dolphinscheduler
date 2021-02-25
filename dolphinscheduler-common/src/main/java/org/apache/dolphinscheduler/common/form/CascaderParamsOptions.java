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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The cascader options field in form-create`s json rule
 */
public class CascaderParamsOptions extends ParamsOptions {

    private List<CascaderParamsOptions> children;

    public CascaderParamsOptions(String label, Object value, List<CascaderParamsOptions> children, boolean disabled) {
        super(label,value,disabled);
        this.children = children;
    }

    public CascaderParamsOptions(String label, Object value,boolean disabled) {
        super(label,value,disabled);
    }

    @JsonProperty("children")
    public List<CascaderParamsOptions> getChildren() {
        return children;
    }

    public void setChildren(List<CascaderParamsOptions> children) {
        this.children = children;
    }
}
