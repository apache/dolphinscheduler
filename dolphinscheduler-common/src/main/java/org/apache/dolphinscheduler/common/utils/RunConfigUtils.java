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

import org.apache.commons.io.IOUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ResUploadType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.apache.dolphinscheduler.common.Constants.COMMON_PROPERTIES_PATH;
import static org.apache.dolphinscheduler.common.Constants.HADOOP_PROPERTIES_PATH;

/**
 * run confi utils
 * single instance
 */
public class RunConfigUtils {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(RunConfigUtils.class);

    private static final Properties properties = new Properties();

    private static final RunConfigUtils propertyUtils = new RunConfigUtils();

    private RunConfigUtils(){
        init();
    }

    private void init(){
        String[] propertyFiles = new String[]{Constants.RUN_CONFIG};
        for (String fileName : propertyFiles) {
            InputStream fis = null;
            try {
                fis = RunConfigUtils.class.getResourceAsStream(fileName);
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
     *
     * @param key property name
     * @return property value
     */
    public static String getString(String key) {
        return properties.getProperty(key.trim());
    }

}
