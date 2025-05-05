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

/**
 * Implementation of FetchResultResponseBody for when results are not yet ready.
 * 
 * This class represents a response from Flink SQL Gateway when the requested results
 * are not yet available. It provides the URI to fetch the results later and the
 * result type, but throws UnsupportedOperationException for result-related operations.
 */
public class NotReadyFetchResultResponseBody implements FetchResultResponseBody {

    /**
     * URI to fetch the results when they become available.
     */
    private String nextResultUri;

    /**
     * Type of the result that will be available.
     */
    private String resultType;

    /**
     * Constructs a new NotReadyFetchResultResponseBody.
     *
     * @param nextResultUri URI to fetch the results when they become available
     * @param resultType Type of the result that will be available
     */
    public NotReadyFetchResultResponseBody(String nextResultUri, String resultType) {
        this.nextResultUri = nextResultUri;
        this.resultType = resultType;
    }

    /**
     * Sets the URI for the next result fetch.
     *
     * @param nextResultUri URI to fetch the results when they become available
     */
    public void setNextResultUri(String nextResultUri) {
        this.nextResultUri = nextResultUri;
    }

    /**
     * Sets the type of the result.
     *
     * @param resultType Type of the result that will be available
     */
    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    /**
     * Gets the URI for the next result fetch.
     *
     * @return URI to fetch the results when they become available
     */
    @Override
    public String getNextResultUri() {
        return nextResultUri;
    }

    /**
     * Gets the type of the result.
     *
     * @return Type of the result that will be available
     */
    @Override
    public String getResultType() {
        return resultType;
    }

    /**
     * Gets the result rows.
     * This operation is not supported as the results are not yet ready.
     *
     * @throws UnsupportedOperationException always, as results are not yet ready
     */
    @Override
    public List<Row> getResult() {
        throw new UnsupportedOperationException("NotReadyFetchResultResponseBody does not support getResult()");
    }

    /**
     * Gets the job ID.
     * This operation is not supported as the results are not yet ready.
     *
     * @throws UnsupportedOperationException always, as results are not yet ready
     */
    @Override
    public String getJobId() {
        throw new UnsupportedOperationException("NotReadyFetchResultResponseBody does not support getJobId()");
    }
}
