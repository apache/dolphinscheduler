package org.apache.dolphinscheduler.common.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Plugin Class Loader
 */
public class PluginClassLoader extends URLClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(PluginClassLoader.class);

    private static final String JAVA_PACKAGE_PREFIX = "java.";
    private static final String JAVAX_PACKAGE_PREFIX = "javax.";

    private final String[] whitePrefixes;

    private final String[] excludePrefixes;

    public PluginClassLoader(URL[] urls, ClassLoader parent, String[] whitePrefix, String[] excludePreifx) {
        super(urls, parent);
        this.whitePrefixes = whitePrefix;
        this.excludePrefixes = excludePreifx;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        logger.trace("Received request to load class '{}'", name);
        synchronized (getClassLoadingLock(name)) {
            if (name.startsWith(JAVA_PACKAGE_PREFIX) || name.startsWith(JAVAX_PACKAGE_PREFIX)) {
                return findSystemClass(name);
            }

            boolean isWhitePrefixes = fromWhitePrefix(name);
            boolean isExcludePrefixed = fromExcludePrefix(name);

            // if the class is part of the plugin engine use parent class loader
            if (!isWhitePrefixes && isExcludePrefixed) {
                return getParent().loadClass(name);
            }

            // check whether it's already been loaded
            Class<?> loadedClass = findLoadedClass(name);
            if (loadedClass != null) {
                logger.debug("Found loaded class '{}'", name);
                return loadedClass;
            }

            // nope, try to load locally
            try {
                loadedClass = findClass(name);
                logger.debug("Found class '{}' in plugin classpath", name);
                return loadedClass;
            } catch (ClassNotFoundException e) {
                // try next step
            }

            // use the standard ClassLoader (which follows normal parent delegation)
            return super.loadClass(name);
        }
    }

    private boolean fromWhitePrefix(String name) {
        for (String whitePrefix : this.whitePrefixes) {
            if (name.startsWith(whitePrefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean fromExcludePrefix(String name) {
        for (String excludePrefix : this.excludePrefixes) {
            if (name.startsWith(excludePrefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> allRes = new LinkedList<>();

        Enumeration<URL> thisRes = findResources(name);
        if (thisRes != null) {
            while (thisRes.hasMoreElements()) {
                allRes.add(thisRes.nextElement());
            }
        }

        Enumeration<URL> parentRes = super.findResources(name);
        if (parentRes != null) {
            while (parentRes.hasMoreElements()) {
                allRes.add(parentRes.nextElement());
            }
        }

        return new Enumeration<URL>() {
            Iterator<URL> it = allRes.iterator();

            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            @Override
            public URL nextElement() {
                return it.next();
            }
        };
    }

    @Override
    public URL getResource(String name) {
        URL res = null;

        if (res == null) {
            res = findResource(name);
        }
        if (res == null) {
            res = super.getResource(name);
        }
        return res;
    }
}
