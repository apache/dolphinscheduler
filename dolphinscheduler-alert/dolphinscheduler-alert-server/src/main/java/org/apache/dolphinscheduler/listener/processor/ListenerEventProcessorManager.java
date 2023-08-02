package org.apache.dolphinscheduler.listener.processor;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.listener.enums.ListenerEventType;
import org.apache.dolphinscheduler.spi.plugin.PrioritySPIFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;

/**
 * @author wxn
 * @date 2023/7/29
 */
@Slf4j
public class ListenerEventProcessorManager {
    private final Map<Integer, ListenerEventProcessor> listenerEventProcessorMap = new ConcurrentHashMap<>();

    private ListenerEventProcessorManager(){
        installListenerEventProcessor();
    }

    private static class ListenerEventProcessorManagerHolder{
        private static final ListenerEventProcessorManager INSTANCE = new ListenerEventProcessorManager();
    }

    public static ListenerEventProcessorManager getInstance(){
        return ListenerEventProcessorManagerHolder.INSTANCE;
    }

    public ListenerEventProcessor getListenerEventProcessor(@NonNull ListenerEventType eventType) {
        return listenerEventProcessorMap.get(eventType.getCode());
    }

    public Map<Integer, ListenerEventProcessor> getListenerEventProcessorMap() {
        return listenerEventProcessorMap;
    }

    private void installListenerEventProcessor() {

        PrioritySPIFactory<ListenerEventProcessor> prioritySPIFactory =
                new PrioritySPIFactory<>(ListenerEventProcessor.class);
        for (Map.Entry<String, ListenerEventProcessor> entry : prioritySPIFactory.getSPIMap().entrySet()) {
            final ListenerEventProcessor processor = entry.getValue();
            final int code = processor.getListenerEventType().getCode();
            if (listenerEventProcessorMap.containsKey(code)) {
                throw new IllegalStateException(format("Duplicate listener event processor '%d'", code));
            }
            listenerEventProcessorMap.put(code, processor);
        }
    }




}
