package org.apache.dolphinscheduler.plugin.task.jupyter;

import org.apache.dolphinscheduler.spi.utils.PropertyUtils;

// this is a wrapper class to decouple untestable static method

public class JupyterPropertyReader {

    protected String readProperty(final String key) {
        return PropertyUtils.getString(key);
    }
}
