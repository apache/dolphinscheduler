package org.apache.dolphinscheduler.listener.processor;

import org.apache.dolphinscheduler.listener.enums.ListenerEventType;
import org.apache.dolphinscheduler.listener.event.DsListenerTaskStartEvent;
import org.apache.dolphinscheduler.listener.plugin.ListenerPlugin;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ListenerEvent;
import com.google.auto.service.AutoService;

/**
 * @author wxn
 * @date 2023/7/29
 */
@AutoService(ListenerEventProcessor.class)
public class TaskStartEventProcessor implements ListenerEventProcessor {

    @Override
    public ListenerEventType getListenerEventType() {
        return ListenerEventType.TASK_START;
    }

    @Override
    public void process(ListenerPlugin plugin, ListenerEvent event) {
        DsListenerTaskStartEvent dsListenerEvent =
                JSONUtils.parseObject(event.getContent(), DsListenerTaskStartEvent.class);
        dsListenerEvent.setListenerInstanceParams(event.getParams());
        plugin.onTaskStart(dsListenerEvent);
    }
}
