package org.apache.dolphinscheduler.common;

import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Constants Test
 */
public class ConstantsTest {

    /**
     * Test PID via env
     */
    @Test
    public void testPID() {
        if (OSUtils.isWindows()) {
            Assert.assertEquals(Constants.PID, "handle");
        } else {
            Assert.assertEquals(Constants.PID, "pid");
        }
    }

}
