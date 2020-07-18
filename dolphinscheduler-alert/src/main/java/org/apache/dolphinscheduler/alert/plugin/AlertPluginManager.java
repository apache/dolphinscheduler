package org.apache.dolphinscheduler.alert.plugin;

import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.spi.DolphinSchedulerPlugin;
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
public class AlertPluginManager extends DolphinPluginManager {
    private static final Logger logger = LoggerFactory.getLogger(AlertPluginManager.class);

    private final Map<String, AlertChannelFactory> alertChannelFactoryMap = new ConcurrentHashMap<>();
    private final Map<String, AlertChannel> alertChannelMap = new ConcurrentHashMap<>();

    public void addAlertChannelFactory(AlertChannelFactory alertChannelFactory)
    {
        requireNonNull(alertChannelFactory, "alertChannelFactory is null");

        if (alertChannelFactoryMap.putIfAbsent(alertChannelFactory.getName(), alertChannelFactory) != null) {
            throw new IllegalArgumentException(format("Alert Plugin '{}' is already registered", alertChannelFactory.getName()));
        }

        try {
            loadConfiguredAlertChannel(alertChannelFactory.getName());
        } catch (Exception e) {
            throw new IllegalArgumentException(format("Alert Plugin '{}' is can not load , read config file failed.", alertChannelFactory.getName()));
        }
    }

    protected void loadConfiguredAlertChannel(String name)
    {
        requireNonNull(name, "name is null");

        AlertChannelFactory alertChannelFactory = alertChannelFactoryMap.get(name);
        checkState(alertChannelFactory != null, "Alert Plugin {} is not registered", name);

        try (ThreadContextClassLoader ignored = new ThreadContextClassLoader(alertChannelFactory.getClass().getClassLoader())) {
            AlertChannel alertChannel = alertChannelFactory.create();
            this.alertChannelMap.put(name, alertChannel);
        }

        logger.info("-- Loaded Alert Plugin {} --", name);
    }

    public Map<String, AlertChannelFactory> getAlertChannelFactoryMap() {
        return alertChannelFactoryMap;
    }

    public Map<String, AlertChannel> getAlertChannelMap() {
        return alertChannelMap;
    }

    @Override
    public void installPlugin(DolphinSchedulerPlugin dolphinSchedulerPlugin) {
        for (AlertChannelFactory alertChannelFactory : dolphinSchedulerPlugin.getAlertChannelFactorys()) {
            logger.info("Registering Alert Plugin '{}'", alertChannelFactory.getName());
            this.addAlertChannelFactory(alertChannelFactory);
            List<PluginParams> params = alertChannelFactory.getParams();
            String nameEn = alertChannelFactory.getName();
            String paramsJson = PluginParamsTransfer.getParamsJson(params);

            PluginDefine pluginDefine = new PluginDefine(nameEn, "alert", paramsJson);
            pluginDao.addOrUpdatePluginDefine(pluginDefine);
        }
    }
}
