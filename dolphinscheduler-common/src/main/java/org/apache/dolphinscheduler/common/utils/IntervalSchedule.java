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

package org.apache.dolphinscheduler.common.utils;

import lombok.Value;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Fixed interval schedule encoded as JSON.
 */
@Value
public class IntervalSchedule {

    long intervalMilliseconds;

    int repeatCount;

    public static IntervalSchedule parse(String expression) {
        final ObjectNode values;
        try {
            values = JSONUtils.parseObject(expression);
        } catch (Exception e) {
            throw new IllegalArgumentException("Interval schedule expression must be a JSON object", e);
        }
        if (values == null || !values.isObject()) {
            throw new IllegalArgumentException("Interval schedule expression must not be null");
        }

        int hours = valueOf(values, "hour", false);
        int minutes = valueOf(values, "minute", false);
        int seconds = valueOf(values, "second", false);
        int repeat = valueOf(values, "repeat", true);
        if (hours < 0 || minutes < 0 || seconds < 0 || repeat < -1) {
            throw new IllegalArgumentException("Interval duration values must not be negative");
        }
        if (minutes > 59 || seconds > 59) {
            throw new IllegalArgumentException("Interval minutes and seconds must be between 0 and 59");
        }

        long intervalMilliseconds = Math.addExact(
                Math.addExact(Math.multiplyExact(hours, 3_600_000L), Math.multiplyExact(minutes, 60_000L)),
                Math.multiplyExact(seconds, 1_000L));
        if (intervalMilliseconds == 0) {
            throw new IllegalArgumentException("Interval duration must be positive");
        }
        return new IntervalSchedule(intervalMilliseconds, repeat);
    }

    private static int valueOf(ObjectNode values, String key, boolean required) {
        if (!values.has(key)) {
            if (required) {
                throw new IllegalArgumentException("Interval schedule field is required: " + key);
            }
            return 0;
        }
        if (!values.get(key).isInt()) {
            throw new IllegalArgumentException("Interval schedule field must be an integer: " + key);
        }
        return values.get(key).intValue();
    }
}
