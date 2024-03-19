package org.apache.dolphinscheduler.server.master.events;

import java.util.concurrent.CompletableFuture;

/**
 * The event firer interface used to fire event.
 *
 * @param <E> event type
 */
public interface IEventFirer<E> {

    /**
     * Fire all active events in the event repository
     *
     * @return the count of fired success events
     */
    CompletableFuture<Integer> fireActiveEvents(IEventRepository<E> eventRepository);

}
