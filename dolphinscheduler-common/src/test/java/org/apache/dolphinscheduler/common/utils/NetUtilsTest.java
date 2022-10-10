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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.net.InetAddress;

import org.junit.Test;
import org.mockito.MockedStatic;

public class NetUtilsTest {

    @Test
    public void testGetAddr() {
        assertEquals(NetUtils.getHost() + ":5678", NetUtils.getAddr(5678));
        assertEquals("127.0.0.1:5678", NetUtils.getAddr("127.0.0.1", 5678));
        assertEquals("localhost:1234", NetUtils.getAddr("localhost", 1234));
    }

    @Test
    public void testGetHostInKubernetesMode() {
        try (MockedStatic<KubernetesUtils> mockedKubernetesUtils = mockStatic(KubernetesUtils.class)) {
            mockedKubernetesUtils.when(() -> KubernetesUtils.isKubernetesMode()).thenReturn(true);

            InetAddress address = mock(InetAddress.class);
            when(address.getCanonicalHostName())
                    .thenReturn("dolphinscheduler-worker-0.dolphinscheduler-worker-headless.default.svc.cluster.local");
            when(address.getHostName()).thenReturn("dolphinscheduler-worker-0");
            assertEquals("dolphinscheduler-worker-0.dolphinscheduler-worker-headless", NetUtils.getHost(address));

            address = mock(InetAddress.class);
            when(address.getCanonicalHostName())
                    .thenReturn("busybox-1.default-subdomain.my-namespace.svc.cluster-domain.example");
            when(address.getHostName()).thenReturn("busybox-1");
            assertEquals("busybox-1.default-subdomain", NetUtils.getHost(address));

            address = mock(InetAddress.class);
            when(address.getCanonicalHostName()).thenReturn("dolphinscheduler.cluster-domain.example");
            when(address.getHostName()).thenReturn("dolphinscheduler");
            assertEquals("dolphinscheduler.cluster-domain.example", NetUtils.getHost(address));

            address = mock(InetAddress.class);
            when(address.getCanonicalHostName()).thenReturn("dolphinscheduler-worker-0");
            when(address.getHostName()).thenReturn("dolphinscheduler-worker-0");
            assertEquals("dolphinscheduler-worker-0", NetUtils.getHost(address));
        }
    }

    @Test
    public void testGetHostInNonKubernetesMode() {
        InetAddress address = mock(InetAddress.class);
        when(address.getCanonicalHostName())
                .thenReturn("dolphinscheduler-worker-0.dolphinscheduler-worker-headless.default.svc.cluster.local");
        when(address.getHostName()).thenReturn("dolphinscheduler-worker-0");
        when(address.getHostAddress()).thenReturn("172.17.0.15");
        assertEquals("172.17.0.15", NetUtils.getHost(address));
    }

    @Test
    public void testGetLocalHost() {
        assertNotNull(NetUtils.getHost());
    }

    @Test
    public void testIsValidAddress() {
        assertFalse(NetUtils.isValidV4Address(null));
        InetAddress address = mock(InetAddress.class);
        when(address.isLoopbackAddress()).thenReturn(true);
        assertFalse(NetUtils.isValidV4Address(address));
        address = mock(InetAddress.class);
        when(address.getHostAddress()).thenReturn("localhost");
        assertFalse(NetUtils.isValidV4Address(address));
        address = mock(InetAddress.class);
        when(address.getHostAddress()).thenReturn("0.0.0.0");
        when(address.isAnyLocalAddress()).thenReturn(true);
        assertFalse(NetUtils.isValidV4Address(address));
        address = mock(InetAddress.class);
        when(address.getHostAddress()).thenReturn("127.0.0.1");
        when(address.isLoopbackAddress()).thenReturn(true);
        assertFalse(NetUtils.isValidV4Address(address));
        address = mock(InetAddress.class);
        when(address.getHostAddress()).thenReturn("1.2.3.4");
        assertTrue(NetUtils.isValidV4Address(address));
        address = mock(InetAddress.class);
        when(address.getHostAddress()).thenReturn("1.2.3.4:80");
        assertFalse(NetUtils.isValidV4Address(address));
        address = mock(InetAddress.class);
        when(address.getHostAddress()).thenReturn("256.0.0.1");
        assertFalse(NetUtils.isValidV4Address(address));
        address = mock(InetAddress.class);
        when(address.getHostAddress()).thenReturn("127.0.0.0.1");
        assertFalse(NetUtils.isValidV4Address(address));
        address = mock(InetAddress.class);
        when(address.getHostAddress()).thenReturn("-1.2.3.4");
        assertFalse(NetUtils.isValidV4Address(address));
    }

}
