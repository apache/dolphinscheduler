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

import com.amazonaws.services.datasync.model.FilterRule;
import com.amazonaws.services.datasync.model.Options;
import com.amazonaws.services.datasync.model.TagListEntry;
import com.amazonaws.services.datasync.model.TaskSchedule;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class DatasyncParameters extends AbstractParameters {

    @JsonProperty("DestinationLocationArn")
    private String destinationLocationArn;
    @JsonProperty("SourceLocationArn")
    private String sourceLocationArn;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("CloudWatchLogGroupArn")
    private String cloudWatchLogGroupArn;

    private boolean jsonFormat;
    private String json;
    @JsonProperty("Options")
    private Options options;
    @JsonProperty("Schedule")
    private TaskSchedule schedule;
    @JsonProperty("Excludes")
    private List<FilterRule> excludes;
    @JsonProperty("Includes")
    private List<FilterRule> includes;
    @JsonProperty("Tags")
    private List<TagListEntry> tags;

    @Override
    public boolean checkParameters() {
        if (jsonFormat) {
            return StringUtils.isNotEmpty(json);
        } else {
            return StringUtils.isNotEmpty(destinationLocationArn) && StringUtils.isNotEmpty(sourceLocationArn) && StringUtils.isNotEmpty(name);
        }
    }

}