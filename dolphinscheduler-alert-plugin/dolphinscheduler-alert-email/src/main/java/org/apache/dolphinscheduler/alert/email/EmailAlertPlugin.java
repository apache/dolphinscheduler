package org.apache.dolphinscheduler.alert.email;

import com.google.common.collect.ImmutableList;
import org.apache.dolphinscheduler.spi.DolphinSchedulerPlugin;
import org.apache.dolphinscheduler.spi.alert.AlertChannelFactory;

/**
 * email alert plugin
 * @author gaojun
 */
public class EmailAlertPlugin implements DolphinSchedulerPlugin {
    @Override
    public Iterable<AlertChannelFactory> getAlertChannelFactorys() {
        return ImmutableList.of(new EmailAlertChannelFactory());
    }
}
