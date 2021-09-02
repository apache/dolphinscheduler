package org.apache.dolphinscheduler.common.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassLoaderUtils {

    static final Logger LOGGER = LoggerFactory.getLogger(ClassLoaderUtils.class);

    public static ClassLoader getCustomClassLoader(Set<String> modulePaths, ClassLoader parentClassLoader, FilenameFilter filenameFilter) throws MalformedURLException {
        URL[] classpaths = getURLsForClasspath(modulePaths, filenameFilter);
        return createModuleClassLoader(modulePaths, classpaths, parentClassLoader);
    }

    public static URL[] getURLsForClasspath(Set<String> modulePaths, FilenameFilter filenameFilter) throws MalformedURLException {
        Set<String> modules = new LinkedHashSet<>();
        if (modulePaths != null) {
            modulePaths.stream()
                    .flatMap(path -> Arrays.stream(path.split(",")))
                    .filter(StringUtils::isNotBlank)
                    .map(String::trim)
                    .forEach(modules::add);
        }
        return toURLs(modules, filenameFilter);
    }

    protected static URL[] toURLs(Set<String> modulePaths, FilenameFilter filenameFilter) throws MalformedURLException {
        List<URL> additionalClasspath = new LinkedList<>();
        if (modulePaths != null) {
            for (String modulePathString : modulePaths) {
                File modulePath = new File(modulePathString);
                if (modulePath.exists()) {
                    additionalClasspath.add(modulePath.toURI().toURL());
                    if (modulePath.isDirectory()) {
                        File[] files = modulePath.listFiles(filenameFilter);
                        if (files != null) {
                            for (File classpathResource : files) {
                                if (classpathResource.isDirectory()) {
                                    LOGGER.warn("Recursive directories are not supported, skipping " + classpathResource.getAbsolutePath());
                                } else {
                                    additionalClasspath.add(classpathResource.toURI().toURL());
                                }
                            }
                        }
                    }
                }
            }
        }
        return additionalClasspath.toArray(new URL[additionalClasspath.size()]);
    }

    protected static ClassLoader createModuleClassLoader(Set<String> modulePaths, URL[] modules, ClassLoader parentClassLoader) {
        return new InstanceClassLoader(modulePaths, modules, parentClassLoader);
    }

}
