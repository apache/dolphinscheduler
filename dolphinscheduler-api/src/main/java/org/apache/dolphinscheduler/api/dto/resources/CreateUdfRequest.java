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

package org.apache.dolphinscheduler.api.dto.resources;

import org.apache.dolphinscheduler.common.enums.UdfType;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * create udf request
 */
@Data
public class CreateUdfRequest {

    @ApiModelProperty(name = "type", value = "UDF_TYPE", required = true, dataType = "UdfType", example = "HIVE,SPARK")
    private UdfType type;

    @ApiModelProperty(name = "funcName", value = "FUNC_NAME", required = true, dataType = "String", example = "evaluate")
    private String funcName;

    @ApiModelProperty(name = "className", value = "CLASS_NAME", required = true, dataType = "String", example = "GetLength")
    private String className;

    @ApiModelProperty(name = "argTypes", value = "ARG_TYPES", dataType = "String", example = "String")
    private String argTypes;

    @ApiModelProperty(name = "database", value = "DATABASE_NAME", dataType = "String", example = "db")
    private String database;

    @ApiModelProperty(name = "description", value = "UDF_DESC", dataType = "String", example = "description")
    private String description;
}
