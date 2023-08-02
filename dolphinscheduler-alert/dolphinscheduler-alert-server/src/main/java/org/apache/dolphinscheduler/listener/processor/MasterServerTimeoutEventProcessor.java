package org.apache.dolphinscheduler.listener.processor;

import org.apache.dolphinscheduler.listener.enums.ListenerEventType;
import org.apache.dolphinscheduler.listener.event.DsListenerMasterTimeoutEvent;
import org.apache.dolphinscheduler.listener.plugin.ListenerPlugin;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ListenerEvent;

import com.google.auto.service.AutoService;

/**
 * @author wxn
 * @date 2023/7/29
 */
@AutoService(ListenerEventProcessor.class)
public class MasterServerTimeoutEventProcessor implements ListenerEventProcessor {

    @Override
    public ListenerEventType getListenerEventType() {
        return ListenerEventType.MASTER_TIMEOUT;
    }

    @Override
    public void process(ListenerPlugin plugin, ListenerEvent event) {
        DsListenerMasterTimeoutEvent dsListenerEvent =
                JSONUtils.parseObject(event.getContent(), DsListenerMasterTimeoutEvent.class);
        dsListenerEvent.setListenerInstanceParams(event.getParams());
        plugin.onMasterTimeout(dsListenerEvent);
    }
}
