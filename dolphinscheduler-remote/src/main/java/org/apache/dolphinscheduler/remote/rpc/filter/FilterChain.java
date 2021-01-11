package org.apache.dolphinscheduler.remote.rpc.filter;

import org.apache.dolphinscheduler.remote.rpc.Invoker;

import java.util.List;

/**
 * FilterChain
 */
public class FilterChain {


    private List<Filter> filters;

    private Invoker invoker;


    public FilterChain(List<Filter> filters, Invoker invoker) {
        this.filters = filters;
        this.invoker = invoker;
    }

    public FilterChain(Invoker invoker) {
        this(LoaderFilters.create().getFilters(), invoker);
    }

    public Invoker buildFilterChain() {
        // 最后一个
        Invoker last = invoker;

        for (int i = filters.size() - 1; i >= 0; i--) {
            last = new FilterWrapper(filters.get(i), last);
        }
        // 第一个
        return last;

    }
}
