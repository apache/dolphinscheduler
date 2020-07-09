package org.apache.dolphinscheduler.alert.plugin;

import org.apache.dolphinscheduler.spi.alert.AlertChannel;
import org.apache.dolphinscheduler.spi.alert.AlertChannelFactory;
import org.apache.dolphinscheduler.spi.classloader.ThreadContextClassLoader;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.PluginParamsTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final String CONFIG_DIR = "alerts";

    private final Map<String, AlertChannelFactory> alertChannelFactoryMap = new ConcurrentHashMap<>();
    private final Map<String, AlertChannel> configuredAlertChannelMap = new ConcurrentHashMap<>();

    public void addAlertChannelFactory(AlertChannelFactory alertChannelFactory)
    {
        requireNonNull(alertChannelFactory, "alertChannelFactory is null");

        if (alertChannelFactoryMap.putIfAbsent(alertChannelFactory.getId(), alertChannelFactory) != null) {
            throw new IllegalArgumentException(format("Alert Plugin '{}' is already registered", alertChannelFactory.getId()));
        }

        try {
            loadConfiguredAlertChannel(alertChannelFactory.getId());
        } catch (Exception e) {
            throw new IllegalArgumentException(format("Alert Plugin '{}' is can not load , read config file failed.", alertChannelFactory.getId()));
        }
    }

    protected void loadConfiguredAlertChannel(String name)
    {
        requireNonNull(name, "name is null");

        AlertChannelFactory alertChannelFactory = alertChannelFactoryMap.get(name);
        checkState(alertChannelFactory != null, "Alert Plugin {} is not registered", name);

        List<PluginParams> params = alertChannelFactory.getParams();
        String nameCh = alertChannelFactory.getNameCh();
        String nameEn = alertChannelFactory.getNameEn();

        String paramsJson = PluginParamsTransfer.getParamsJson(params);

        //TODO: I think params, nameCh, nameEn should save in mysql .
        //TODO: Then the Web UI can get the configured Alert Plugin and disable there name , params.

        try (ThreadContextClassLoader ignored = new ThreadContextClassLoader(alertChannelFactory.getClass().getClassLoader())) {
            AlertChannel alertChannel = alertChannelFactory.create();
            this.configuredAlertChannelMap.put(name, alertChannel);
        }

        logger.info("-- Loaded Alert Plugin {} --", name);
    }

    public Map<String, AlertChannelFactory> getAlertChannelFactoryMap() {
        return alertChannelFactoryMap;
    }

    public Map<String, AlertChannel> getConfiguredAlertChannelMap() {
        return configuredAlertChannelMap;
    }
}
