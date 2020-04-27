package org.apache.dolphinscheduler.common.task.latch;

import java.util.List;

import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;

public class LatchParameters extends AbstractParameters {


    private int second;


    @Override
    public boolean checkParameters() {
        return false;
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return null;
    }
}
