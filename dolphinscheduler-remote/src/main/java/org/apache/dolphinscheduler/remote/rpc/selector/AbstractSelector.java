package org.apache.dolphinscheduler.remote.rpc.selector;

import org.apache.dolphinscheduler.common.utils.CollectionUtils;

import java.util.Collection;

/**
 * AbstractSelector
 */
public  abstract class AbstractSelector<T> implements Selector<T>{
    @Override
    public T select(Collection<T> source) {

        if (CollectionUtils.isEmpty(source)) {
            throw new IllegalArgumentException("Empty source.");
        }

        /**
         * if only one , return directly
         */
        if (source.size() == 1) {
            return (T)source.toArray()[0];
        }
        return doSelect(source);
    }

    protected abstract T  doSelect(Collection<T> source);

}