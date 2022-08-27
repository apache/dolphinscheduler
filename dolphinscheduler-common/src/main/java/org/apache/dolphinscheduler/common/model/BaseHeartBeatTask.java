package org.apache.dolphinscheduler.common.model;

import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;

@Slf4j
public abstract class BaseHeartBeatTask<T> extends BaseDaemonThread {

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
                T heartBeat = getHeartBeat();
                writeHeartBeat(heartBeat);
                Thread.sleep(heartBeatInterval);
            } catch (InterruptedException ex) {
                handleInterruptException(ex);
            } catch (Throwable ex) {
                log.error("{} task execute failed", threadName, ex);
                try {
                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                } catch (InterruptedException e) {
                    handleInterruptException(e);
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

    public abstract T getHeartBeat();

    public abstract void writeHeartBeat(T heartBeat);
}
