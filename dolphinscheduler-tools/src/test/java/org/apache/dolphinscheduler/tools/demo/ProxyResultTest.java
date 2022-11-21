package org.apache.dolphinscheduler.tools.demo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProxyResultTest {

    @Test
    void success() {
        ProxyResult<String> result = ProxyResult.success("String");
        Assertions.assertNotNull(result);
    }

    @Test
    void testSuccess() {
        ProxyResult success = ProxyResult.success();
        Assertions.assertNotNull(success);
    }

    @Test
    void isSuccess() {
        ProxyResult success = ProxyResult.success();
        assertTrue(success.isSuccess());
    }

    @Test
    void isFailed() {
        ProxyResult success = ProxyResult.success();
        Assertions.assertEquals(false, success.isFailed());
    }

}
