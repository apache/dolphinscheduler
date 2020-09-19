package org.apache.dolphinscheduler.alert.plugin;

import org.apache.dolphinscheduler.alert.AlertServer;
import org.apache.dolphinscheduler.alert.utils.Constants;
import org.apache.dolphinscheduler.alert.utils.PropertyUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * AlertPluginManager Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Sep 19, 2020</pre>
 */
public class AlertPluginManagerTest {

    @Test
    public void testLoadPlugins() throws Exception {
        AlertPluginManager alertPluginManager = new AlertPluginManager();
        DolphinPluginManagerConfig alertPluginManagerConfig = new DolphinPluginManagerConfig();
        String path = DolphinPluginLoader.class.getClassLoader().getResource("").getPath();
        alertPluginManagerConfig.setPlugins(path + "../../../dolphinscheduler-alert-plugin/dolphinscheduler-alert-email/pom.xml");
        if (StringUtils.isNotBlank(PropertyUtils.getString(AlertServer.ALERT_PLUGIN_DIR))) {
            alertPluginManagerConfig.setInstalledPluginsDir(org.apache.dolphinscheduler.alert.utils.PropertyUtils.getString(AlertServer.ALERT_PLUGIN_DIR, Constants.ALERT_PLUGIN_PATH).trim());
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
