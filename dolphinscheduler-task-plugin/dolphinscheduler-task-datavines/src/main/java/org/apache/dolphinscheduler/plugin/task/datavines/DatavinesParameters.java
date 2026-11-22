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

package org.apache.dolphinscheduler.plugin.task.datavines;

import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@EqualsAndHashCode(callSuper = true)
public class DatavinesParameters extends AbstractParameters {

    @JsonProperty("datavinesAddress")
    private String address;

    @JsonProperty("datavinesJobId")
    private Long jobId;

    @JsonProperty("datavinesToken")
    private String token;

    @JsonProperty("datavinesInterval")
    private Integer interval = DatavinesConstants.DEFAULT_INTERVAL_SECONDS;

    @JsonProperty("datavinesTimeout")
    private Integer timeout = DatavinesConstants.DEFAULT_TIMEOUT_SECONDS;

    @JsonProperty("datavinesRetryTimes")
    private Integer retryTimes = DatavinesConstants.DEFAULT_RETRY_TIMES;

    @Override
    public boolean checkParameters() {
        return StringUtils.isNotBlank(address)
                && (StringUtils.startsWith(address, "http://") || StringUtils.startsWith(address, "https://"))
                && jobId != null
                && jobId > 0
                && StringUtils.isNotBlank(token)
                && interval != null
                && interval >= 1
                && timeout != null
                && timeout > 0
                && retryTimes != null
                && retryTimes >= 0;
    }

    public String getNormalizedAddress() {
        if (StringUtils.isBlank(address)) {
            return address;
        }
        String normalized = address.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    @Override
    public String toString() {
        return "DatavinesParameters{"
                + "address='" + address + '\''
                + ", jobId=" + jobId
                + ", token='***'"
                + ", interval=" + interval
                + ", timeout=" + timeout
                + ", retryTimes=" + retryTimes
                + '}';
    }
}
