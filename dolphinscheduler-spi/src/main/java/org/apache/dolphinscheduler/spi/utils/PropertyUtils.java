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

package org.apache.dolphinscheduler.spi.utils;

import static org.apache.dolphinscheduler.spi.utils.Constants.COMMON_PROPERTIES_PATH;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * set value
     * @param key key
     * @param value value
     */
    public static void setValue(String key, String value) {
        properties.setProperty(key, value);
    }

}
