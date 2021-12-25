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

package org.apache.dolphinscheduler.plugin.datasource.api.utils;

import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.commons.collections.CollectionUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassLoaderUtils {
    private static final Logger logger = LoggerFactory.getLogger(ClassLoaderUtils.class);

    public static ClassLoader getCustomClassLoader(Set<String> modulePaths, ClassLoader parentClassLoader, FilenameFilter filenameFilter) throws MalformedURLException {
        URL[] classpaths = getURLsForClasspath(modulePaths, filenameFilter);
        return createModuleClassLoader(modulePaths, classpaths, parentClassLoader);
    }

    public static URL[] getURLsForClasspath(Set<String> modulePaths, FilenameFilter filenameFilter) throws MalformedURLException {
        Set<String> modules = new LinkedHashSet<>();
        if (CollectionUtils.isNotEmpty(modulePaths)) {
            modulePaths.stream()
                    .flatMap(path -> Arrays.stream(path.split(",")))
                    .filter(StringUtils::isNotBlank)
                    .map(String::trim)
                    .forEach(modules::add);
        }
        return toURLs(modules, filenameFilter);
    }

    protected static URL[] toURLs(Set<String> modulePaths, FilenameFilter filenameFilter) throws MalformedURLException {
        List<URL> additionalClasspath = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(modulePaths)) {
            for (String modulePathString : modulePaths) {
                File modulePath = new File(modulePathString);
                if (modulePath.exists()) {
                    additionalClasspath.add(modulePath.toURI().toURL());
                    if (modulePath.isDirectory()) {
                        File[] files = modulePath.listFiles(filenameFilter);
                        if (files != null) {
                            for (File classpathResource : files) {
                                if (classpathResource.isDirectory()) {
                                    logger.warn("Recursive directories are not supported, skipping " + classpathResource.getAbsolutePath());
                                } else {
                                    additionalClasspath.add(classpathResource.toURI().toURL());
                                }
                            }
                        }
                    }
                }
            }
        }
        return additionalClasspath.toArray(new URL[additionalClasspath.size()]);
    }

    protected static ClassLoader createModuleClassLoader(Set<String> modulePaths, URL[] modules, ClassLoader parentClassLoader) {
        return new PluginClassLoader(modulePaths, modules, parentClassLoader);
    }
}
