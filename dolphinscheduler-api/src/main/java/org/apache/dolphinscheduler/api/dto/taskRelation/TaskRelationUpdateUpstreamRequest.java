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

package org.apache.dolphinscheduler.api.dto.taskRelation;

import static org.apache.dolphinscheduler.common.constants.Constants.COMMA;

import org.apache.dolphinscheduler.api.dto.PageQueryDto;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * task relation update request
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TaskRelationUpdateUpstreamRequest extends PageQueryDto {

    @Schema(example = "1234587654321", description = "workflow code ")
    private long workflowCode;

    @Schema(example = "12345678,87654321", required = true, description = "upstream you want to update separated by comma")
    private String upstreams;

    public List<Long> getUpstreams() {
        return Stream.of(this.upstreams.split(COMMA))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
}
