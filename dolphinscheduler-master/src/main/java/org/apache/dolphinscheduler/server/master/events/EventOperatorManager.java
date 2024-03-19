package org.apache.dolphinscheduler.server.master.events;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * The event operator manager interface used to get {@link ITaskEventOperator}.
 */
@Slf4j
@Component
public class EventOperatorManager implements IEventOperatorManager<IEvent> {

    @Override
    public IEventOperator<IEvent> getEventOperator(IEvent event) {
        return null;
    }

}
