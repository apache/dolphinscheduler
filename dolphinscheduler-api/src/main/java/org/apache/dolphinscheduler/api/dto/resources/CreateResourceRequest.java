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

import org.apache.dolphinscheduler.spi.enums.ResourceType;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * create resource request
 */
@Data
public class CreateResourceRequest {

    @ApiModelProperty(name = "type", value = "RESOURCE_TYPE", notes = "FILE , UDF", required = true, example = "FILE")
    private ResourceType type;

    @ApiModelProperty(name = "fileName", value = "RESOURCE_NAME", required = true)
    private String fileName;

    @ApiModelProperty(name = "suffix", value = "SUFFIX", required = true)
    private String suffix;

    @ApiModelProperty(name = "description", value = "RESOURCE_DESC")
    private String description;

    @ApiModelProperty(name = "pid", value = "RESOURCE_PID", required = true, example = "10")
    private int pid;

    @ApiModelProperty(name = "content", value = "CONTENT", required = true)
    private String content;

    @ApiModelProperty(name = "currentDir", value = "RESOURCE_CURRENT_DIR", required = true, example = "firstDir")
    private String currentDir;
}
