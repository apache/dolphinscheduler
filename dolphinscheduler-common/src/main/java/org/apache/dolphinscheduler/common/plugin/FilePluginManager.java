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
package org.apache.dolphinscheduler.common.plugin;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.plugin.api.AlertPlugin;
import org.apache.dolphinscheduler.plugin.spi.AlertPluginProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FilePluginManager
 */
public class FilePluginManager implements PluginManager {

    private static final Logger logger = LoggerFactory.getLogger(FilePluginManager.class);

    private Map<String, AlertPlugin> pluginMap = new ConcurrentHashMap<>();

    private Map<String, ServiceLoader<AlertPluginProvider>> pluginLoaderMap = new ConcurrentHashMap<>();

    private Map<String, PluginClassLoader> classLoaderMap = new ConcurrentHashMap<>();

    private String[] whitePrefixes;

    private String[] excludePrefixes;

    public FilePluginManager(String dirPath, String[] whitePrefixes, String[] excludePrefixes) {
        this.whitePrefixes = whitePrefixes;
        this.excludePrefixes = excludePrefixes;
        try {
            load(dirPath);
        } catch (MalformedURLException e) {
            logger.error("load plugins failed.", e);
        }
    }

    private void load(String dirPath) throws MalformedURLException {
        logger.info("start to load jar files in {}", dirPath);
        if (dirPath == null) {
            logger.error("not a valid path - {}", dirPath);
            return;
        }
        File[] files = new File(dirPath).listFiles();
        if (files == null) {
            logger.error("not a valid path - {}", dirPath);
            return;
        }
        for (File file : files) {
            if (file.isDirectory() && !file.getPath().endsWith(Constants.PLUGIN_JAR_SUFFIX)) {
                continue;
            }
            String pluginName = file.getName()
                    .substring(0, file.getName().length() - Constants.PLUGIN_JAR_SUFFIX.length());
            URL[] urls = new URL[]{ file.toURI().toURL() };
            PluginClassLoader classLoader =
                    new PluginClassLoader(urls, Thread.currentThread().getContextClassLoader(), whitePrefixes, excludePrefixes);
            classLoaderMap.put(pluginName, classLoader);

            ServiceLoader<AlertPluginProvider> loader = ServiceLoader.load(AlertPluginProvider.class, classLoader);
            pluginLoaderMap.put(pluginName, loader);

            loader.forEach(provider -> {
                AlertPlugin plugin = provider.createPlugin();
                pluginMap.put(plugin.getId(), plugin);
                logger.info("loaded plugin - {}", plugin.getId());
            });
        }
    }

    @Override
    public AlertPlugin findOne(String name) {
        return pluginMap.get(name);
    }

    @Override
    public Map<String, AlertPlugin> findAll() {
        return pluginMap;
    }

    @Override
    public void addPlugin(AlertPlugin plugin) {
        pluginMap.put(plugin.getId(), plugin);
    }

}
