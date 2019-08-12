/*
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.escheduler.plugin.sdk.creation;

import cn.escheduler.plugin.api.ErrorCode;
import cn.escheduler.plugin.api.GenerateResourceBundle;

@GenerateResourceBundle
public enum CreationError implements ErrorCode {

    CREATION_000("Failed to instantiate {} '{}' [ERROR]: {}"),
    CREATION_001("Failed to instantiate config bean '{}' [ERROR]: {}"),
    CREATION_002("Configuration definition missing '{}', there is a library/stage mismatch [ERROR]"),
    CREATION_003("Failed to access config bean [ERROR]: {}"),

    CREATION_004("Could not set default value to configuration [ERROR]: {}"),

    CREATION_005("Could not resolve implicit EL expression '{}': {}"),

    CREATION_006("Stage definition not found Library '{}' Stage '{}' Version '{}'"),

    CREATION_007("Stage definition Library '{}' Stage '{}' Version '{}' is for error stages only"),
    CREATION_008("Stage definition Library '{}' Stage '{}' Version '{}' is not for error stages"),

    CREATION_009("Pipeline error handling is not configured"),

    CREATION_010("Configuration value '{}' is not a valid '{}' enum value: {}"),
    CREATION_011("Configuration value '{}' is not string, it is a '{}'"),
    CREATION_012("Configuration value '{}' is not character, it is a '{}'"),
    CREATION_013("Configuration value '{}' is not boolean, it is a '{}'"),
    CREATION_014("Configuration value '{}' is not number, it is a '{}'"),
    CREATION_015("Configuration value '{}' cannot be converted to '{}': {}"),

    CREATION_016("Could not obtain Java default value: {}"),
    CREATION_017("Stage definition Library '{}' Stage '{}' Version '{}' can not be used for pipeline lifecycle event handling"),
    CREATION_018("Configuration value '{}' should be a String or a CredentialValue, it is a '{}'"),

    CREATION_020("Configuration value is not a LIST"),
    CREATION_021("LIST configuration has a NULL value"),

    CREATION_030("Configuration value is not a MAP"),
    CREATION_031("MAP configuration value has invalid values type '{}'"),
    CREATION_032("MAP configuration has a NULL key"),
    CREATION_033("MAP configuration has a NULL value"),

    CREATION_040("ComplexField configuration is not a LIST, it is a '{}'"),
    CREATION_041("Failed to instantiate ComplexField bean '{}' [ERROR]: {}"),
    CREATION_042("ComplexField configuration value is invalid: {}"),
    CREATION_043("ComplexField could not load class '{}' in class loader {}"),

    CREATION_050("Configuration value cannot be NULL"),
    CREATION_051("Configuration type '{}' is invalid [ERROR]"),

    CREATION_060("Could not set configuration value '{}' [ERROR]: {}"),

    CREATION_070("Invalid execution mode '{}'"),
    CREATION_071("Execution mode not set"),

    CREATION_080("Configuration value is required for Webhook URL"),

    // Detached stage support
    CREATION_0900("Can't parse stage definition: {}"),
    ;

    private final String msg;

    CreationError(String msg) {
        this.msg = msg;
    }

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getMessage() {
        return msg;
    }

}
