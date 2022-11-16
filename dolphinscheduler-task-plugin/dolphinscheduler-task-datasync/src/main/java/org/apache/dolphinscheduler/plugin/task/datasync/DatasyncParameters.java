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

import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonProperty;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class DatasyncParameters extends AbstractParameters {

    private String destinationLocationArn;
    private String sourceLocationArn;
    private String name;
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
            return StringUtils.isNotEmpty(destinationLocationArn) && StringUtils.isNotEmpty(sourceLocationArn)
                    && StringUtils.isNotEmpty(name);
        }
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @ToString
    public class Options {

        private String verifyMode;
        private String overwriteMode;
        private String atime;
        private String mtime;
        private String uid;
        private String gid;
        private String preserveDeletedFiles;
        private String preserveDevices;
        private String posixPermissions;
        private Long bytesPerSecond;
        private String taskQueueing;
        private String logLevel;
        private String transferMode;
        private String securityDescriptorCopyFlags;
        private String objectTags;
    }
    @Setter
    @Getter
    @NoArgsConstructor
    @ToString
    public static class TaskSchedule {

        private String scheduleExpression;
    }
    @Setter
    @Getter
    @NoArgsConstructor
    @ToString
    public static class FilterRule {

        private String filterType;
        private String value;
    }
    @Setter
    @Getter
    @NoArgsConstructor
    @ToString
    public static class TagListEntry {

        private String key;
        private String value;
    }
}
