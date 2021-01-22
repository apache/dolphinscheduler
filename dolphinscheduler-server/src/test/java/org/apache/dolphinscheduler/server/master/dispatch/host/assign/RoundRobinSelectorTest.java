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

import org.apache.dolphinscheduler.remote.utils.Host;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * round robin selector
 */
public class RoundRobinSelectorTest {

    @Test(expected = IllegalArgumentException.class)
    public void testSelectWithIllegalArgumentException() {
        RoundRobinSelector selector = new RoundRobinSelector();
        selector.select(null);
    }

    @Test
    public void testSelect1() {
        RoundRobinSelector selector = new RoundRobinSelector();
        // dismiss of server warm-up time
        long startTime = System.currentTimeMillis() - 60 * 10 * 1000;
        List<Host> hostOneList = Arrays.asList(
            new Host("192.168.1.1", 80, 20, startTime, "kris"),
            new Host("192.168.1.2", 80, 10, startTime, "kris"));

        List<Host> hostTwoList = Arrays.asList(
            new Host("192.168.1.1", 80, 20, startTime, "kris"),
            new Host("192.168.1.2", 80, 10, startTime, "kris"),
            new Host("192.168.1.3", 80, 10, startTime, "kris"));

        Host result;
        result = selector.select(hostOneList);
        Assert.assertEquals("192.168.1.1", result.getIp());

        result = selector.select(hostOneList);
        Assert.assertEquals("192.168.1.2", result.getIp());

        result = selector.select(hostOneList);
        Assert.assertEquals("192.168.1.1", result.getIp());

        result = selector.select(hostOneList);
        Assert.assertEquals("192.168.1.1", result.getIp());

        result = selector.select(hostOneList);
        Assert.assertEquals("192.168.1.2", result.getIp());

        // add new host
        result = selector.select(hostTwoList);
        Assert.assertEquals("192.168.1.1", result.getIp());

        result = selector.select(hostTwoList);
        Assert.assertEquals("192.168.1.3", result.getIp());

        result = selector.select(hostTwoList);
        Assert.assertEquals("192.168.1.1", result.getIp());

        result = selector.select(hostTwoList);
        Assert.assertEquals("192.168.1.2", result.getIp());
        result = selector.select(hostTwoList);
        Assert.assertEquals("192.168.1.1", result.getIp());

        result = selector.select(hostTwoList);
        Assert.assertEquals("192.168.1.3", result.getIp());

        // remove host3
        result = selector.select(hostOneList);
        Assert.assertEquals("192.168.1.1", result.getIp());

        result = selector.select(hostOneList);
        Assert.assertEquals("192.168.1.2", result.getIp());

        result = selector.select(hostOneList);
        Assert.assertEquals("192.168.1.1", result.getIp());

    }

    @Test
    public void testWarmUpRoundRobinSelector() {
        RoundRobinSelector selector = new RoundRobinSelector();
        Host result;
        result = selector.select(
            Arrays.asList(new Host("192.168.1.1", 80, 20, System.currentTimeMillis() - 60 * 1000 * 2, "kris"), new Host("192.168.1.2", 80, 10, System.currentTimeMillis() - 60 * 1000 * 10, "kris")));
        Assert.assertEquals("192.168.1.2", result.getIp());

    }

}
