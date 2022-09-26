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

import static org.apache.dolphinscheduler.common.Constants.COMMA;

import java.util.stream.Stream;

import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

/**
 * task relation want to delete request
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
public class TaskRelationDeleteRequest {

    @ApiModelProperty(example = "12345678,87654321", required = true, notes = "relation pair want to delete relation, separated by comma")
    private String codePair;

    private long[] parseCodePair(String codePair) {
        return Stream.of(codePair.split(COMMA))
                .map(String::trim)
                .mapToLong(Long::parseLong)
                .toArray();
    }

    public long getUpstreamCode() {
        return this.parseCodePair(codePair)[0];
    }

    public long getDownstreamCode() {
        return this.parseCodePair(codePair)[1];
    }

    public TaskRelationDeleteRequest(String codePair) {
        this.codePair = codePair;
    }
}
