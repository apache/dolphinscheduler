package org.apache.dolphinscheduler.server.master.events;

import java.util.concurrent.LinkedBlockingDeque;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventRepository implements IEventRepository<IEvent> {

    protected final LinkedBlockingDeque<IEvent> eventQueue;

    public EventRepository() {
        this.eventQueue = new LinkedBlockingDeque<>();
    }

    @Override
    public void storeEventToTail(IEvent event) {
        eventQueue.offerLast(event);
    }

    @Override
    public void storeEventToHead(IEvent event) {
        eventQueue.offerFirst(event);
    }

    @Override
    public IEvent poolEvent() {
        return eventQueue.poll();
    }

    @Override
    public int getEventSize() {
        return eventQueue.size();
    }

}
