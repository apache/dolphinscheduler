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
import org.apache.dolphinscheduler.common.utils.NetUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MasterPriorityQueue implements TaskPriorityQueue<Server> {

    /**
     * queue size
     */
    private static final Integer QUEUE_MAX_SIZE = 20;

    /**
     * queue
     */
    private PriorityBlockingQueue<Server> queue = new PriorityBlockingQueue<>(QUEUE_MAX_SIZE, new ServerComparator());

    private HashMap<String, Integer> hostIndexMap = new HashMap<>();

    @Override
    public void put(Server serverInfo) {
        this.queue.put(serverInfo);
        refreshMasterList();
    }

    @Override
    public Server take() throws InterruptedException {
        return queue.take();
    }

    @Override
    public Server poll(long timeout, TimeUnit unit) {
        return queue.poll();
    }

    @Override
    public int size() {
        return queue.size();
    }

    public void putAll(Collection<Server> serverList) {
        for (Server server : serverList) {
            this.queue.put(server);
        }
        refreshMasterList();
    }

    public void remove(Server server) {
        this.queue.remove(server);
    }

    public void clear() {
        queue.clear();
        refreshMasterList();
    }

    private void refreshMasterList() {
        hostIndexMap.clear();
        int index = 0;
        for (Server server : getOrderedElements()) {
            String addr = NetUtils.getAddr(server.getHost(), server.getPort());
            hostIndexMap.put(addr, index);
            index += 1;
        }
    }

    /**
     * get ordered collection of priority queue
     *
     * @return ordered collection
     */
    Server[] getOrderedElements() {
        Server[] nQueue = queue.toArray(new Server[0]);
        Arrays.sort(nQueue, new ServerComparator());
        return nQueue;
    }

    public int getIndex(String addr) {
        if (!hostIndexMap.containsKey(addr)) {
            return -1;
        }
        return hostIndexMap.get(addr);
    }

    /**
     * server comparator, used to sort server by createTime in reverse order.
     */
    private class ServerComparator implements Comparator<Server> {

        @Override
        public int compare(Server o1, Server o2) {
            return o2.getCreateTime().compareTo(o1.getCreateTime());
        }
    }

}
