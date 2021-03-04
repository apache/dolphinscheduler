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

package org.apache.dolphinscheduler.common.enums.dq;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * frontend form type
 */
public enum FormType {
    /**
     * 0-input
     * 1-radio
     * 2-select
     * 3-checkbox
     * 4-cascader
     * 5-textarea
     */
    INPUT(0,"input"),
    RADIO(1,"radio"),
    SELECT(2,"select"),
    SWITCH(3,"checkbox"),
    CASCADER(4,"cascader"),
    TEXTAREA(5,"textarea");

    FormType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @EnumValue
    private final int code;

    private final String description;

    public int getCode() {
        return code;
    }

    @JsonValue
    public String getDescription() {
        return description;
    }

    public static FormType of(int value) {
        for (FormType e: FormType.values()) {
            if (e.ordinal() == value) {
                return e;
            }
        }
        //For values out of enum scope
        return null;
    }
}