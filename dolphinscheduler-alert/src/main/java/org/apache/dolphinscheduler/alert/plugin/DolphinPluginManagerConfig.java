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

package org.apache.dolphinscheduler.alert.plugin;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

/**
 * Dolphin Scheduler Plugin Manager Config
 */
public class DolphinPluginManagerConfig {

    /**
     * The dir of the Alert Plugin in.
     * When AlertServer is running on the server, it will load the Alert Plugin from this directory.
     */
    private File installedPluginsDir;

    /**
     * The plugin should be load.
     * The installedPluginsDir is empty when we development and run server in IDEA. Then we can config which plugin should be load by param name alert.plugin.binding in the alert.properties file
     */
    private List<String> plugins;

    /**
     * Development, When AlertServer is running on IDE, AlertPluginLoad can load Alert Plugin from local Repository.
     */
    private String mavenLocalRepository = System.getProperty("user.home") + "/.m2/repository";
    private List<String> mavenRemoteRepository = ImmutableList.of("http://repo1.maven.org/maven2/");

    public File getInstalledPluginsDir() {
        return installedPluginsDir;
    }

    /**
     * @param pluginDir
     */
    public DolphinPluginManagerConfig setInstalledPluginsDir(String pluginDir) {
        requireNonNull(pluginDir, "pluginDir can not be null");
        File pluginDirFile = new File(pluginDir);
        if (!pluginDirFile.exists()) {
            throw new IllegalArgumentException(format("plugin dir not exists ! {}", pluginDirFile.getPath()));
        }
        this.installedPluginsDir = pluginDirFile;
        return this;
    }

    public List<String> getPlugins() {
        return plugins;
    }

    public DolphinPluginManagerConfig setPlugins(List<String> plugins) {
        this.plugins = plugins;
        return this;
    }

    /**
     * When development and run server in IDE, this method can set plugins in alert.properties .
     * Then when you start AlertServer in IDE, the plugin can be load.
     * eg:
     * file: alert.properties
     * alert.plugin=\
     * ../dolphinscheduler-alert-plugin/dolphinscheduler-alert-email/pom.xml, \
     * ../dolphinscheduler-alert-plugin/dolphinscheduler-alert-wechat/pom.xml
     *
     * @param plugins
     * @return
     */
    public DolphinPluginManagerConfig setPlugins(String plugins) {
        if (plugins == null) {
            this.plugins = null;
        } else {
            this.plugins = ImmutableList.copyOf(Splitter.on(',').omitEmptyStrings().trimResults().split(plugins));
        }
        return this;
    }

    public String getMavenLocalRepository() {
        return mavenLocalRepository;
    }

    public DolphinPluginManagerConfig setMavenLocalRepository(String mavenLocalRepository) {
        this.mavenLocalRepository = mavenLocalRepository;
        return this;
    }

    public List<String> getMavenRemoteRepository() {
        return mavenRemoteRepository;
    }

    public DolphinPluginManagerConfig setMavenRemoteRepository(List<String> mavenRemoteRepository) {
        this.mavenRemoteRepository = mavenRemoteRepository;
        return this;
    }

    public DolphinPluginManagerConfig setMavenRemoteRepository(String mavenRemoteRepository) {
        this.mavenRemoteRepository = ImmutableList.copyOf(Splitter.on(',').omitEmptyStrings().trimResults().split(mavenRemoteRepository));
        return this;
    }
}
