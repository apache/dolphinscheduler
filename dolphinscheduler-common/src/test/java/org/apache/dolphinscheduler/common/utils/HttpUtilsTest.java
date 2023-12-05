package org.apache.dolphinscheduler.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class HttpUtilsTest {

    @Test
    void testGetRequest() {
        // test HTTP URL
//        String response = HttpUtils.get("http://www.bing.com/");
//        assertNotNull(response, "Response should not be null for a http URL");

        // test HTTPS URL
        String response = HttpUtils.get("https://poc.bzzt.net");
        assertNull(response, "Response should be null for a https URL");
    }

}
