package org.apache.dolphinscheduler.alert.plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import io.airlift.resolver.ArtifactResolver;
import org.apache.dolphinscheduler.spi.DolphinSchedulerPlugin;
import org.apache.dolphinscheduler.spi.alert.AlertChannelFactory;
import org.apache.dolphinscheduler.spi.classloader.ThreadContextClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.artifact.Artifact;

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

import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Alert Plugin Loader
 * Load Alert Plugin from pom when development and run server in IDE
 * Load Alert Plugin from the plugin directory when running on the server
 */
public class AlertPluginLoader {
    private static final Logger logger = LoggerFactory.getLogger(AlertPluginLoader.class);

    private static final ImmutableList<String> ALERT_SPI_PACKAGES = ImmutableList.<String>builder()
            .add("org.apache.dolphinscheduler.spi.")
            .build();

    private final File installedPluginsDir;
    private final List<String> plugins;
    private final ArtifactResolver resolver;
    private final AlertChannelManager alertChannelManager;


    public AlertPluginLoader(DolphinPluginManagerConfig config, AlertChannelManager alertChannelManager) {
        installedPluginsDir = config.getInstalledPluginsDir();
        if (config.getPlugins() == null) {
            this.plugins = ImmutableList.of();
        }
        else {
            this.plugins = ImmutableList.copyOf(config.getPlugins());
        }

        this.alertChannelManager = requireNonNull(alertChannelManager, "alertChannelManager is null");
        this.resolver = new ArtifactResolver(config.getMavenLocalRepository(), config.getMavenRemoteRepository());
    }

    public void loadPlugins()
            throws Exception
    {
        for (File file : listPluginDirs(installedPluginsDir)) {
            if (file.isDirectory()) {
                loadPlugin(file.getAbsolutePath());
            }
        }

        for (String plugin : plugins) {
            loadPlugin(plugin);
        }
    }

    private void loadPlugin(String plugin)
            throws Exception
    {
        logger.info("-- Loading Alert plugin %s --", plugin);
        URLClassLoader pluginClassLoader = buildPluginClassLoader(plugin);
        try (ThreadContextClassLoader ignored = new ThreadContextClassLoader(pluginClassLoader)) {
            loadPlugin(pluginClassLoader);
        }
        logger.info("-- Finished loading Alert plugin %s --", plugin);
    }

    private void loadPlugin(URLClassLoader pluginClassLoader)
    {
        ServiceLoader<DolphinSchedulerPlugin> serviceLoader = ServiceLoader.load(DolphinSchedulerPlugin.class, pluginClassLoader);
        List<DolphinSchedulerPlugin> plugins = ImmutableList.copyOf(serviceLoader);
        checkState(!plugins.isEmpty(), "No service providers the plugin %s", DolphinSchedulerPlugin.class.getName());
        for (DolphinSchedulerPlugin plugin : plugins) {
            logger.info("Installing %s", plugin.getClass().getName());
            installPlugin(plugin);
        }
    }

    public void installPlugin(DolphinSchedulerPlugin plugin)
    {
        for (AlertChannelFactory alertChannelFactory : plugin.getAlertChannelFactorys()) {
            logger.info("Registering Alert Plugin %s", alertChannelFactory.getId());
            alertChannelManager.addAlertChannelFactory(alertChannelFactory);
        }
    }

    private URLClassLoader buildPluginClassLoader(String plugin)
            throws Exception
    {
        File file = new File(plugin);
        if (file.isFile() && (file.getName().equals("pom.xml") || file.getName().endsWith(".pom"))) {
            return buildPluginClassLoaderFromPom(file);
        }
        if (file.isDirectory()) {
            return buildPluginClassLoaderFromDirectory(file);
        }
        else {
            throw new IllegalArgumentException(format("plugin must be a pom file or directory %s .", plugin));
        }
    }

    private URLClassLoader buildPluginClassLoaderFromPom(File pomFile)
            throws Exception
    {
        List<Artifact> artifacts = resolver.resolvePom(pomFile);
        URLClassLoader classLoader = createClassLoader(artifacts, pomFile.getPath());

        Artifact artifact = artifacts.get(0);
        Set<String> plugins = AlertPluginDiscovery.discoverPluginsFromArtifact(artifact, classLoader);
        if (!plugins.isEmpty()) {
            AlertPluginDiscovery.writePluginServices(plugins, artifact.getFile());
        }

        return classLoader;
    }

    private URLClassLoader buildPluginClassLoaderFromDirectory(File dir)
            throws Exception
    {
        logger.info("Classpath for %s:", dir.getName());
        List<URL> urls = new ArrayList<>();
        for (File file : listPluginDirs(dir)) {
            logger.info("   %s", file);
            urls.add(file.toURI().toURL());
        }
        return createClassLoader(urls);
    }

    private URLClassLoader createClassLoader(List<Artifact> artifacts, String name)
            throws IOException
    {
        logger.info("Classpath for %s:", name);
        List<URL> urls = new ArrayList<>();
        for (Artifact artifact : sortArtifacts(artifacts)) {
            if (artifact.getFile() == null) {
                throw new RuntimeException("Could not resolve artifact: " + artifact);
            }
            File file = artifact.getFile().getCanonicalFile();
            logger.info("    %s", file);
            urls.add(file.toURI().toURL());
        }
        return createClassLoader(urls);
    }

    private URLClassLoader createClassLoader(List<URL> urls)
    {
        ClassLoader parent = getClass().getClassLoader();
        return new DolphinPluginClassLoader(urls, parent, ALERT_SPI_PACKAGES);
    }


    private static List<File> listPluginDirs(File installedPluginsDir)
    {
        if (installedPluginsDir != null && installedPluginsDir.isDirectory()) {
            File[] files = installedPluginsDir.listFiles();
            if (files != null) {
                Arrays.sort(files);
                return ImmutableList.copyOf(files);
            }
        }
        return ImmutableList.of();
    }

    private static List<Artifact> sortArtifacts(List<Artifact> artifacts)
    {
        List<Artifact> list = new ArrayList<>(artifacts);
        Collections.sort(list, Ordering.natural().nullsLast().onResultOf(Artifact::getFile));
        return list;
    }

}
