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

package org.apache.dolphinscheduler.plugin.datasource.mysql.param;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

/**
 * Dynamic driver loader supporting isolated loading of different MySQL driver versions
 */
@Slf4j
public class DynamicDriverLoader {

    private static final Map<String, Driver> DRIVER_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, ClassLoader> CLASSLOADER_CACHE = new ConcurrentHashMap<>();

    /**
     * Dynamically load driver based on JAR path and class name
     * @param driverJarPath Driver JAR file path
     * @param driverClassName Driver class name
     * @return Driver instance
     * @throws Exception Loading exception
     */
    public static Driver loadDriver(String driverJarPath, String driverClassName) throws Exception {
        String cacheKey = driverJarPath + "::" + driverClassName;

        // Check cache
        if (DRIVER_CACHE.containsKey(cacheKey)) {
            return DRIVER_CACHE.get(cacheKey);
        }

        File jarFile = new File(driverJarPath);
        if (!jarFile.exists()) {
            throw new RuntimeException("Driver JAR file not found: " + driverJarPath);
        }

        // Create isolated class loader
        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{jarFile.toURI().toURL()},
                null // Use null as parent classloader for complete isolation
        );

        try {
            // Load driver class
            Class<?> driverClass = classLoader.loadClass(driverClassName);
            Driver driver = (Driver) driverClass.getDeclaredConstructor().newInstance();

            // Cache driver and classloader
            DRIVER_CACHE.put(cacheKey, driver);
            CLASSLOADER_CACHE.put(cacheKey, classLoader);

            log.info("Successfully loaded MySQL driver from {}: {}", driverJarPath, driverClassName);
            return driver;
        } catch (Exception e) {
            classLoader.close();
            throw new RuntimeException("Failed to load driver: " + driverClassName + " from " + driverJarPath, e);
        }
    }
}
