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

import org.apache.dolphinscheduler.plugin.task.flink.gateway.serde.FetchResultResponseBodyDeserializer;
import org.apache.dolphinscheduler.plugin.task.flink.gateway.serde.FetchResultResponseBodySerializer;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Response body for fetch result operations from Flink SQL Gateway.
 * 
 * This interface represents the response structure for SQL query executions,
 * containing the result data and associated metadata. It is used to handle
 * the response from Flink SQL Gateway's fetch result operations.
 */
@JsonSerialize(using = FetchResultResponseBodySerializer.class)
@JsonDeserialize(using = FetchResultResponseBodyDeserializer.class)
public interface FetchResultResponseBody {

    /**
     * Gets the type of the result.
     *
     * @return The result type as a string
     */
    String getResultType();

    /**
     * Gets the URI for the next set of results, if available.
     *
     * @return The URI for the next result set, or null if no more results
     */
    String getNextResultUri();

    /**
     * Gets the list of rows containing the query results.
     *
     * @return List of Row objects containing the query results
     */
    List<Row> getResult();

    /**
     * Gets the ID of the job that produced these results.
     *
     * @return The job ID as a string
     */
    String getJobId();
}
