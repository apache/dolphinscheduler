package org.apache.dolphinscheduler.listener.processor;

import com.google.auto.service.AutoService;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ListenerEvent;

import org.apache.dolphinscheduler.listener.enums.ListenerEventType;
import org.apache.dolphinscheduler.listener.event.DsListenerTaskAddedEvent;
import org.apache.dolphinscheduler.listener.plugin.ListenerPlugin;

/**
 * @author wxn
 * @date 2023/7/29
 */
@AutoService(ListenerEventProcessor.class)
public class TaskAddedEventProcessor implements ListenerEventProcessor {

    @Override
    public ListenerEventType getListenerEventType() {
        return ListenerEventType.TASK_ADDED;
    }

    @Override
    public void process(ListenerPlugin plugin, ListenerEvent event) {
        DsListenerTaskAddedEvent dsListenerEvent =
                JSONUtils.parseObject(event.getContent(), DsListenerTaskAddedEvent.class);
        dsListenerEvent.setListenerInstanceParams(event.getParams());
        plugin.onTaskAdded(dsListenerEvent);
    }
}
