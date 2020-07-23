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


/**
 *  utility methods for validating input
 *
 */
public final class Preconditions {

    private Preconditions() {}

    /**
     * if obj is null will throw NPE
     *
     * @param obj obj
     * @param <T> T
     * @return T
     */
    public static <T> T checkNotNull(T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return obj;
    }

    /**
     * if obj is null will throw NullPointerException with error message
     * @param obj obj
     * @param errorMsg error message
     * @param <T> T
     * @return T
     */
    public static <T> T checkNotNull(T obj,  String errorMsg) {
        if (obj == null) {
            throw new NullPointerException(errorMsg);
        }
        return obj;
    }


    /**
     * if condition is false will throw an IllegalArgumentException with the given message
     *
     * @param condition condition
     * @param errorMsg  error message
     *
     * @throws IllegalArgumentException Thrown, if the condition is violated.
     */
    public static void checkArgument(boolean condition,  Object errorMsg) {
        if (!condition) {
            throw new IllegalArgumentException(String.valueOf(errorMsg));
        }
    }


}
