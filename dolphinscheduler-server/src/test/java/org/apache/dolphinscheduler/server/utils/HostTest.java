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

package org.apache.dolphinscheduler.server.utils;

import org.apache.dolphinscheduler.remote.utils.Host;

import org.junit.Assert;
import org.junit.Test;

/**
 * host test
 */
public class HostTest {

    @Test
    public void testHostWarmUp() {
        Host host = Host.of(("192.158.2.2:22:100:" + (System.currentTimeMillis() - 60 * 5 * 1000)));
        Assert.assertEquals(50, host.getWeight());
        host = Host.of(("192.158.2.2:22:100:" + (System.currentTimeMillis() - 60 * 10 * 1000)));
        Assert.assertEquals(100, host.getWeight());
    }

    @Test
    public void testHost() {
        Host host = Host.of("192.158.2.2:22");
        Assert.assertEquals(22, host.getPort());
    }
}
