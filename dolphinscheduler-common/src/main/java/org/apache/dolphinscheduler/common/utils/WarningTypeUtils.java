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

import org.apache.dolphinscheduler.common.enums.WarningType;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class WarningTypeUtils {

    private WarningTypeUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Parse warning types from comma-separated string.
     *
     * @param warningType comma-separated warning type codes (e.g., "1,2,3")
     * @return list of WarningType, empty list if input is blank
     */
    public static List<WarningType> parseFromString(String warningType) {
        if (StringUtils.isBlank(warningType)) {
            return Collections.emptyList();
        }
        return Arrays.stream(warningType.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try {
                        int code = Integer.parseInt(s);
                        for (WarningType wt : WarningType.values()) {
                            if (wt.getCode() == code) {
                                return wt;
                            }
                        }
                    } catch (NumberFormatException e) {
                        // ignore invalid code
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of WarningType to comma-separated string.
     *
     * @param warningTypes list of WarningType
     * @return comma-separated string, null if list is empty
     */
    public static String convertToString(List<WarningType> warningTypes) {
        if (warningTypes == null || warningTypes.isEmpty()) {
            return null;
        }
        return warningTypes.stream()
                .map(wt -> String.valueOf(wt.getCode()))
                .collect(Collectors.joining(","));
    }
}
