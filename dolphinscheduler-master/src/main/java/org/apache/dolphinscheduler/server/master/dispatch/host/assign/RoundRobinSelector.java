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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

/**
 * Smooth Weight Round Robin
 */
@Service
public class RoundRobinSelector extends AbstractSelector<HostWorker> {

    private ConcurrentMap<String, ConcurrentMap<String, WeightedRoundRobin>> workGroupWeightMap =
            new ConcurrentHashMap<>();

    private static final int RECYCLE_PERIOD = 100000;

    private AtomicBoolean updateLock = new AtomicBoolean();

    protected static class WeightedRoundRobin {

        private int weight;
        private AtomicLong current = new AtomicLong(0);
        private long lastUpdate;

        int getWeight() {
            return weight;
        }

        void setWeight(int weight) {
            this.weight = weight;
            current.set(0);
        }

        long increaseCurrent() {
            return current.addAndGet(weight);
        }

        void sel(int total) {
            current.addAndGet(-1L * total);
        }

        long getLastUpdate() {
            return lastUpdate;
        }

        void setLastUpdate(long lastUpdate) {
            this.lastUpdate = lastUpdate;
        }

    }

    @Override
    public HostWorker doSelect(Collection<HostWorker> source) {

        List<HostWorker> hosts = new ArrayList<>(source);
        String key = hosts.get(0).getWorkerGroup();
        ConcurrentMap<String, WeightedRoundRobin> map = workGroupWeightMap.get(key);
        if (map == null) {
            workGroupWeightMap.putIfAbsent(key, new ConcurrentHashMap<>());
            map = workGroupWeightMap.get(key);
        }

        int totalWeight = 0;
        long maxCurrent = Long.MIN_VALUE;
        long now = System.currentTimeMillis();
        HostWorker selectedHost = null;
        WeightedRoundRobin selectWeightRoundRobin = null;

        for (HostWorker host : hosts) {
            String workGroupHost = host.getWorkerGroup() + host.getAddress();
            WeightedRoundRobin weightedRoundRobin = map.get(workGroupHost);
            int weight = host.getHostWeight();
            if (weight < 0) {
                weight = 0;
            }

            if (weightedRoundRobin == null) {
                weightedRoundRobin = new WeightedRoundRobin();
                // set weight
                weightedRoundRobin.setWeight(weight);
                map.putIfAbsent(workGroupHost, weightedRoundRobin);
                weightedRoundRobin = map.get(workGroupHost);
            }
            if (weight != weightedRoundRobin.getWeight()) {
                weightedRoundRobin.setWeight(weight);
            }

            long cur = weightedRoundRobin.increaseCurrent();
            weightedRoundRobin.setLastUpdate(now);
            if (cur > maxCurrent) {
                maxCurrent = cur;
                selectedHost = host;
                selectWeightRoundRobin = weightedRoundRobin;
            }

            totalWeight += weight;
        }

        if (!updateLock.get() && hosts.size() != map.size() && updateLock.compareAndSet(false, true)) {
            try {
                ConcurrentMap<String, WeightedRoundRobin> newMap = new ConcurrentHashMap<>(map);
                newMap.entrySet().removeIf(item -> now - item.getValue().getLastUpdate() > RECYCLE_PERIOD);
                workGroupWeightMap.put(key, newMap);
            } finally {
                updateLock.set(false);
            }
        }

        if (selectedHost != null) {
            selectWeightRoundRobin.sel(totalWeight);
            return selectedHost;
        }

        return hosts.get(0);
    }
}
