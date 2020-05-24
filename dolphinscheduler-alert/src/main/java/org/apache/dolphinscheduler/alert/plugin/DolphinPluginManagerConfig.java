package org.apache.dolphinscheduler.alert.plugin;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Dolphin Scheduler Plugin Manager Config
 */
public class DolphinPluginManagerConfig {

    private static final Logger logger = LoggerFactory.getLogger(DolphinPluginManagerConfig.class);

    /**
     * The dir of the Alert Plugin in.
     * When AlertServer is running on the server, it will load the Alert Plugin from this directory.
     */
    private File installedPluginsDir = new File("plugin/alert");
    private List<String> plugins;

    /**
     * Development, When AlertServer is running on IDE, AlertPluginLoad can load Alert Plugin from local Repository.
     */
    private String mavenLocalRepository = System.getProperty("user.home") + "/.m2/repository";
    private List<String> mavenRemoteRepository = ImmutableList.of("http://repo1.maven.org/maven2/");

    public File getInstalledPluginsDir()
    {
        return installedPluginsDir;
    }

    /**
     * @param pluginDir
     */
    public DolphinPluginManagerConfig setInstalledPluginsDir(String pluginDir) {
        requireNonNull(pluginDir, "pluginDir can not be null");
        File pluginDirFile = new File(pluginDir);
        if(!pluginDirFile.exists()) {
            throw new IllegalArgumentException(format("plugin dir not exists ! {}", pluginDirFile.getPath()));
        }
        this.installedPluginsDir = pluginDirFile;
        return this;
    }

    public List<String> getPlugins()
    {
        return plugins;
    }


    public DolphinPluginManagerConfig setPlugins(List<String> plugins)
    {
        this.plugins = plugins;
        return this;
    }

    /**
     * When development and run server in IDE, this method can set plugins in alert.properties .
     * Then when you start AlertServer in IDE, the plugin can be load.
     * eg:
     * file: alert.properties
     * alert.plugin=\
     *   ../dolphinscheduler-alert-plugin/dolphinscheduler-alert-email/pom.xml, \
     *   ../dolphinscheduler-alert-plugin/dolphinscheduler-alert-wechat/pom.xml
     * @param plugins
     * @return
     */
    public DolphinPluginManagerConfig setPlugins(String plugins)
    {
        if (plugins == null) {
            this.plugins = null;
        }
        else {
            this.plugins = ImmutableList.copyOf(Splitter.on(',').omitEmptyStrings().trimResults().split(plugins));
        }
        return this;
    }

    public String getMavenLocalRepository()
    {
        return mavenLocalRepository;
    }

    public DolphinPluginManagerConfig setMavenLocalRepository(String mavenLocalRepository)
    {
        this.mavenLocalRepository = mavenLocalRepository;
        return this;
    }

    public List<String> getMavenRemoteRepository()
    {
        return mavenRemoteRepository;
    }

    public DolphinPluginManagerConfig setMavenRemoteRepository(List<String> mavenRemoteRepository)
    {
        this.mavenRemoteRepository = mavenRemoteRepository;
        return this;
    }

    public DolphinPluginManagerConfig setMavenRemoteRepository(String mavenRemoteRepository)
    {
        this.mavenRemoteRepository = ImmutableList.copyOf(Splitter.on(',').omitEmptyStrings().trimResults().split(mavenRemoteRepository));
        return this;
    }
}
