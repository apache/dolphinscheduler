package org.apache.dolphinscheduler.listener.util;

import org.apache.dolphinscheduler.dao.entity.PluginJar;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * @author wxn
 * @date 2023/5/14
 */
@Slf4j
@Component
public class ClassLoaderUtil {

    private final ConcurrentHashMap<String, PluginJar> localCache = new ConcurrentHashMap<>();

    public URLClassLoader getClassLoader(String jarPath) throws Exception {
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        if (!method.isAccessible()) {
            method.setAccessible(true);
        }
        URLClassLoader classLoader = new URLClassLoader(new URL[]{}, getClass().getClassLoader());
        File jarFile = new File(jarPath);
        method.invoke(classLoader, jarFile.toURI().toURL());
        PluginJar jarEntity = PluginJar.builder()
                .jarPath(jarPath)
                .urlClassLoader(classLoader)
                .jarFile(new JarFile(jarFile))
                .build();
        localCache.put(jarPath, jarEntity);
        return classLoader;
    }

    public void removeJarFile(String jarPath) throws Exception {
        PluginJar jarEntity = localCache.get(jarPath);
        jarEntity.getUrlClassLoader().close();
        jarEntity.getJarFile().close();
        localCache.remove(jarEntity.getJarPath());
    }
}
