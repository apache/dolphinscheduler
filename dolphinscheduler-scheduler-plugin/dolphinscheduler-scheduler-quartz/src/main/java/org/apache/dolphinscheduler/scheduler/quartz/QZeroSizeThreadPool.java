package org.apache.dolphinscheduler.scheduler.quartz;

import org.quartz.simpl.ZeroSizeThreadPool;

/**
 * fix spring bug : add getter、setter method for threadCount field
 */
public class QZeroSizeThreadPool extends ZeroSizeThreadPool {

    public void setThreadCount(int count) {
    }

    public int getThreadCount() {
        return -1;
    }
}
