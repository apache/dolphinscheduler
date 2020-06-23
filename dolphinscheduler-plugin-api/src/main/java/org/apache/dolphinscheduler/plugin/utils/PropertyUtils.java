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
package org.apache.dolphinscheduler.plugin.utils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * property utils
 * single instance
 */
public class PropertyUtils {

    private static final Logger logger = LoggerFactory.getLogger(PropertyUtils.class);

    private static final Properties properties = new Properties();

    private PropertyUtils() {
        throw new IllegalStateException("PropertyUtils class");
    }

    static {
        String propertyFiles = "/plugin.properties";
        InputStream fis = null;
        try {
            fis = PropertyUtils.class.getResourceAsStream(propertyFiles);
            properties.load(fis);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            if (fis != null) {
                IOUtils.closeQuietly(fis);
            }
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    /**
     * get property value
     *
     * @param key        property name
     * @param defaultVal default value
     * @return property value
     */
    public static String getString(String key, String defaultVal) {
        String val = properties.getProperty(key.trim());
        return val == null ? defaultVal : val;
    }

    /**
     * get property value
     *
     * @param key property name
     * @return property value
     */
    public static String getString(String key) {
        if (key == null) {
            return null;
        }
        return properties.getProperty(key.trim());
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
     * get int
     *
     * @param key          key
     * @param defaultValue default value
     * @return property value
     */
    public static int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key.trim());
        if (value == null) {
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
        String value = properties.getProperty(key.trim());
        if (value == null) {
            return false;
        }

        return Boolean.parseBoolean(value);
    }

    /**
     * get property value
     *
     * @param key          property name
     * @param defaultValue default value
     * @return property value
     */
    public static Boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key.trim());
        if (value == null) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value);
    }

    /**
     * get property long value
     *
     * @param key        key
     * @param defaultVal default value
     * @return property value
     */
    public static long getLong(String key, long defaultVal) {
        String val = getString(key);
        return val == null ? defaultVal : Long.parseLong(val);
    }

    /**
     * get long
     *
     * @param key key
     * @return property value
     */
    public static long getLong(String key) {
        return getLong(key, -1);
    }

    /**
     * get double
     *
     * @param key        key
     * @param defaultVal default value
     * @return property value
     */
    public static double getDouble(String key, double defaultVal) {
        String val = properties.getProperty(key.trim());
        return val == null ? defaultVal : Double.parseDouble(val);
    }

    /**
     * @param key          key
     * @param type         type
     * @param defaultValue default value
     * @param <T>          T
     * @return get enum value
     */
    public <T extends Enum<T>> T getEnum(String key, Class<T> type,
                                         T defaultValue) {
        String val = properties.getProperty(key.trim());
        return val == null ? defaultValue : Enum.valueOf(type, val);
    }

}
