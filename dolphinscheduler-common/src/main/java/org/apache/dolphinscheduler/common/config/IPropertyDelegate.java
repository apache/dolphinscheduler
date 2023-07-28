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

package org.apache.dolphinscheduler.common.config;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public interface IPropertyDelegate {

    String get(String key);

    String get(String key, String defaultValue);

    Set<String> getPropertyKeys();

    default Optional<String> getOptional(String key) {
        return getOptional(key, Function.identity());
    }

    default Integer getInt(String key) {
        return get(key, Integer::parseInt);
    }

    default Integer getInt(String key, Integer defaultValue) {
        return get(key, Integer::parseInt, defaultValue);
    }

    default Long getLong(String key) {
        return get(key, Long::parseLong);
    }

    default Long getLong(String key, Long defaultValue) {
        return get(key, Long::parseLong, defaultValue);
    }

    default Double getDouble(String key) {
        return get(key, Double::parseDouble);
    }

    default Double getDouble(String key, Double defaultValue) {
        return get(key, Double::parseDouble, defaultValue);
    }

    default Boolean getBoolean(String key) {
        return get(key, Boolean::parseBoolean);
    }

    default Boolean getBoolean(String key, Boolean defaultValue) {
        return get(key, Boolean::parseBoolean, defaultValue);
    }

    default <T> T get(String key, Function<String, T> transformFunction) {
        String value = get(key);
        if (value == null) {
            return null;
        }
        return transformFunction.apply(value);
    }

    default <T> T get(String key, Function<String, T> transformFunction, T defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return transformFunction.apply(value);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    default <T> Optional<T> getOptional(String key, Function<String, T> transformFunction) {
        return Optional.ofNullable(get(key, transformFunction));
    }
}
