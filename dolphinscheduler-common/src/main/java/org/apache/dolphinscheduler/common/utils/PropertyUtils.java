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

import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.spi.enums.ResUploadType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.apache.dolphinscheduler.common.Constants.COMMON_PROPERTIES_PATH;

public class PropertyUtils {

    private static final Logger logger = LoggerFactory.getLogger(PropertyUtils.class);

    private static final Properties properties = new Properties();

    private PropertyUtils() {
        throw new UnsupportedOperationException("Construct PropertyUtils");
    }

    static {
        loadPropertyFile(COMMON_PROPERTIES_PATH);
    }

    public static synchronized void loadPropertyFile(String... propertyFiles) {
        for (String fileName : propertyFiles) {
            try (InputStream fis = PropertyUtils.class.getResourceAsStream(fileName);) {
                properties.load(fis);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                System.exit(1);
            }
        }

        // Override from system properties
        System.getProperties().forEach((k, v) -> {
            final String key = String.valueOf(k);
            logger.info("Overriding property from system property: {}", key);
            PropertyUtils.setValue(key, String.valueOf(v));
        });
    }

    /**
     * @return judge whether resource upload startup
     */
    public static boolean getResUploadStartupState() {
        String resUploadStartupType = PropertyUtils.getUpperCaseString(Constants.RESOURCE_STORAGE_TYPE);
        ResUploadType resUploadType = ResUploadType.valueOf(StringUtils.isEmpty(resUploadStartupType) ? ResUploadType.NONE.name() : resUploadStartupType);
        return resUploadType != ResUploadType.NONE;
    }

    /**
     * get property value
     *
     * @param key property name
     * @return property value
     */
    public static String getString(String key) {
        return properties.getProperty(key.trim());
    }

    /**
     * get property value with upper case
     *
     * @param key property name
     * @return property value  with upper case
     */
    public static String getUpperCaseString(String key) {
        String val = getString(key);
        return StringUtils.isEmpty(val) ? val : val.toUpperCase();
    }

    /**
     * get property value
     *
     * @param key property name
     * @param defaultVal default value
     * @return property value
     */
    public static String getString(String key, String defaultVal) {
        String val = getString(key);
        return StringUtils.isEmpty(val) ? defaultVal : val;
    }

    /**
     * get property value
     *
     * @param key property name
     * @return get property int value , if key == null, then return -1
     */
    public static int getInt(String key) {
        return getInt(key, -1);
    }

    /**
     * @param key key
     * @param defaultValue default value
     * @return property value
     */
    public static int getInt(String key, int defaultValue) {
        String value = getString(key);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.info(e.getMessage(), e);
        }
        return defaultValue;
    }

    /**
     * get property value
     *
     * @param key property name
     * @return property value
     */
    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /**
     * get property value
     *
     * @param key property name
     * @param defaultValue default value
     * @return property value
     */
    public static Boolean getBoolean(String key, boolean defaultValue) {
        String value = getString(key);
        return StringUtils.isEmpty(value) ? defaultValue : Boolean.parseBoolean(value);
    }

    /**
     * get property long value
     *
     * @param key key
     * @param defaultValue default value
     * @return property value
     */
    public static long getLong(String key, long defaultValue) {
        String value = getString(key);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            logger.info(e.getMessage(), e);
        }
        return defaultValue;
    }

    /**
     * @param key key
     * @return property value
     */
    public static long getLong(String key) {
        return getLong(key, -1);
    }

    /**
     * @param key key
     * @param defaultValue default value
     * @return property value
     */
    public static double getDouble(String key, double defaultValue) {
        String value = getString(key);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            logger.info(e.getMessage(), e);
        }
        return defaultValue;
    }

    /**
     * get array
     *
     * @param key property name
     * @param splitStr separator
     * @return property value through array
     */
    public static String[] getArray(String key, String splitStr) {
        String value = getString(key);
        if (StringUtils.isEmpty(value)) {
            return new String[0];
        }
        return value.split(splitStr);
    }

    /**
     * @param key key
     * @param type type
     * @param defaultValue default value
     * @param <T> T
     * @return get enum value
     */
    public static <T extends Enum<T>> T getEnum(String key, Class<T> type,
                                                T defaultValue) {
        String value = getString(key);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }

        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            logger.info(e.getMessage(), e);
        }
        return defaultValue;
    }

    /**
     * get all properties with specified prefix, like: fs.
     *
     * @param prefix prefix to search
     * @return all properties with specified prefix
     */
    public static Map<String, String> getPrefixedProperties(String prefix) {
        Map<String, String> matchedProperties = new HashMap<>();
        for (String propName : properties.stringPropertyNames()) {
            if (propName.startsWith(prefix)) {
                matchedProperties.put(propName, properties.getProperty(propName));
            }
        }
        return matchedProperties;
    }

    /**
     * set value
     * @param key key
     * @param value value
     */
    public static void setValue(String key, String value) {
        properties.setProperty(key, value);
    }

    public static Map<String, String> getPropertiesByPrefix(String prefix) {
        if (StringUtils.isEmpty(prefix)) {
            return null;
        }
        Set<Object> keys = properties.keySet();
        if (keys.isEmpty()) {
            return null;
        }
        Map<String, String> propertiesMap = new HashMap<>();
        keys.forEach(k -> {
            if (k.toString().contains(prefix)) {
                propertiesMap.put(k.toString().replaceFirst(prefix + ".", ""), properties.getProperty((String) k));
            }
        });
        return propertiesMap;
    }

}
