package org.apache.dolphinscheduler.remote.rpc.directory;

import org.apache.dolphinscheduler.remote.rpc.filter.SelectorFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Directory
 */
public class Directory {


    private static final Logger logger = LoggerFactory.getLogger(Directory.class);


    private SelectorFilter selectorFilter = SelectorFilter.getInstance();

    public static Directory getInstance() {
        return Directory.DirectoryInner.INSTANCE;
    }

    private static class DirectoryInner {

        private static final Directory INSTANCE = new Directory();
    }

    private Directory() {
    }


    private ConcurrentHashMap<String, List<String>> directoryMap = new ConcurrentHashMap<>();

    public List<String> getDirectory(String serviceName) {
        return directoryMap.get(serviceName);
    }

    public boolean addServer(String serviceName, String servicePath) {
        synchronized (this) {
            if (directoryMap.containsKey(serviceName)) {
                directoryMap.get(serviceName).add(servicePath);
                return true;
            }
        }
        directoryMap.putIfAbsent(serviceName, new ArrayList<>(Collections.singletonList(servicePath)));
        return true;
    }

    public boolean removeServer(String serviceName, String servicePath) {

        return true;
    }

}
