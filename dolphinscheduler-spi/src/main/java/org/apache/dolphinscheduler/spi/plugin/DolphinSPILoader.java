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

package org.apache.dolphinscheduler.spi.plugin;

import org.apache.dolphinscheduler.spi.annotation.DolphinSchedulerSPI;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * SPI extension loader, there are two differences compare with {@link DolphinPluginLoader}.
 * <p>1. The extension interface loaded by DolphinSPILoader should be mark by {@link DolphinSchedulerSPI}
 * <p>2. The DolphinSPILoader will only load class from classpath, this means you don't need to package your extension individual.
 * This loader make it easy to create a new extension. If you hope to have a class isolation or multiple version of extension you need to use {@link DolphinPluginLoader}.
 * <p>More detail you can see https://github.com/apache/dolphinscheduler/issues/6612
 */
public class DolphinSPILoader {

    private static final Map<String, Object> SPI_INSTANCE_MAP = new ConcurrentHashMap<>();

    private static final String META_INF_PATH = "/META-INF/Dolphinscheduler/";

    /**
     * @param extensionInterface    extension interface, should be marked by {@link DolphinSchedulerSPI}
     * @param extensionInstanceName extension instance name, should be defined as key in /META-INF/dolphinscheduler/
     * @param <T>                   extension interface type
     * @return extension instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T loadSPI(Class<T> extensionInterface, String extensionInstanceName) {
        final Logger logger = LoggerFactory.getLogger(DolphinSPILoader.class);
        Preconditions.checkNotNull(extensionInterface, "SPI Interface cannot be null");
        Preconditions.checkNotNull(extensionInstanceName, "SPI Instance name cannot be null");
        return (T) SPI_INSTANCE_MAP.computeIfAbsent(extensionInstanceName, (instanceName) -> {
            Class<T> SPIInstanceClass = loadSPIClass(extensionInterface, extensionInstanceName);
            T t = newInstance(SPIInstanceClass);
            logger.info("load SPI Instance success, name: {}", extensionInstanceName);
            return t;
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> loadSPIClass(Class<T> SPIInterfaceClass, String SPIInstanceName) {
        DolphinSchedulerSPI annotation = SPIInterfaceClass.getAnnotation(DolphinSchedulerSPI.class);
        if (annotation == null) {
            throw new IllegalArgumentException(String.format("%s: is not a DolphinScheduler SPI class", SPIInterfaceClass));
        }
        final String SPIMetaPath = META_INF_PATH + SPIInterfaceClass.getName();
        String SPIInstanceClassName = null;
        try (InputStream inputStream = DolphinSPILoader.class.getResourceAsStream(SPIMetaPath)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            SPIInstanceClassName = properties.getProperty(SPIInstanceName);
            if (SPIInstanceClassName == null) {
                throw new IllegalArgumentException(String.format("Cannot find the target instance: %s", SPIInstanceName));
            }
            Class<T> subInstance = (Class<T>) Class.forName(SPIInstanceClassName);
            if (subInstance.isAssignableFrom(SPIInterfaceClass)) {
                throw new IllegalArgumentException(String.format("%s is not sub instance of %s", subInstance, SPIInterfaceClass));
            }
            return subInstance;
        } catch (IOException e) {
            throw new RuntimeException(String.format("SPI META-INF: %s cannot found", SPIMetaPath));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(String.format("SPI Class Instance: %s cannot found", SPIInstanceClassName));
        }
    }

    private static <T> T newInstance(Class<T> tClass) {
        Preconditions.checkNotNull(tClass, "class cannot be null");
        try {
            return tClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(String.format("SPI instance: %s create error", tClass), e);
        }
    }
}
