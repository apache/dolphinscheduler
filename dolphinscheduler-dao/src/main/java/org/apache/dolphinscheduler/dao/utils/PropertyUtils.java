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

import org.apache.dolphinscheduler.common.Constants;
import com.baomidou.mybatisplus.core.toolkit.IOUtils;
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

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(PropertyUtils.class);

    private static final Properties properties = new Properties();

    private static final PropertyUtils propertyUtils = new PropertyUtils();

    private PropertyUtils(){
        init();
    }

    /**
     * init
     */
    private void init(){
        String[] propertyFiles = new String[]{Constants.DATASOURCE_PROPERTIES};
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
     * @return get string value
     */
    public static String getString(String key) {
        return properties.getProperty(key);
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
     * @param key property name
     * @return  get property int value , if key == null, then return -1
     */
    public static int getInt(String key) {
        return getInt(key, -1);
    }

    /**
     * get property value
     * @param key key
     * @param defaultValue defaultValue
     * @return get property int value，if key == null ，then return  defaultValue
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
     *
     * @param key property name
     * @return property value
     */
    public static Boolean getBoolean(String key) {
        String value = properties.getProperty(key.trim());
        if(null != value){
            return Boolean.parseBoolean(value);
        }

        return false;
    }

    /**
     * get property value
     *
     * @param key property name
     * @param defaultValue default value
     * @return property value
     */
    public static Boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key.trim());
        if(null != value){
            return Boolean.parseBoolean(value);
        }

        return defaultValue;
    }

    /**
     * get property long value
     * @param key key
     * @param defaultVal default value
     * @return property value
     */
    public static long getLong(String key, long defaultVal) {
        String val = getString(key);
        return val == null ? defaultVal : Long.parseLong(val);
    }
}
