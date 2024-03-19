package org.apache.dolphinscheduler.server.master.events;

/**
 * The event operator manager interface used to get event operator.
 */
public interface IEventOperatorManager<E> {

    /**
     * Get the {@link IEventOperator} for the given event.
     *
     * @param event event
     * @return event operator for the given event
     */
    IEventOperator<E> getEventOperator(E event);

}
