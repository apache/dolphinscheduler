package org.apache.dolphinscheduler.dao.entity;

import java.io.Serializable;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

import lombok.Builder;
import lombok.Data;

/**
 * @author wxn
 * @date 2023/7/11
 */
@Data
@Builder
public class PluginJar implements Serializable {

    private String jarPath;
    private URLClassLoader urlClassLoader;
    private JarFile jarFile;
}
