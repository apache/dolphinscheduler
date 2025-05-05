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

package org.apache.dolphinscheduler.plugin.task.flink.gateway.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Response indicating that the SQL Gateway result is not yet ready.
 */
public class PendingSqlGatewayResponse implements SqlGatewayResponse {

    private final String resultType;
    private final String nextResultUri;
    private final String jobId;

    public PendingSqlGatewayResponse(String resultType, String nextResultUri, String jobId) {
        this.resultType = resultType;
        this.nextResultUri = nextResultUri;
        this.jobId = jobId;
    }

    @Override
    public String getResultType() {
        return resultType;
    }

    @Override
    public String getNextResultUri() {
        return nextResultUri;
    }

    @Override
    public List<Row> getResult() {
        return Collections.emptyList();
    }

    @Override
    public String getJobId() {
        return jobId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PendingSqlGatewayResponse that = (PendingSqlGatewayResponse) o;
        return Objects.equals(resultType, that.resultType)
                && Objects.equals(nextResultUri, that.nextResultUri)
                && Objects.equals(jobId, that.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultType, nextResultUri, jobId);
    }

    @Override
    public String toString() {
        return "PendingSqlGatewayResponse{"
                + "resultType='" + resultType + '\''
                + ", nextResultUri='" + nextResultUri + '\''
                + ", jobId='" + jobId + '\''
                + '}';
    }
}
