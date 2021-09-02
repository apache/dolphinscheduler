package org.apache.dolphinscheduler.dao.datasource;

import org.apache.dolphinscheduler.common.enums.DbType;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Jdbc Driver Manager
 */
public class JdbcDriverManager {

    /**
     * meta plugin dir
     */
    public final String pluginPath = System.getProperty("dolphinscheduler.plugin.dir");
    /**
     * datasource jdbc dir
     */
    public final String jdbcDir = System.getProperty("dolphinscheduler.jdbc.dir");

    private final Map<String, SortedMap<String, String>> jdbcDrivers = new HashMap<>();

    private static JdbcDriverManager instance;

    private JdbcDriverManager() {
    }

    public static synchronized JdbcDriverManager getInstance() {
        if (instance == null) {
            instance = new JdbcDriverManager();
        }
        return instance;
    }

    /**
     * Storage of driver package
     */
    public void init() {
        if (jdbcDir != null) {
            File pluginRoot = new File(jdbcDir);
            File[] typeNames = pluginRoot.listFiles(File::isDirectory);
            if (typeNames != null) {
                for (File type : typeNames) {
                    String typeName = type.getName();
                    SortedMap<String, String> inner = jdbcDrivers.computeIfAbsent(typeName, k -> new TreeMap<>());
                    File[] jdbcFiles = type.listFiles(File::isFile);
                    if (jdbcFiles != null) {
                        Arrays.sort(jdbcFiles);
                        for (File jdbc : jdbcFiles) {
                            String jdbcName = jdbc.getName();
                            inner.put(jdbcName, jdbc.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    /**
     * DefaultDriver Plugin Path
     */
    public String getDefaultDriverPluginPath(String typeName) {
        init();
        SortedMap<String, String> drivers = jdbcDrivers.get(typeName);
        return drivers.get(drivers.firstKey());
    }

    public String getPluginPath(DbType type) {
        return String.format("%s/%s", this.pluginPath, type.getDescp());
    }

    public String getHadoopClientPath() {
        return System.getenv("HADOOP_CLIENT") == null ? String.format("%s/hadoop-client", System.getenv("DOLPHINSCHEDULER_HOME")) : System.getenv("HADOOP_CLIENT");
    }

}
