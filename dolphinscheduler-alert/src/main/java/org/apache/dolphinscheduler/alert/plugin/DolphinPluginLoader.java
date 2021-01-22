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

import static com.google.common.base.Preconditions.checkState;

import org.apache.dolphinscheduler.spi.DolphinSchedulerPlugin;
import org.apache.dolphinscheduler.spi.classloader.ThreadContextClassLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.artifact.Artifact;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import io.airlift.resolver.ArtifactResolver;

/**
 * Plugin Loader
 * Load Plugin from pom when development and run server in IDE
 * Load Plugin from the plugin directory when running on the server
 */
public class DolphinPluginLoader {
    private static final Logger logger = LoggerFactory.getLogger(DolphinPluginLoader.class);

    /**
     * All third-party jar packages used in the classes which in spi package need to be add
     */
    private static final ImmutableList<String> DOLPHIN_SPI_PACKAGES = ImmutableList.<String>builder()
            .add("org.apache.dolphinscheduler.spi.")
            .add("com.fasterxml.jackson.")
            .build();

    private final File installedPluginsDir;
    private final List<String> configPlugins;
    private ArtifactResolver resolver = null;
    private final List<AbstractDolphinPluginManager> dolphinPluginManagerList;

    public DolphinPluginLoader(DolphinPluginManagerConfig config, List<AbstractDolphinPluginManager> dolphinPluginManagerList) {
        installedPluginsDir = config.getInstalledPluginsDir();
        if (config.getPlugins() == null) {
            this.configPlugins = ImmutableList.of();
        } else {
            this.configPlugins = ImmutableList.copyOf(config.getPlugins());
        }

        this.dolphinPluginManagerList = requireNonNull(dolphinPluginManagerList, "dolphinPluginManagerList is null");
        if (configPlugins != null && configPlugins.size() > 0) {
            this.resolver = new ArtifactResolver(config.getMavenLocalRepository(), config.getMavenRemoteRepository());
        }
    }

    public void loadPlugins()
            throws Exception {
        for (File file : listPluginDirs(installedPluginsDir)) {
            if (file.isDirectory()) {
                loadPlugin(file.getAbsolutePath());
            }
        }

        for (String plugin : configPlugins) {
            loadPlugin(plugin);
        }
    }

    private void loadPlugin(String plugin)
            throws Exception {
        logger.info("-- Loading Alert plugin {} --", plugin);
        URLClassLoader pluginClassLoader = buildPluginClassLoader(plugin);
        try (ThreadContextClassLoader ignored = new ThreadContextClassLoader(pluginClassLoader)) {
            loadPlugin(pluginClassLoader);
        }
        logger.info("-- Finished loading Alert plugin {} --", plugin);
    }

    private void loadPlugin(URLClassLoader pluginClassLoader) {
        ServiceLoader<DolphinSchedulerPlugin> serviceLoader = ServiceLoader.load(DolphinSchedulerPlugin.class, pluginClassLoader);
        List<DolphinSchedulerPlugin> plugins = ImmutableList.copyOf(serviceLoader);
        checkState(!plugins.isEmpty(), "No service providers the plugin {}", DolphinSchedulerPlugin.class.getName());
        for (DolphinSchedulerPlugin plugin : plugins) {
            logger.info("Installing {}", plugin.getClass().getName());
            for (AbstractDolphinPluginManager dolphinPluginManager : dolphinPluginManagerList) {
                dolphinPluginManager.installPlugin(plugin);
            }
        }
    }

    private URLClassLoader buildPluginClassLoader(String plugin)
            throws Exception {
        File file = new File(plugin);

        if (!file.isDirectory() && (file.getName().equals("pom.xml") || file.getName().endsWith(".pom"))) {
            return buildPluginClassLoaderFromPom(file);
        }
        if (file.isDirectory()) {
            return buildPluginClassLoaderFromDirectory(file);
        } else {
            throw new IllegalArgumentException(format("plugin must be a pom file or directory {} .", plugin));
        }
    }

    private URLClassLoader buildPluginClassLoaderFromPom(File pomFile)
            throws Exception {
        List<Artifact> artifacts = resolver.resolvePom(pomFile);
        URLClassLoader classLoader = createClassLoader(artifacts, pomFile.getPath());

        Artifact artifact = artifacts.get(0);
        Set<String> plugins = DolphinPluginDiscovery.discoverPluginsFromArtifact(artifact, classLoader);
        if (!plugins.isEmpty()) {
            DolphinPluginDiscovery.writePluginServices(plugins, artifact.getFile());
        }

        return classLoader;
    }

    private URLClassLoader buildPluginClassLoaderFromDirectory(File dir)
            throws Exception {
        logger.info("Classpath for {}:", dir.getName());
        List<URL> urls = new ArrayList<>();
        for (File file : listPluginDirs(dir)) {
            logger.info("   {}", file);
            urls.add(file.toURI().toURL());
        }
        return createClassLoader(urls);
    }

    private URLClassLoader createClassLoader(List<Artifact> artifacts, String name)
            throws IOException {
        logger.info("Classpath for {}:", name);
        List<URL> urls = new ArrayList<>();
        for (Artifact artifact : sortArtifacts(artifacts)) {
            if (artifact.getFile() == null) {
                throw new RuntimeException("Could not resolve artifact: " + artifact);
            }
            File file = artifact.getFile().getCanonicalFile();
            logger.info("    {}", file);
            urls.add(file.toURI().toURL());
        }
        return createClassLoader(urls);
    }

    private URLClassLoader createClassLoader(List<URL> urls) {
        ClassLoader parent = getClass().getClassLoader();
        return new DolphinPluginClassLoader(urls, parent, DOLPHIN_SPI_PACKAGES);
    }

    private static List<File> listPluginDirs(File installedPluginsDir) {
        if (installedPluginsDir != null && installedPluginsDir.isDirectory()) {
            File[] files = installedPluginsDir.listFiles();
            if (files != null) {
                Arrays.sort(files);
                return ImmutableList.copyOf(files);
            }
        }
        return ImmutableList.of();
    }

    private static List<Artifact> sortArtifacts(List<Artifact> artifacts) {
        List<Artifact> list = new ArrayList<>(artifacts);
        Collections.sort(list, Ordering.natural().nullsLast().onResultOf(Artifact::getFile));
        return list;
    }

}
