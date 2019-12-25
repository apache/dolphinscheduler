package org.apache.dolphinscheduler.server.monitor;

/**
 * server monitor and auto restart server
 */
public interface Monitor {

    /**
     * monitor server and restart
     */
    void monitor(String masterPath, String workerPath, Integer port, String installPath);
}
