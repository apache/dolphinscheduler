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

package org.apache.dolphinscheduler.dao.utils;

public class EnvironmentUtils {

    private static final long EMPTY_ENVIRONMENT_CODE = -1L;

    /**
     * Check if the environment code is empty (we should use null instead of -1, this is used to comply with the original code)
     *
     * @return true if the environment code is empty, false otherwise
     */
    public static boolean isEnvironmentCodeEmpty(Long environmentCode) {
        return environmentCode == null || environmentCode <= 0;
    }

    /**
     * Get the empty environment code
     */
    public static Long getDefaultEnvironmentCode() {
        return EMPTY_ENVIRONMENT_CODE;
    }

    /**
     * Get the environment code or the default environment code if the environment code is empty
     */
    public static Long getEnvironmentCodeOrDefault(Long environmentCode) {
        return getEnvironmentCodeOrDefault(environmentCode, getDefaultEnvironmentCode());
    }

    /**
     * Get the environment code or the default environment code if the environment code is empty
     */
    public static Long getEnvironmentCodeOrDefault(Long environmentCode, Long defaultEnvironmentCode) {
        return isEnvironmentCodeEmpty(environmentCode) ? defaultEnvironmentCode : environmentCode;
    }

}
