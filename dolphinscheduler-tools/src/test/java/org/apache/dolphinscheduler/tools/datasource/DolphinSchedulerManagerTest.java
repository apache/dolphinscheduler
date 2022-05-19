package org.apache.dolphinscheduler.tools.datasource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

public class DolphinSchedulerManagerTest {
    private DolphinSchedulerManager dolphinSchedulerManager;

    @Before
    public void before() throws Exception {
        dolphinSchedulerManager = PowerMockito.mock(DolphinSchedulerManager.class);
        PowerMockito.whenNew(DolphinSchedulerManager.class).withAnyArguments().thenReturn(dolphinSchedulerManager);
    }

    @Test
    public void testSchemaIsInitializedFalse() {
        Assert.assertFalse(dolphinSchedulerManager.schemaIsInitialized());
    }
}
