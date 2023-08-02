package org.apache.dolphinscheduler.listener.processor;

import org.apache.dolphinscheduler.listener.enums.ListenerEventType;
import org.apache.dolphinscheduler.listener.event.DsListenerTaskUpdateEvent;
import org.apache.dolphinscheduler.listener.plugin.ListenerPlugin;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ListenerEvent;
import com.google.auto.service.AutoService;

/**
 * @author wxn
 * @date 2023/7/29
 */
@AutoService(ListenerEventProcessor.class)
public class TaskUpdateEventProcessor implements ListenerEventProcessor {

    @Override
    public ListenerEventType getListenerEventType() {
        return ListenerEventType.TASK_UPDATE;
    }

    @Override
    public void process(ListenerPlugin plugin, ListenerEvent event) {
        DsListenerTaskUpdateEvent dsListenerEvent =
                JSONUtils.parseObject(event.getContent(), DsListenerTaskUpdateEvent.class);
        dsListenerEvent.setListenerInstanceParams(event.getParams());
        plugin.onTaskUpdate(dsListenerEvent);
    }
}
