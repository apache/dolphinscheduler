package org.apache.dolphinscheduler.common.zk.monitor;

/**
 * server monitor and auto restart server
 */
public interface MonitorServer {

    /**
     * monitor server and restart
     */
    void monitor(String masterPath,String workerPath,Integer port,String installPath);
}
