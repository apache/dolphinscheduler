package org.apache.dolphinscheduler.remote.rpc.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * LoaderFilters
 */
public class LoaderFilters {


    private List<Filter> filterList = new ArrayList<>();

    private LoaderFilters() {
    }

    public static LoaderFilters create() {

        return new LoaderFilters();
    }

    public List<Filter> getFilters() {
        filterList.add(SelectorFilter.getInstance());
        return filterList;
    }
}
