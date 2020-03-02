package org.apache.dolphinscheduler.common.task;

import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.flink.FlinkParameters;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class FlinkParametersTest {
    @Test
    public void getResourceFilesList() {
        FlinkParameters flinkParameters = new FlinkParameters();
        Assert.assertNotNull(flinkParameters.getResourceFilesList());
        Assert.assertTrue(flinkParameters.getResourceFilesList().isEmpty());
        
        flinkParameters.setResourceList(Collections.singletonList(new ResourceInfo()));
        Assert.assertNotNull(flinkParameters.getResourceFilesList());
        Assert.assertEquals(1, flinkParameters.getResourceFilesList().size());
    }
}
