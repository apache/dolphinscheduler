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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

/**
 * Dolphin Scheduler Plugin Manager Config
 */
public class DolphinPluginManagerConfig {

    private static final Logger logger = LoggerFactory.getLogger(DolphinPluginManagerConfig.class);

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
    private final String defaultLocalRepository = System.getProperty("user.home") + "/.m2/repository";
    private String mavenLocalRepository = getMavenLocalRepositoryOrDefault(defaultLocalRepository);
    private List<String> mavenRemoteRepository = ImmutableList.of("https://repo1.maven.org/maven2/");

    File getInstalledPluginsDir() {
        return installedPluginsDir;
    }

    /**
     * @param pluginDir plugin directory
     */
    public void setInstalledPluginsDir(String pluginDir) {
        requireNonNull(pluginDir, "pluginDir can not be null");
        File pluginDirFile = new File(pluginDir);
        if (!pluginDirFile.exists()) {
            throw new IllegalArgumentException(format("plugin dir not exists ! %s", pluginDirFile.getPath()));
        }
        this.installedPluginsDir = pluginDirFile;
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
     * @param plugins plugins
     * @return DolphinPluginManagerConfig
     */
    public DolphinPluginManagerConfig setPlugins(String plugins) {
        if (plugins == null) {
            this.plugins = null;
        } else {
            this.plugins = ImmutableList.copyOf(Splitter.on(',').omitEmptyStrings().trimResults().split(plugins));
        }
        return this;
    }

    String getMavenLocalRepository() {
        return mavenLocalRepository;
    }

    public void setMavenLocalRepository(String mavenLocalRepository) {
        this.mavenLocalRepository = mavenLocalRepository;
    }

    List<String> getMavenRemoteRepository() {
        return mavenRemoteRepository;
    }

    /**
     * Get local repository from maven settings.xml if available.
     * <p>
     * if System environment does not exists settings.xml, return the default value.
     * </p>
     *
     * @param defaultRepository default repository path.
     * @return local repository path.
     */
    private String getMavenLocalRepositoryOrDefault(String defaultRepository) {
        // get 'settings.xml' from user home
        Path settingsXmlPath = getMavenSettingsXmlFromUserHome();
        // if user home does not exist settings.xml, get from '$MAVEN_HOME/conf/settings.xml'
        if (settingsXmlPath == null || !Files.exists(settingsXmlPath)) {
            logger.info("User home does not exists maven settings.xml");
            settingsXmlPath = getMavenSettingsXmlFromEvn();
        }
        // still not exist, return default repository
        if (settingsXmlPath == null || !Files.exists(settingsXmlPath)) {
            logger.info("Maven home does not exists maven settings.xml, use default");
            return defaultRepository;
        }
        // parse xml
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            // security settings
            try {
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            } catch (Exception e) {
                logger.warn("Error at parse settings.xml, setting security features: {}", e.getLocalizedMessage());
            }
            documentBuilder = factory.newDocumentBuilder();
            Document document = documentBuilder.parse(settingsXmlPath.toFile());
            // search node named 'localRepository'
            String localRepositoryNodeTag = "localRepository";
            NodeList nodeList = document.getElementsByTagName(localRepositoryNodeTag);
            int length = nodeList.getLength();
            if (length <= 0) {
                // if node not exists, return default repository
                logger.info("File {} does not contains node named {}", settingsXmlPath, localRepositoryNodeTag);
                return defaultRepository;
            }
            for (int i = 0; i < length; i++) {
                Node node = nodeList.item(i);
                String content = node.getTextContent();
                if (StringUtils.isNotEmpty(content) && StringUtils.isNotBlank(content)) {
                    Path localRepositoryPath = Paths.get(content);
                    if (Files.exists(localRepositoryPath)) {
                        logger.info("Got local repository path {}", content);
                        return content;
                    }
                }
            }
            return defaultRepository;
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return defaultRepository;
        }
    }

    /**
     * Get maven settings.xml file path from "${user.home}/.m2"
     * <p>
     * if "${user.home}/.m2/settings.xml" does not exist,
     * null will be returned
     * </p>
     *
     * @return settings.xml file path, could be null
     */
    private Path getMavenSettingsXmlFromUserHome() {
        String userHome = System.getProperty("user.home");
        Path settingsXmlPath = null;
        if (!StringUtils.isEmpty(userHome)) {
            settingsXmlPath = Paths.get(userHome, ".m2", "settings.xml").toAbsolutePath();
        }
        return settingsXmlPath;
    }

    /**
     * Get maven settings.xml file path from "${MAVEN_HOME}/conf"
     * <p>
     * if "${MAVEN_HOME}/conf/settings.xml" does not exist,
     * null will be returned
     * </p>
     *
     * @return settings.xml file path, could be null
     */
    private Path getMavenSettingsXmlFromEvn() {
        String mavenHome = System.getenv("MAVEN_HOME");
        Path settingsXmlPath = null;
        if (mavenHome == null) {
            mavenHome = System.getenv("M2_HOME");
        }
        if (mavenHome != null) {
            settingsXmlPath = Paths.get(mavenHome, "conf", "settings.xml").toAbsolutePath();
        }
        return settingsXmlPath;
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
