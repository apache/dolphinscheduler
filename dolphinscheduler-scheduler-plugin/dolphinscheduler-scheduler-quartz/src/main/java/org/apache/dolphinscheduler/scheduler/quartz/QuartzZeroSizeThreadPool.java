package org.apache.dolphinscheduler.scheduler.quartz;

import org.quartz.simpl.ZeroSizeThreadPool;


public class QuartzZeroSizeThreadPool extends ZeroSizeThreadPool {

    /**
     * fix spring bug : add getter、setter method for threadCount field
     */
    public void setThreadCount(int count) {
        // do nothing
    }

    public int getThreadCount() {
        return -1;
    }
}
