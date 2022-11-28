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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.net.InetAddress;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class NetUtilsTest {

    @Test
    public void testGetAddr() {
        Assertions.assertEquals(NetUtils.getHost() + ":5678", NetUtils.getAddr(5678));
        Assertions.assertEquals("127.0.0.1:5678", NetUtils.getAddr("127.0.0.1", 5678));
        Assertions.assertEquals("localhost:1234", NetUtils.getAddr("localhost", 1234));
    }

    @Test
    public void testGetHostInKubernetesMode() {
        try (MockedStatic<KubernetesUtils> mockedKubernetesUtils = mockStatic(KubernetesUtils.class)) {
            mockedKubernetesUtils.when(() -> KubernetesUtils.isKubernetesMode()).thenReturn(true);

            InetAddress address = mock(InetAddress.class);
            when(address.getCanonicalHostName())
                    .thenReturn("dolphinscheduler-worker-0.dolphinscheduler-worker-headless.default.svc.cluster.local");
            when(address.getHostName()).thenReturn("dolphinscheduler-worker-0");
            Assertions.assertEquals("dolphinscheduler-worker-0.dolphinscheduler-worker-headless",
                    NetUtils.getHost(address));

            address = mock(InetAddress.class);
            when(address.getCanonicalHostName())
                    .thenReturn("busybox-1.default-subdomain.my-namespace.svc.cluster-domain.example");
            when(address.getHostName()).thenReturn("busybox-1");
            Assertions.assertEquals("busybox-1.default-subdomain", NetUtils.getHost(address));

            address = mock(InetAddress.class);
            when(address.getCanonicalHostName()).thenReturn("dolphinscheduler.cluster-domain.example");
            when(address.getHostName()).thenReturn("dolphinscheduler");
            Assertions.assertEquals("dolphinscheduler.cluster-domain.example", NetUtils.getHost(address));

            address = mock(InetAddress.class);
            when(address.getCanonicalHostName()).thenReturn("dolphinscheduler-worker-0");
            when(address.getHostName()).thenReturn("dolphinscheduler-worker-0");
            Assertions.assertEquals("dolphinscheduler-worker-0", NetUtils.getHost(address));
        }
    }

    @Test
    public void testGetHostInNonKubernetesMode() {
        InetAddress address = mock(InetAddress.class);
        when(address.getCanonicalHostName())
                .thenReturn("dolphinscheduler-worker-0.dolphinscheduler-worker-headless.default.svc.cluster.local");
        when(address.getHostName()).thenReturn("dolphinscheduler-worker-0");
        when(address.getHostAddress()).thenReturn("172.17.0.15");
        Assertions.assertEquals("172.17.0.15", NetUtils.getHost(address));
    }

    @Test
    public void testGetLocalHost() {
        Assertions.assertNotNull(NetUtils.getHost());
    }

    @Test
    public void testIsValidAddress() {
        Assertions.assertFalse(NetUtils.isValidV4Address(null));
        InetAddress address = mock(InetAddress.class);
        when(address.isLoopbackAddress()).thenReturn(true);
        Assertions.assertFalse(NetUtils.isValidV4Address(address));
        address = mock(InetAddress.class);
        when(address.getHostAddress()).thenReturn("localhost");
        Assertions.assertFalse(NetUtils.isValidV4Address(address));
        address = mock(InetAddress.class);
        when(address.getHostAddress()).thenReturn("0.0.0.0");
        when(address.isAnyLocalAddress()).thenReturn(true);
        Assertions.assertFalse(NetUtils.isValidV4Address(address));
        address = mock(InetAddress.class);
        when(address.getHostAddress()).thenReturn("127.0.0.1");
        when(address.isLoopbackAddress()).thenReturn(true);
        Assertions.assertFalse(NetUtils.isValidV4Address(address));
        address = mock(InetAddress.class);
        when(address.getHostAddress()).thenReturn("1.2.3.4");
        Assertions.assertTrue(NetUtils.isValidV4Address(address));
        address = mock(InetAddress.class);
        when(address.getHostAddress()).thenReturn("1.2.3.4:80");
        Assertions.assertFalse(NetUtils.isValidV4Address(address));
        address = mock(InetAddress.class);
        when(address.getHostAddress()).thenReturn("256.0.0.1");
        Assertions.assertFalse(NetUtils.isValidV4Address(address));
        address = mock(InetAddress.class);
        when(address.getHostAddress()).thenReturn("127.0.0.0.1");
        Assertions.assertFalse(NetUtils.isValidV4Address(address));
        address = mock(InetAddress.class);
        when(address.getHostAddress()).thenReturn("-1.2.3.4");
        Assertions.assertFalse(NetUtils.isValidV4Address(address));
    }

    @Test
    public void giveIpAddress_thenCheckIsValidIPv4Address_thenCheck_expectNormal() {
        Assertions.assertFalse(NetUtils.isValidIPv4Address(""));

        String ipAddress = "127.0.0.1";
        Assertions.assertTrue(NetUtils.isValidIPv4Address(ipAddress));

        String wrongIpAddress = "this is a ip";
        Assertions.assertFalse(NetUtils.isValidIPv4Address(wrongIpAddress));
    }

    @Test
    public void givePort_thenCheckIsValidPort_expectNormal() {
        Assertions.assertFalse(NetUtils.isValidPort(""));
        Assertions.assertFalse(NetUtils.isValidPort("0"));
        Assertions.assertFalse(NetUtils.isValidPort("this is a port"));
        Assertions.assertFalse(NetUtils.isValidPort("110000"));
        Assertions.assertTrue(NetUtils.isValidPort("12345"));
    }

    @Test
    public void giveIpAndPort_thenIsLegalAddress_expectNormal() {
        Assertions.assertFalse(NetUtils.isLegalAddress(""));
        Assertions.assertFalse(NetUtils.isLegalAddress("127.0.0.1"));
        Assertions.assertFalse(NetUtils.isLegalAddress("default"));
        Assertions.assertFalse(NetUtils.isLegalAddress("worker"));
        Assertions.assertTrue(NetUtils.isLegalAddress("127.0.0.1:12345"));
        Assertions.assertTrue(NetUtils.isLegalAddress("[2001:0db8:85a3:0000:0000:8a2e:0370:7334]:12345"));
    }

    @Test
    public void giveIPv6_thenIsLegalAddress_expectNormal() {
        Assertions.assertFalse(NetUtils.isValidIPv6Address("127.0.0.1"));
        Assertions.assertTrue(NetUtils.isValidIPv6Address("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
    }

}
