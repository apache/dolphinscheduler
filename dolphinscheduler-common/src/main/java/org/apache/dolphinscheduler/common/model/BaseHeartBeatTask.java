package org.apache.dolphinscheduler.common.model;

import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

@Slf4j
public abstract class BaseHeartBeatTask<T extends HeartBeat> extends BaseDaemonThread {

    private final String threadName;
    private final long heartBeatInterval;

    protected boolean runningFlag;

    public BaseHeartBeatTask(String threadName, long heartBeatInterval) {
        super(threadName);
        this.threadName = threadName;
        this.heartBeatInterval = heartBeatInterval;
        this.runningFlag = true;
    }

    @Override
    public synchronized void start() {
        log.info("Starting {}", threadName);
        super.start();
        log.info("Started {}, heartBeatInterval: {}", threadName, heartBeatInterval);
    }

    @Override
    public void run() {
        while (runningFlag) {
            try {
                if (ServerLifeCycleManager.isRunning()) {
                    T heartBeat = getHeartBeat();
                    writeHeartBeat(heartBeat);
                } else {
                    log.info("The current server status is {}, will not write heart beat",
                            ServerLifeCycleManager.getServerStatus());
                }
            } catch (Exception ex) {
                log.error("{} task execute failed", threadName, ex);
            } finally {
                try {
                    Thread.sleep(heartBeatInterval);
                } catch (InterruptedException ex) {
                    handleInterruptException(ex);
                }
            }
        }
    }

    public void shutdown() {
        log.warn("{} task finished", threadName);
        runningFlag = false;
    }

    private void handleInterruptException(InterruptedException ex) {
        shutdown();
        log.warn("{} has been interrupted, will stop this thread", threadName, ex);
        Thread.currentThread().interrupt();
    }

    public String getHeartBeatJsonString() {
        T heartBeat = getHeartBeat();
        return JSONUtils.toJsonString(heartBeat);
    }

    public abstract T getHeartBeat();

    public abstract void writeHeartBeat(T heartBeat);
}
