package org.apache.dolphinscheduler.server.master.events;

/**
 * The event repository interface used to store event.
 */
public interface IEventRepository<E> {

    void storeEventToTail(E event);

    void storeEventToHead(E event);

    E poolEvent();

    int getEventSize();

}
