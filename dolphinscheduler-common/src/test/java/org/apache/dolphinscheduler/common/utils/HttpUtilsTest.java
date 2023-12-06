/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
