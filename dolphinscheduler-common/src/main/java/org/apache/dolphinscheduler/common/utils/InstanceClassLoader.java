package org.apache.dolphinscheduler.common.utils;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

public class InstanceClassLoader extends URLClassLoader {

    private final Set<String> driverLocations = new HashSet<>();

    static {
        //Parallel loading
        ClassLoader.registerAsParallelCapable();
    }

    public InstanceClassLoader(Set<String> driverLocations, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.driverLocations.addAll(driverLocations);
    }

    public Set<String> location() {
        return this.driverLocations;
    }

    @Override
    public String toString() {
        return "InstanceClassLoader{driverLocation=" + driverLocations + "}";
    }

}
