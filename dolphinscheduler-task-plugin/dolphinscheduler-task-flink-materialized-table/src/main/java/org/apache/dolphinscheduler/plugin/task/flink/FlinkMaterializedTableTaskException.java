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

/**
 * Custom exception class for Flink Materialized Table tasks.
 * 
 * This exception is thrown when errors occur during the execution of Flink Materialized Table tasks.
 * It provides specific error handling for materialized table operations.
 */
public class FlinkMaterializedTableTaskException extends RuntimeException {

    /**
     * Default constructor.
     * 
     * Creates a new exception without a message or cause.
     */
    public FlinkMaterializedTableTaskException() {
        super();
    }

    /**
     * Constructor with error message.
     * 
     * Creates a new exception with the specified error message.
     *
     * @param message The error message
     */
    public FlinkMaterializedTableTaskException(String message) {
        super(message);
    }

    /**
     * Constructor with error message and cause.
     * 
     * Creates a new exception with the specified error message and cause.
     *
     * @param message The error message
     * @param cause The cause of the exception
     */
    public FlinkMaterializedTableTaskException(String message, Throwable cause) {
        super(message, cause);
    }
}
