package org.apache.dolphinscheduler.alert.plugin;

import com.google.common.collect.ImmutableList;
import org.apache.dolphinscheduler.alert.AlertServer;
import org.apache.dolphinscheduler.alert.utils.PropertyUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DolphinPluginLoader Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>七月 16, 2020</pre>
 */
public class DolphinPluginLoaderTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: loadPlugins()
     */
    @Test
    public void testLoadPlugins() throws Exception {
        System.out.println(System.getProperty("user.dir"));
        AlertPluginManager alertPluginManager = new AlertPluginManager();
        DolphinPluginManagerConfig alertPluginManagerConfig = new DolphinPluginManagerConfig();
        alertPluginManagerConfig.setPlugins("../dolphinscheduler-alert-plugin/dolphinscheduler-alert-email/pom.xml");
        if (StringUtils.isNotBlank(PropertyUtils.getString(AlertServer.ALERT_PLUGIN_DIR))) {
            alertPluginManagerConfig.setInstalledPluginsDir(PropertyUtils.getString(AlertServer.ALERT_PLUGIN_DIR).trim());
        }

        if (StringUtils.isNotBlank(PropertyUtils.getString(AlertServer.MAVEN_LOCAL_REPOSITORY))) {
            alertPluginManagerConfig.setMavenLocalRepository(PropertyUtils.getString(AlertServer.MAVEN_LOCAL_REPOSITORY).trim());
        }

        DolphinPluginLoader alertPluginLoader = new DolphinPluginLoader(alertPluginManagerConfig, ImmutableList.of(alertPluginManager));
        try {
            alertPluginLoader.loadPlugins();
        } catch (Exception e) {
            throw new RuntimeException("load Alert Plugin Failed !", e);
        }

        Assert.assertNotNull(alertPluginManager.getAlertChannelFactoryMap().get("email alert"));

    }

} 
