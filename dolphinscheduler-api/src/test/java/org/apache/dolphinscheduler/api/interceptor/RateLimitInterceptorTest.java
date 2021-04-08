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

package org.apache.dolphinscheduler.api.interceptor;

import org.apache.dolphinscheduler.api.ApiApplicationServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplicationServer.class)
public class RateLimitInterceptorTest {

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Test
    public void testPreHandleSuccess() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Assert.assertTrue(rateLimitInterceptor.preHandle(request, response, null));
    }

    @Test
    public void testPreHandleFalse() throws InterruptedException {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        RateLimitInterceptor rateLimitInterceptor = new RateLimitInterceptor(2);
        ExecutorService executors = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 10; i++) {
            executors.submit(() -> rateLimitInterceptor.preHandle(request, response, null));
        }
        Assert.assertFalse(rateLimitInterceptor.preHandle(request, response, null));
        Thread.sleep(1000);
        Assert.assertTrue(rateLimitInterceptor.preHandle(request, response, null));
    }
}