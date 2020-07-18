package org.apache.dolphinscheduler.alert.plugin;

import org.apache.dolphinscheduler.dao.DaoFactory;
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.spi.DolphinSchedulerPlugin;

public abstract class DolphinPluginManager {

    protected PluginDao pluginDao = DaoFactory.getDaoInstance(PluginDao.class);

    public abstract void installPlugin(DolphinSchedulerPlugin dolphinSchedulerPlugin);
}
