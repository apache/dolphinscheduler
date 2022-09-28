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

import org.apache.dolphinscheduler.api.configuration.TrafficConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class RateLimitInterceptorTest {

    @Test
    public void testPreHandleWithoutControl() throws ExecutionException {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        RateLimitInterceptor rateLimitInterceptor = new RateLimitInterceptor(new TrafficConfiguration());
        Assert.assertTrue(rateLimitInterceptor.preHandle(request, response, null));
        Assert.assertTrue(rateLimitInterceptor.preHandle(request, response, null));
    }

    @Test
    public void testPreHandleWithTenantLevenControl() throws ExecutionException {
        TrafficConfiguration trafficConfiguration = new TrafficConfiguration();
        trafficConfiguration.setTenantSwitch(true);
        Map<String, Integer> map = new HashMap<>();
        map.put("tenant1", 2);
        map.put("tenant2", 2);
        trafficConfiguration.setCustomizeTenantQpsRate(map);
        trafficConfiguration.setDefaultTenantQpsRate(4);
        RateLimitInterceptor rateLimitInterceptor = new RateLimitInterceptor(trafficConfiguration);

        HttpServletRequest tenant1Request = Mockito.mock(HttpServletRequest.class);
        HttpServletRequest tenant2Request = Mockito.mock(HttpServletRequest.class);
        PowerMockito.when(tenant1Request.getHeader(Mockito.any())).thenReturn("tenant1");
        PowerMockito.when(tenant2Request.getHeader(Mockito.any())).thenReturn("tenant2");
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        for (int i = 0; i < 2; i++) {
            rateLimitInterceptor.preHandle(tenant1Request, response, null);
        }
        Assert.assertFalse(rateLimitInterceptor.preHandle(tenant1Request, response, null));
        Assert.assertTrue(rateLimitInterceptor.preHandle(tenant2Request, response, null));
    }

    @Test
    public void testPreHandleWithGlobalControl() throws ExecutionException {
        TrafficConfiguration trafficConfiguration = new TrafficConfiguration();
        trafficConfiguration.setTenantSwitch(true);
        trafficConfiguration.setGlobalSwitch(true);
        trafficConfiguration.setMaxGlobalQpsRate(3);

        RateLimitInterceptor rateLimitInterceptor = new RateLimitInterceptor(trafficConfiguration);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        for (int i = 0; i < 2; i++) {
            rateLimitInterceptor.preHandle(request, response, null);
        }
        Assert.assertFalse(rateLimitInterceptor.preHandle(request, response, null));
    }

}
