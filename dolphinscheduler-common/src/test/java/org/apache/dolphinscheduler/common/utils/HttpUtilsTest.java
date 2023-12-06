package org.apache.dolphinscheduler.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class HttpUtilsTest {

    @Test
    void testGetRequest() {
        // test HTTP URL
        String response1 = HttpUtils.get("http://www.bing.com/");
        assertNotNull(response1, "Response should not be null for a http URL");

        // test invalid certification HTTPS URL
        String response2 = HttpUtils.get("https://poc.bzzt.net");
        assertNull(response2, "Response should be null for ainvalid certification https URL and throw exception in console");


        // test valid certification HTTPS URL
        String response3 = HttpUtils.get("https://www.google.com/");
        assertNotNull(response3, "Response should not be null for a valid certification https URL");


    }

}
