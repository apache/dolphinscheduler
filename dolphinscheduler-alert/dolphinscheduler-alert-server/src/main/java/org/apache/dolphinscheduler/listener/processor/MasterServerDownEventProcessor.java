package org.apache.dolphinscheduler.listener.processor;

import com.google.auto.service.AutoService;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ListenerEvent;
import org.apache.dolphinscheduler.listener.enums.ListenerEventType;
import org.apache.dolphinscheduler.listener.event.DsListenerMasterDownEvent;
import org.apache.dolphinscheduler.listener.plugin.ListenerPlugin;

/**
 * @author wxn
 * @date 2023/7/29
 */
@AutoService(ListenerEventProcessor.class)
public class MasterServerDownEventProcessor implements ListenerEventProcessor {

    @Override
    public ListenerEventType getListenerEventType() {
        return ListenerEventType.MASTER_DOWN;
    }

    @Override
    public void process(ListenerPlugin plugin, ListenerEvent event) {
        DsListenerMasterDownEvent dsListenerEvent =
                JSONUtils.parseObject(event.getContent(), DsListenerMasterDownEvent.class);
        dsListenerEvent.setListenerInstanceParams(event.getParams());
        plugin.onMasterDown(dsListenerEvent);
    }
}
