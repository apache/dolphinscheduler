package org.apache.dolphinscheduler.service.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ParamUtilsTest {

    @Test
    void testGetGlobalParamMap() {
        String globalParam = "[{\"prop\":\"startParam1\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"\"}]";
        Map<String, String> globalParamMap = ParamUtils.getGlobalParamMap(globalParam);
        Assertions.assertEquals(globalParamMap.size(), 1);
        Assertions.assertEquals(globalParamMap.get("startParam1"), "");

        Map<String, String> emptyParamMap = ParamUtils.getGlobalParamMap(null);
        Assertions.assertEquals(emptyParamMap.size(), 0);
    }
}
