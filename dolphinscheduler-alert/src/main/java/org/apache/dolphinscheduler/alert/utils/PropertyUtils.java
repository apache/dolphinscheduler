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
package org.apache.dolphinscheduler.alert.utils;

import org.apache.dolphinscheduler.common.utils.IOUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.PatternSyntaxException;

import static org.apache.dolphinscheduler.alert.utils.Constants.ALERT_PROPERTIES_PATH;

/**
 * property utils
 * single instance
 */
public class PropertyUtils {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(PropertyUtils.class);

    private static final Properties properties = new Properties();

    private static final PropertyUtils propertyUtils = new PropertyUtils();

    private PropertyUtils(){
        init();
    }

    private void init(){
        String[] propertyFiles = new String[]{ALERT_PROPERTIES_PATH};
        for (String fileName : propertyFiles) {
            InputStream fis = null;
            try {
                fis = PropertyUtils.class.getResourceAsStream(fileName);
                properties.load(fis);

            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                if (fis != null) {
                    IOUtils.closeQuietly(fis);
                }
                System.exit(1);
            } finally {
                IOUtils.closeQuietly(fis);
            }
        }
    }

    /**
     * get property value
     * @param key property name
     * @return the value
     */
    public static String getString(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        return properties.getProperty(key.trim());
    }

    /**
     * get property value
     *
     * @param key property name
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
     * @return  get property int value , if key == null, then return -1
     */
    public static int getInt(String key) {

        return getInt(key, -1);
    }

    /**
     * get int value
     * @param key the key
     * @param defaultValue the default value
     * @return the value related the key or the default value if the key not existed
     */
    public static int getInt(String key, int defaultValue) {
        String value = getString(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.info(e.getMessage(),e);
        }
        return defaultValue;
    }

    /**
     * get property value
     * @param key property name
     * @return  the boolean result value
     */
    public static Boolean getBoolean(String key) {

        if (StringUtils.isEmpty(key)) {
            return false;
        }

        String value = properties.getProperty(key.trim());
        if(null != value){
            return Boolean.parseBoolean(value);
        }

        return false;
    }

    /**
     * get long value
     * @param key the key
     * @return if the value not existed, return -1, or will return the related value
     */
    public static long getLong(String key) {
        return getLong(key,-1);
    }

    /**
     * get long value
     * @param key the key
     * @param defaultVal the default value
     * @return the value related the key or the default value if the key not existed
     */
    public static long getLong(String key, long defaultVal) {

        String val = getString(key);
        if (val == null) {
            return defaultVal;
        }

        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            logger.info(e.getMessage(),e);
        }

        return defaultVal;
    }

    /**
     * get double value
     * @param key the key
     * @return if the value not existed, return -1.0, or will return the related value
     */
    public static double getDouble(String key) {
        String val = getString(key);
        return getDouble(key,-1.0);
    }

    /**
     * get double value
     * @param key the key
     * @param defaultVal the default value
     * @return the value related the key or the default value if the key not existed
     */
    public static double getDouble(String key, double defaultVal) {

        String val = getString(key);
        if (val == null) {
            return defaultVal;
        }

        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            logger.info(e.getMessage(),e);
        }

        return defaultVal;
    }


    /**
     *  get array
     * @param key       property name
     * @param splitStr  separator
     * @return the result array
     */
    public static String[] getArray(String key, String splitStr) {
        String value = getString(key);
        if (value == null || StringUtils.isEmpty(splitStr)) {
            return null;
        }
        try {
            return value.split(splitStr);
        } catch (PatternSyntaxException e) {
            logger.info(e.getMessage(),e);
        }
        return null;
    }

    /**
     * get enum
     * @param key the key
     * @param type the class type
     * @param defaultValue the default value
     * @param <T> the generic class type
     * @return  get enum value
     */
    public static <T extends Enum<T>> T getEnum(String key, Class<T> type,
                                         T defaultValue) {
        String val = getString(key);
        if (val == null) {
            return defaultValue;
        }

        try {
            return Enum.valueOf(type, val);
        } catch (IllegalArgumentException e) {
            logger.info(e.getMessage(),e);
        }

        return defaultValue;
    }
}
