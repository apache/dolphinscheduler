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

package org.apache.dolphinscheduler.plugin.task.flink;

import java.io.Serializable;

/**
 * Execution context for Flink Materialized Table tasks.
 * 
 * This class holds the execution context information specific to Flink Materialized Table tasks,
 * including connection parameters and other runtime information.
 */
public class FlinkMaterializedTableTaskExecutionContext implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Connection parameters for the Flink SQL Gateway.
     * 
     * This includes information needed to establish and maintain the connection.
     */
    private String connectionParams;

    /**
     * Gets the connection parameters.
     *
     * @return The connection parameters as a string
     */
    public String getConnectionParams() {
        return connectionParams;
    }

    /**
     * Sets the connection parameters.
     *
     * @param connectionParams The connection parameters to set
     */
    public void setConnectionParams(String connectionParams) {
        this.connectionParams = connectionParams;
    }

    /**
     * Returns a string representation of the execution context.
     *
     * @return A string containing the execution context information
     */
    @Override
    public String toString() {
        return "FlinkMaterializedTableTaskExecutionContext{"
                + "connectionParams='" + connectionParams + '\''
                + '}';
    }
}
