package org.apache.dolphinscheduler.common.task.latch;

import java.util.List;

import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;

public class LatchParameters extends AbstractParameters {

    private long milliseconds;


    @Override
    public boolean checkParameters() {
        if(milliseconds < 0 ){
            return false ;
        }else {
            return  true ;
        }

    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return null;
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(long milliseconds) {
        this.milliseconds = milliseconds;
    }
}
