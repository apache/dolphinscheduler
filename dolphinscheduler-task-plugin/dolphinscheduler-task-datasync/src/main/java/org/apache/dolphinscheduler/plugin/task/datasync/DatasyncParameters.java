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

package org.apache.dolphinscheduler.plugin.task.datasync;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import lombok.Data;

import java.util.List;

import org.apache.dolphinscheduler.spi.utils.StringUtils;
import software.amazon.awssdk.services.datasync.model.FilterRule;
import software.amazon.awssdk.services.datasync.model.Options;
import software.amazon.awssdk.services.datasync.model.TagListEntry;
import software.amazon.awssdk.services.datasync.model.TaskSchedule;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class DatasyncParameters extends AbstractParameters {

    private String destinationLocationArn;
    private String sourceLocationArn;
    private String name;
    private String cloudWatchLogGroupArn;

    private boolean isJsonFormat;
    private String json;
    @JsonIgnore
    private Options options;
    @JsonIgnore
    private TaskSchedule schedule;
    @JsonIgnore
    private List<FilterRule> excludes;
    @JsonIgnore
    private List<FilterRule> includes;
    @JsonIgnore
    private List<TagListEntry> tags;

    @Override
    public boolean checkParameters() {
        if (isJsonFormat) {
            return StringUtils.isNotEmpty(json);
        } else {
            return StringUtils.isNotEmpty(destinationLocationArn)&&StringUtils.isNotEmpty(sourceLocationArn)&&StringUtils.isNotEmpty(name);
        }
    }

}