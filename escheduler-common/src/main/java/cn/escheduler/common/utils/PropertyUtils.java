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
package cn.escheduler.common.utils;

import cn.escheduler.common.Constants;
import cn.escheduler.common.enums.ResUploadType;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static cn.escheduler.common.Constants.COMMON_PROPERTIES_PATH;
import static cn.escheduler.common.Constants.HADOOP_PROPERTIES_PATH;

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
        String[] propertyFiles = new String[]{HADOOP_PROPERTIES_PATH,COMMON_PROPERTIES_PATH};
        for (String fileName : propertyFiles) {
            InputStream fis = null;
            try {
                fis = PropertyUtils.class.getResourceAsStream(fileName);
                properties.load(fis);

            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                System.exit(1);
            } finally {
                IOUtils.closeQuietly(fis);
            }
        }
    }

    /**
     * judge whether resource upload startup
     * @return
     */
    public static Boolean getResUploadStartupState(){
        String resUploadStartupType = PropertyUtils.getString(Constants.RES_UPLOAD_STARTUP_TYPE);
        ResUploadType resUploadType = ResUploadType.valueOf(resUploadStartupType);
        return resUploadType == ResUploadType.HDFS || resUploadType == ResUploadType.S3;
    }

    /**
     * get property value
     *
     * @param key property name
     * @return
     */
    public static String getString(String key) {
        return properties.getProperty(key.trim());
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
     *
     * @param key
     * @param defaultValue
     * @return
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
     * @return
     */
    public static Boolean getBoolean(String key) {
        String value = properties.getProperty(key.trim());
        if(null != value){
            return Boolean.parseBoolean(value);
        }

        return null;
    }

    /**
     * get property long value
     * @param key
     * @param defaultVal
     * @return
     */
    public static long getLong(String key, long defaultVal) {
        String val = getString(key);
        return val == null ? defaultVal : Long.parseLong(val);
    }

    /**
     *
     * @param key
     * @return
     */
    public static long getLong(String key) {
        return getLong(key,-1);
    }

    /**
     *
     * @param key
     * @param defaultVal
     * @return
     */
    public double getDouble(String key, double defaultVal) {
        String val = getString(key);
        return val == null ? defaultVal : Double.parseDouble(val);
    }


    /**
     *  get array
     * @param key       property name
     * @param splitStr  separator
     * @return
     */
    public static String[] getArray(String key, String splitStr) {
        String value = getString(key);
        if (value == null) {
            return null;
        }
        try {
            String[] propertyArray = value.split(splitStr);
            return propertyArray;
        } catch (NumberFormatException e) {
            logger.info(e.getMessage(),e);
        }
        return null;
    }

    /**
     *
     * @param key
     * @param type
     * @param defaultValue
     * @param <T>
     * @return  get enum value
     */
    public <T extends Enum<T>> T getEnum(String key, Class<T> type,
                                         T defaultValue) {
        String val = getString(key);
        return val == null ? defaultValue : Enum.valueOf(type, val);
    }

    /**
     * get all properties with specified prefix, like: fs.
     * @param prefix prefix to search
     * @return
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
}
