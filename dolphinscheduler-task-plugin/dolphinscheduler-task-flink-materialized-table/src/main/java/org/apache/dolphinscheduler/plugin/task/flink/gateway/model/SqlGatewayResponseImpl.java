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

import java.util.List;
import java.util.Objects;

/**
 * Implementation of SqlGatewayResponse interface.
 */
public class SqlGatewayResponseImpl implements SqlGatewayResponse {

    private String resultType;
    private String nextResultUri;
    private List<Row> results;
    private String jobId;

    /**
     * Constructs a new SqlGatewayResponseImpl with the specified parameters.
     *
     * @param resultType The type of result
     * @param nextResultUri URI for the next result set
     * @param results List of result rows
     * @param jobId The job identifier
     */
    public SqlGatewayResponseImpl(String resultType, String nextResultUri, List<Row> results, String jobId) {
        this.resultType = resultType;
        this.nextResultUri = nextResultUri;
        this.results = results;
        this.jobId = jobId;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public void setNextResultUri(String nextResultUri) {
        this.nextResultUri = nextResultUri;
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
        return results;
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
        SqlGatewayResponseImpl that = (SqlGatewayResponseImpl) o;
        return Objects.equals(resultType, that.resultType)
                && Objects.equals(nextResultUri, that.nextResultUri)
                && Objects.equals(results, that.results)
                && Objects.equals(jobId, that.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultType, nextResultUri, results, jobId);
    }

    @Override
    public String toString() {
        return "SqlGatewayResponseImpl{"
                + "resultType='" + resultType + '\''
                + ", nextResultUri='" + nextResultUri + '\''
                + ", results=" + results
                + ", jobId='" + jobId + '\''
                + '}';
    }

    /**
     * Implementation of Row interface representing a single row of results.
     */
    public static class RowImpl implements Row {

        private final List<String> values;

        /**
         * Constructs a new RowImpl with the specified values.
         *
         * @param values List of string values representing the row data
         */
        public RowImpl(List<String> values) {
            this.values = values;
        }

        @Override
        public List<String> getValues() {
            return values;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RowImpl row = (RowImpl) o;
            return Objects.equals(values, row.values);
        }

        @Override
        public int hashCode() {
            return Objects.hash(values);
        }

        @Override
        public String toString() {
            return "RowImpl{"
                    + "values=" + values
                    + '}';
        }
    }
}
