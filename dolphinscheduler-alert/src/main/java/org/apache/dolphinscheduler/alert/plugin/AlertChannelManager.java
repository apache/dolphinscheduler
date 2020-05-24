package org.apache.dolphinscheduler.alert.plugin;

import com.google.common.collect.ImmutableMap;
import org.apache.dolphinscheduler.common.utils.PluginPropertiesUtil;
import org.apache.dolphinscheduler.spi.alert.AlertChannel;
import org.apache.dolphinscheduler.spi.alert.AlertChannelFactory;
import org.apache.dolphinscheduler.spi.classloader.ThreadContextClassLoader;
import org.apache.dolphinscheduler.spi.params.AbsPluginParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * load the configured alert plugin and manager them
 */
public class AlertChannelManager {
    private static final Logger logger = LoggerFactory.getLogger(AlertChannelManager.class);

    private static final File CONFIG_DIR = new File("alert");

    private final Map<String, AlertChannelFactory> alertChannelFactoryMap = new ConcurrentHashMap<>();
    private final Map<String, AlertChannel> configuredAlertChannelMap = new ConcurrentHashMap<>();

    public void addAlertChannelFactory(AlertChannelFactory alertChannelFactory)
    {
        requireNonNull(alertChannelFactory, "alertChannelFactory is null");

        File configFile = getPluginConfigFile(alertChannelFactory.getId());
        if (configFile == null) {
            logger.info("Alert Plugin: %s not config , skip.", alertChannelFactory.getId());
            return;
        }

        if (alertChannelFactoryMap.putIfAbsent(alertChannelFactory.getId(), alertChannelFactory) != null) {
            throw new IllegalArgumentException(format("Alert Plugin '%s' is already registered", alertChannelFactory.getId()));
        }

        try {
            loadConfiguredAlertChannel(alertChannelFactory.getId(), configFile);
        } catch (Exception e) {
            throw new IllegalArgumentException(format("Alert Plugin '%s' is can not load , read config file failed.", alertChannelFactory.getId()));
        }
    }

    private File getPluginConfigFile(String pluginId) {
        File configFile = CONFIG_DIR.getAbsoluteFile();
        if (!configFile.exists() || !configFile.isDirectory()) {
            logger.warn(CONFIG_DIR + " not exists or is not a directory. can not load alert channel plugin!");
            return null;
        }

        for (File file : configFile.listFiles()) {
            if (file.getName().equals(pluginId + ".properties")) {
                return file;
            }
        }

        return null;
    }

    public void loadConfiguredAlertChannel(String pluginId, File configFile)
            throws Exception
    {
        logger.info("-- Loading Alert Plugin: %s from config file %s .", pluginId, configFile.getPath());
        Map<String, String> properties = new HashMap<>(PluginPropertiesUtil.loadProperties(configFile));
        setConfiguredAlertChannel(pluginId, properties);
    }

    protected void setConfiguredAlertChannel(String name, Map<String, String> properties)
    {
        requireNonNull(name, "name is null");
        requireNonNull(properties, "properties is null");

        AlertChannelFactory alertChannelFactory = alertChannelFactoryMap.get(name);
        checkState(alertChannelFactory != null, "Alert Plugin '%s' is not registered", name);

        List<AbsPluginParams> params = alertChannelFactory.getParams();
        String nameCh = alertChannelFactory.getNameCh();
        String nameEn = alertChannelFactory.getNameEn();

        //TODO I think params, nameCh, nameEn should save in mysql .
        //TODO Then the Web UI can get the configured Alert Plugin and disable there name , params.

        try (ThreadContextClassLoader ignored = new ThreadContextClassLoader(alertChannelFactory.getClass().getClassLoader())) {
            AlertChannel alertChannel = alertChannelFactory.create(ImmutableMap.copyOf(properties));
            this.configuredAlertChannelMap.put(name, alertChannel);
        }

        logger.info("-- Loaded Alert Plugin %s --", name);
    }

    public Map<String, AlertChannelFactory> getAlertChannelFactoryMap() {
        return alertChannelFactoryMap;
    }

    public Map<String, AlertChannel> getConfiguredAlertChannelMap() {
        return configuredAlertChannelMap;
    }
}
