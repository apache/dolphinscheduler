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

import static org.apache.dolphinscheduler.common.constants.Constants.AWS_YAML_PATH;
import static org.apache.dolphinscheduler.common.constants.Constants.COMMON_PROPERTIES_PATH;
import static org.apache.dolphinscheduler.common.constants.Constants.REMOTE_LOGGING_YAML_PATH;

import org.apache.dolphinscheduler.common.config.ImmutablePriorityPropertyDelegate;
import org.apache.dolphinscheduler.common.config.ImmutablePropertyDelegate;
import org.apache.dolphinscheduler.common.config.ImmutableYamlDelegate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Strings;

@Slf4j
@UtilityClass
public class PropertyUtils {

    // todo: add another implementation for zookeeper/etcd/consul/xx
    private final ImmutablePriorityPropertyDelegate propertyDelegate =
            new ImmutablePriorityPropertyDelegate(
                    new ImmutablePropertyDelegate(COMMON_PROPERTIES_PATH),
                    new ImmutableYamlDelegate(REMOTE_LOGGING_YAML_PATH, AWS_YAML_PATH));

    public static String getString(String key) {
        return propertyDelegate.get(key.trim());
    }

    public static String getString(String key, String defaultVal) {
        String val = getString(key);
        return Strings.isNullOrEmpty(val) ? defaultVal : val;
    }

    public static String getUpperCaseString(String key) {
        String val = getString(key);
        return Strings.isNullOrEmpty(val) ? val : val.toUpperCase();
    }

    public static Integer getInt(String key) {
        return propertyDelegate.getInt(key.trim());
    }

    public static Integer getInt(String key, int defaultValue) {
        return propertyDelegate.getInt(key, defaultValue);
    }

    public static Boolean getBoolean(String key) {
        return propertyDelegate.getBoolean(key);
    }

    public static Boolean getBoolean(String key, Boolean defaultValue) {
        return propertyDelegate.getBoolean(key, defaultValue);
    }

    public static Long getLong(String key) {
        return propertyDelegate.getLong(key);
    }

    public static Long getLong(String key, Long defaultValue) {
        return propertyDelegate.getLong(key, defaultValue);
    }

    public static Double getDouble(String key) {
        return propertyDelegate.getDouble(key);
    }

    public static Double getDouble(String key, Double defaultValue) {
        return propertyDelegate.getDouble(key, defaultValue);
    }

    /**
     * get all properties with specified prefix, like: fs.
     *
     * @param prefix prefix to search
     * @return all properties with specified prefix
     */
    public static Map<String, String> getByPrefix(String prefix) {
        Map<String, String> matchedProperties = new HashMap<>();
        for (String propName : propertyDelegate.getPropertyKeys()) {
            if (propName.startsWith(prefix)) {
                matchedProperties.put(propName, propertyDelegate.get(propName));
            }
        }
        return matchedProperties;
    }

    /**
     * Get all properties with specified prefix, like: fs., will replace the prefix with newPrefix
     */
    public static Map<String, String> getByPrefix(String prefix, String newPrefix) {
        Map<String, String> matchedProperties = new HashMap<>();
        for (String propName : propertyDelegate.getPropertyKeys()) {
            if (propName.startsWith(prefix)) {
                matchedProperties.put(propName.replace(prefix, newPrefix), propertyDelegate.get(propName));
            }
        }
        return matchedProperties;
    }

    public static <T> Set<T> getSet(String key, Function<String, Set<T>> transformFunction, Set<T> defaultValue) {
        return propertyDelegate.get(key, transformFunction, defaultValue);
    }
}
