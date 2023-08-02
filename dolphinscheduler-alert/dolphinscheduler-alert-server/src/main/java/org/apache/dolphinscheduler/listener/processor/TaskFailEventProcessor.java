package org.apache.dolphinscheduler.listener.processor;

import org.apache.dolphinscheduler.listener.enums.ListenerEventType;
import org.apache.dolphinscheduler.listener.event.DsListenerTaskFailEvent;
import org.apache.dolphinscheduler.listener.plugin.ListenerPlugin;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ListenerEvent;
import com.google.auto.service.AutoService;

/**
 * @author wxn
 * @date 2023/7/29
 */
@AutoService(ListenerEventProcessor.class)
public class TaskFailEventProcessor implements ListenerEventProcessor {

    @Override
    public ListenerEventType getListenerEventType() {
        return ListenerEventType.TASK_FAIL;
    }

    @Override
    public void process(ListenerPlugin plugin, ListenerEvent event) {
        DsListenerTaskFailEvent dsListenerEvent =
                JSONUtils.parseObject(event.getContent(), DsListenerTaskFailEvent.class);
        dsListenerEvent.setListenerInstanceParams(event.getParams());
        plugin.onTaskFail(dsListenerEvent);
    }
}
