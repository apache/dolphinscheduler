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
package cn.escheduler.plugin.api.base;

import cn.escheduler.plugin.api.ErrorCode;
import cn.escheduler.plugin.api.GenerateResourceBundle;

/**
 * Error codes produced by the Data Collector where there is a stage configuration issue.
 */
// we are using the annotation for reference purposes only.
// the annotation processor does not work on this maven project
// we have a hardcoded 'datacollector-resource-bundles.json' file in resources
@GenerateResourceBundle
public enum Errors implements ErrorCode {
    API_00("Stage '{}' requires exactly {} output streams. There are '{}'."),
    API_01("Cannot convert {} field '{}' to Boolean"),
    API_02("Cannot convert {} field '{}' to Byte[]"),
    API_03("Cannot convert Byte[] to {}"),
    API_04("Cannot convert {} field '{}' to Byte"),
    API_05("Cannot convert {} field '{}' to Char"),
    API_06("Cannot parse '{}' to a Date. Use the following ISO 8601 UTC date format: yyyy-MM-dd'T'HH:mm'Z'."),
    API_07("Cannot convert {} field '{}' to Date"),
    API_08("Cannot convert {} field '{}' to Decimal"),
    API_09("Cannot convert {} field '{}' to Double"),
    API_10("Cannot convert {} field '{}' to Float"),
    API_11("Cannot convert {} field '{}' to Integer"),
    API_12("Cannot convert {} field '{}' to List"),
    API_13("Cannot convert List to {}"),
    API_14("Cannot convert {} field '{}' to Long"),
    API_15("Cannot convert {} field '{}' to Map"),
    API_16("Cannot convert Map to {}"),
    API_17("Cannot convert {} field '{}' to Short"),
    API_18("Cannot convert Map, List, or Byte[] to String"),

    API_19("Error while initializing stage: {}"),
    API_20("The stage implementation overridden the init() but didn't call super.init()"),

    API_21("Cannot convert {} field '{}' to ListMap"),
    API_22("Cannot convert ListMap to {}"),
    API_23("Cannot convert {} to File Ref Object"),
    API_24("Cannot convert FileRef Object to {}"),
    API_25("Cannot convert {} field '{}' to ZonedDateTime"),
    API_26("Cannot parse '{}' to a ZonedDateTime. " +
            "Use ISO 8601 format with offset and zone, such as '2011-12-03T10:15:30+01:00[Europe/Paris]'"),

    ;

    private final String msg;

    Errors(String msg) {
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
