package org.apache.dolphinscheduler.server.master.events;

/**
 * The event dispatcher interface used to dispatch event.
 * Each event should be dispatched to the corresponding workflow event queue.
 */
public interface IEventDispatcher<E> {

    void start();

    void stop();

    void dispatchEvent(E event);

}
