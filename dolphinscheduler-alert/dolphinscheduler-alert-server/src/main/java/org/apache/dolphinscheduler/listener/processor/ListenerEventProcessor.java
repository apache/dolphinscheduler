package org.apache.dolphinscheduler.listener.processor;

import org.apache.dolphinscheduler.listener.enums.ListenerEventType;
import org.apache.dolphinscheduler.listener.plugin.ListenerPlugin;
import org.apache.dolphinscheduler.dao.entity.ListenerEvent;
import org.apache.dolphinscheduler.spi.plugin.PrioritySPI;
import org.apache.dolphinscheduler.spi.plugin.SPIIdentify;

/**
 * @author wxn
 * @date 2023/7/29
 */
public interface ListenerEventProcessor extends PrioritySPI {

    ListenerEventType getListenerEventType();
    void process(ListenerPlugin listenerPlugin, ListenerEvent event);
    @Override
    default SPIIdentify getIdentify() {
        return SPIIdentify.builder().name(String.valueOf(getListenerEventType().getCode())).build();
    }
}
