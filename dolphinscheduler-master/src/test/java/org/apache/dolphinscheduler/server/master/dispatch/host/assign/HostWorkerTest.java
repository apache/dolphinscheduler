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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HostWorkerTest {

    @Test
    public void testHostWorker1() {
        HostWorker hostWorker = new HostWorker("192.158.2.2", 11, 20, "default");
        Assertions.assertEquals("192.158.2.2", hostWorker.getIp());
        Assertions.assertEquals(11, hostWorker.getPort());
        Assertions.assertEquals(20, hostWorker.getHostWeight());
        Assertions.assertEquals("default", hostWorker.getWorkerGroup());
    }

    @Test
    public void testHostWorker2() {
        HostWorker hostWorker = HostWorker.of("192.158.2.2:22", 80, "default");
        Assertions.assertEquals("192.158.2.2", hostWorker.getIp());
        Assertions.assertEquals(22, hostWorker.getPort());
        Assertions.assertEquals(80, hostWorker.getHostWeight());
        Assertions.assertEquals("default", hostWorker.getWorkerGroup());
    }

}
