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

package org.apache.dolphinscheduler.server.master.dispatch.host.assign;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * round robin selector
 */
public class RoundRobinSelectorTest {

    @Test
    public void testSelectWithIllegalArgumentException() {
        RoundRobinSelector selector = new RoundRobinSelector();
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            selector.select(null);
        });
    }

    @Test
    public void testSelect1() {
        RoundRobinSelector selector = new RoundRobinSelector();
        List<HostWorker> hostOneList = Arrays.asList(
            new HostWorker("192.168.1.1", 80, 20, "kris"),
            new HostWorker("192.168.1.2", 80, 10, "kris"));

        List<HostWorker> hostTwoList = Arrays.asList(
            new HostWorker("192.168.1.1", 80, 20, "kris"),
            new HostWorker("192.168.1.2", 80, 10, "kris"),
            new HostWorker("192.168.1.3", 80, 10, "kris"));

        HostWorker result;
        result = selector.select(hostOneList);
        Assertions.assertEquals("192.168.1.1", result.getIp());

        result = selector.select(hostOneList);
        Assertions.assertEquals("192.168.1.2", result.getIp());

        result = selector.select(hostOneList);
        Assertions.assertEquals("192.168.1.1", result.getIp());

        result = selector.select(hostOneList);
        Assertions.assertEquals("192.168.1.1", result.getIp());

        result = selector.select(hostOneList);
        Assertions.assertEquals("192.168.1.2", result.getIp());

        // add new host
        result = selector.select(hostTwoList);
        Assertions.assertEquals("192.168.1.1", result.getIp());

        result = selector.select(hostTwoList);
        Assertions.assertEquals("192.168.1.3", result.getIp());

        result = selector.select(hostTwoList);
        Assertions.assertEquals("192.168.1.1", result.getIp());

        result = selector.select(hostTwoList);
        Assertions.assertEquals("192.168.1.2", result.getIp());
        result = selector.select(hostTwoList);
        Assertions.assertEquals("192.168.1.1", result.getIp());

        result = selector.select(hostTwoList);
        Assertions.assertEquals("192.168.1.3", result.getIp());

        // remove host3
        result = selector.select(hostOneList);
        Assertions.assertEquals("192.168.1.1", result.getIp());

        result = selector.select(hostOneList);
        Assertions.assertEquals("192.168.1.2", result.getIp());

        result = selector.select(hostOneList);
        Assertions.assertEquals("192.168.1.1", result.getIp());
    }

    @Test
    public void testWeightRoundRobinSelector() {
        RoundRobinSelector selector = new RoundRobinSelector();
        HostWorker result;
        result = selector.select(
            Arrays.asList(new HostWorker("192.168.1.1", 11, 20, "kris"), new HostWorker("192.168.1.2", 22, 80, "kris")));
        Assertions.assertEquals("192.168.1.2", result.getIp());
    }

}
