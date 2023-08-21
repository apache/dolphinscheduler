/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.listener.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClassLoaderUtil {

    private final ConcurrentHashMap<String, PluginJar> localCache = new ConcurrentHashMap<>();

    public URLClassLoader getClassLoader(String jarPath) throws Exception {
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        if (!method.isAccessible()) {
            method.setAccessible(true);
        }
        URLClassLoader classLoader = new URLClassLoader(new URL[]{}, getClass().getClassLoader());
        File jarFile = new File(jarPath);
        method.invoke(classLoader, jarFile.toURI().toURL());
        PluginJar jarEntity = PluginJar.builder()
                .jarPath(jarPath)
                .urlClassLoader(classLoader)
                .jarFile(new JarFile(jarFile))
                .build();
        localCache.put(jarPath, jarEntity);
        return classLoader;
    }

    public void removeJarFile(String jarPath) throws Exception {
        PluginJar jarEntity = localCache.get(jarPath);
        jarEntity.getUrlClassLoader().close();
        jarEntity.getJarFile().close();
        localCache.remove(jarEntity.getJarPath());
    }
}
