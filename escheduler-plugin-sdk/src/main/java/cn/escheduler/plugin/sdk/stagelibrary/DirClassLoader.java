package cn.escheduler.plugin.sdk.stagelibrary;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.stream.Collectors;

public class DirClassLoader
        extends URLClassLoader {
    private final Date startDate = new Date();
    private final String name;

    public DirClassLoader(File dir, ClassLoader parent) {
        super(new URL[0], parent);
        name = dir.getName();
        addDir(dir);
    }

    /**
     * getName() is needed, since SDC use this name as this stage library's name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    public void addJarFile(File jarfile) {
        try {
            this.addURL(jarfile.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addDir(File path) {
        if (!path.exists()) {
            return;
        }

        if (path.isDirectory()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    this.addDir(file);
                }
            }
        } else { //文件
            this.addJarFile(path);
        }
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> originResult = super.getResources(name);
        if (name.equals("Services.json")) {
            ArrayList<URL> elems = Collections.list(originResult);
            if (!elems.isEmpty()) {
                return Collections.enumeration(
                        elems.stream().filter(e -> !e.toString().contains("streamsets-datacollector-sdk-")).collect(Collectors.toList())
                );
            }
        }
        return originResult;
    }

    @Override
    public String toString() {
        return super.toString() + ",time:" + DateFormat.getTimeInstance().format(startDate);
    }
}