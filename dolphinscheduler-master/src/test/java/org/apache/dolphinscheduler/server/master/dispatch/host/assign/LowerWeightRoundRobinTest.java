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

import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LowerWeightRoundRobinTest {

    @Test
    public void testSelect() {
        Collection<HostWeight> sources = new ArrayList<>();
        sources.add(new HostWeight(HostWorker.of("192.158.2.1:11", 100, "default"), 0.06, 0.44, 3.84, 1,
                System.currentTimeMillis() - 60 * 8 * 1000));
        sources.add(new HostWeight(HostWorker.of("192.158.2.2:22", 100, "default"), 0.06, 0.56, 3.24, 2,
                System.currentTimeMillis() - 60 * 5 * 1000));
        sources.add(new HostWeight(HostWorker.of("192.158.2.3:33", 100, "default"), 0.06, 0.80, 3.15, 1,
                System.currentTimeMillis() - 60 * 2 * 1000));

        LowerWeightRoundRobin roundRobin = new LowerWeightRoundRobin();
        HostWeight result;
        result = roundRobin.select(sources);
        Assertions.assertEquals("192.158.2.1", result.getHost().getIp());
        result = roundRobin.select(sources);
        Assertions.assertEquals("192.158.2.1", result.getHost().getIp());
        Assertions.assertEquals("192.158.2.1", result.getHost().getIp());
        result = roundRobin.select(sources);
        Assertions.assertEquals("192.158.2.1", result.getHost().getIp());
        result = roundRobin.select(sources);
        Assertions.assertEquals("192.158.2.3", result.getHost().getIp());
        Assertions.assertEquals("192.158.2.3", result.getHost().getIp());
        result = roundRobin.select(sources);
        Assertions.assertEquals("192.158.2.1", result.getHost().getIp());
        result = roundRobin.select(sources);
        Assertions.assertEquals("192.158.2.1", result.getHost().getIp());
        Assertions.assertEquals("192.158.2.1", result.getHost().getIp());
    }

    @Test
    public void testWarmUpSelect() {
        Collection<HostWeight> sources = new ArrayList<>();
        sources.add(new HostWeight(HostWorker.of("192.158.2.1:11", 100, "default"), 0.06, 0.44, 3.84, 0,
                System.currentTimeMillis() - 60 * 8 * 1000));
        sources.add(new HostWeight(HostWorker.of("192.158.2.2:22", 100, "default"), 0.06, 0.44, 3.84, 0,
                System.currentTimeMillis() - 60 * 5 * 1000));
        sources.add(new HostWeight(HostWorker.of("192.158.2.3:33", 100, "default"), 0.06, 0.44, 3.84, 0,
                System.currentTimeMillis() - 60 * 3 * 1000));
        sources.add(new HostWeight(HostWorker.of("192.158.2.4:33", 100, "default"), 0.06, 0.44, 3.84, 0,
                System.currentTimeMillis() - 60 * 11 * 1000));

        LowerWeightRoundRobin roundRobin = new LowerWeightRoundRobin();
        HostWeight result;
        result = roundRobin.select(sources);
        Assertions.assertEquals("192.158.2.4", result.getHost().getIp());
        result = roundRobin.select(sources);
        Assertions.assertEquals("192.158.2.1", result.getHost().getIp());
        result = roundRobin.select(sources);
        Assertions.assertEquals("192.158.2.2", result.getHost().getIp());
        result = roundRobin.select(sources);
        Assertions.assertEquals("192.158.2.4", result.getHost().getIp());
        result = roundRobin.select(sources);
        Assertions.assertEquals("192.158.2.1", result.getHost().getIp());
    }

    @Test
    public void testDoSelect() {
        Collection<HostWeight> sources = new ArrayList<>();
        LowerWeightRoundRobin roundRobin = new LowerWeightRoundRobin();
        HostWeight result;
        result = roundRobin.doSelect(sources);
        Assertions.assertNull(result);

        sources.add(new HostWeight(HostWorker.of("192.158.2.1:11", 100, "default"), 0.06, 0.44, 3.14, 1,
                System.currentTimeMillis() - 60 * 8 * 1000));
        sources.add(new HostWeight(HostWorker.of("192.158.2.2:22", 100, "default"), 0.06, 0.56, 3.24, 2,
                System.currentTimeMillis() - 60 * 5 * 1000));
        sources.add(new HostWeight(HostWorker.of("192.158.2.3:33", 100, "default"), 0.06, 0.80, 3.15, 1,
                System.currentTimeMillis() - 60 * 2 * 1000));
        result = roundRobin.doSelect(sources);
        Assertions.assertEquals("192.158.2.1", result.getHost().getIp());
    }
}
