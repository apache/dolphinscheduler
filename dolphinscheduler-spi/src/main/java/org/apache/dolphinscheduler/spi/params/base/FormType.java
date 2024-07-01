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

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Functions;

public enum FormType {

    INPUT("input"),
    INPUTNUMBER("input-number"),
    RADIO("radio"),
    SELECT("select"),
    SWITCH("switch"),
    CHECKBOX("checkbox"),
    TIMEPICKER("timePicker"),
    DATEPICKER("datePicker"),
    SLIDER("slider"),
    RATE("rate"),
    COLORPICKER("colorPicker"),
    CASCADER("cascader"),
    UPLOAD("upload"),
    ELTRANSFER("el-transfer"),
    TREE("tree"),
    TEXTAREA("textarea"),
    GROUP("group");

    private String formType;

    FormType(String formType) {
        this.formType = formType;
    }

    @JsonValue
    public String getFormType() {
        return this.formType;
    }

    private static final Map<String, FormType> FORM_TYPE_MAP =
            Arrays.stream(FormType.values()).collect(toMap(FormType::getFormType, Functions.identity()));

    public static FormType of(String type) {
        if (FORM_TYPE_MAP.containsKey(type)) {
            return FORM_TYPE_MAP.get(type);
        }
        return null;
    }
}
