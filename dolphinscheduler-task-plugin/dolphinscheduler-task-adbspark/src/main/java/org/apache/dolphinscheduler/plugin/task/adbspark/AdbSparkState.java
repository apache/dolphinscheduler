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

package org.apache.dolphinscheduler.plugin.task.adbspark;

import org.apache.commons.lang3.StringUtils;

public enum AdbSparkState {

    SUBMITTED("SUBMITTED"),
    STARTING("STARTING"),
    RUNNING("RUNNING"),
    FAILING("FAILING"),
    SUCCEEDING("SUCCEEDING"),
    KILLING("KILLING"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED"),
    KILLED("KILLED"),
    FATAL("FATAL"),
    UNKNOWN("UNKNOWN");

    private final String value;

    AdbSparkState(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public static AdbSparkState fromValue(String value) {
        if (StringUtils.isNotBlank(value)) {
            for (AdbSparkState state : AdbSparkState.values()) {
                if (state.value.equalsIgnoreCase(value)) {
                    return state;
                }
            }

            throw new IllegalArgumentException("Cannot create enum from " + value + " value!");
        } else {
            throw new IllegalArgumentException("Value cannot be null or empty!");
        }
    }

}
