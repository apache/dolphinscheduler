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

package org.apache.dolphinscheduler.service.queue;

import org.apache.dolphinscheduler.common.model.Server;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MasterPriorityQueueTest {

    @Test
    public void getOrderedCollection() {

        MasterPriorityQueue queue = new MasterPriorityQueue();

        // Test empty queue
        Server[] emptyElements = queue.getOrderedElements();
        Assertions.assertArrayEquals(emptyElements, new Server[]{});

        // Test queue with fabricated servers
        queue.putAll(getServerList());
        Server[] orderElements = queue.getOrderedElements();
        Assertions.assertEquals(extractServerIds(orderElements), Arrays.asList(4, 2, 1, 3));

    }

    @Test
    public void refreshMasterList() {
        MasterPriorityQueue queue = new MasterPriorityQueue();

        // Test empty queue
        queue.clear();
        Assertions.assertEquals(queue.getIndex("127.0.0.1:124"), -1);

        // Test queue with fabricated servers
        queue.putAll(getServerList());

        Assertions.assertEquals(queue.getIndex("127.0.0.1:124"), 0);
        Assertions.assertEquals(queue.getIndex("127.0.0.1:122"), 1);
        Assertions.assertEquals(queue.getIndex("127.0.0.1:121"), 2);
        Assertions.assertEquals(queue.getIndex("127.0.0.1:123"), 3);

    }

    private List<Server> getServerList() {

        long baseTime = new Date().getTime();

        Server server1 = new Server();
        server1.setId(1);
        server1.setHost("127.0.0.1");
        server1.setPort(121);
        server1.setCreateTime(new Date(baseTime - 1000));

        Server server2 = new Server();
        server2.setId(2);
        server2.setHost("127.0.0.1");
        server2.setPort(122);
        server2.setCreateTime(new Date(baseTime + 1000));

        Server server3 = new Server();
        server3.setId(3);
        server3.setHost("127.0.0.1");
        server3.setPort(123);
        server3.setCreateTime(new Date(baseTime - 2000));

        Server server4 = new Server();
        server4.setId(4);
        server4.setHost("127.0.0.1");
        server4.setPort(124);
        server4.setCreateTime(new Date(baseTime + 2000));

        return Arrays.asList(server1, server2, server3, server4);
    }

    private List<Integer> extractServerIds(Server[] servers) {
        return Arrays.stream(servers).map(Server::getId).collect(Collectors.toList());
    }

}
