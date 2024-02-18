package org.apache.dolphinscheduler.server.master.events;

/**
 * The event operator interface used to handle event.
 */
public interface IEventOperator<E> {

    /**
     * Handle the given event
     *
     * @param event event
     */
    void handleEvent(E event);

}
