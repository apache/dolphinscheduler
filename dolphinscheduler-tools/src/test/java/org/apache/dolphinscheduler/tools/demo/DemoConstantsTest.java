package org.apache.dolphinscheduler.tools.demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;

class DemoConstantsTest {

    @Test
    void demoConstants() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            ReflectionUtils.newInstance(DemoConstants.class);
        });
    }
}
