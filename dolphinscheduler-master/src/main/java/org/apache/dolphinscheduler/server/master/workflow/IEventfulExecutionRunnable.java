package org.apache.dolphinscheduler.server.master.workflow;

import org.apache.dolphinscheduler.server.master.events.IEvent;
import org.apache.dolphinscheduler.server.master.events.IEventRepository;

public interface IEventfulExecutionRunnable {

    default IEventRepository<IEvent> getEventRepository() {
        return null;
    }
}
