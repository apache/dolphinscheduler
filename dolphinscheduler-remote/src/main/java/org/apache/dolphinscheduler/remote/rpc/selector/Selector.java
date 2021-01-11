package org.apache.dolphinscheduler.remote.rpc.selector;

import java.util.Collection;

/**
 * Selector
 */
public interface Selector<T> {

    /**
     * select
     * @param source source
     * @return T
     */
    T select(Collection<T> source);
}
