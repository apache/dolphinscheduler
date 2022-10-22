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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;

/**
 * lower weight round robin
 */
public class LowerWeightRoundRobin extends AbstractSelector<HostWeight> {

    /**
     * select
     *
     * @param sources sources
     * @return HostWeight
     */
    @Override
    public HostWeight doSelect(Collection<HostWeight> sources) {
        double totalWeight = 0;
        double lowWeight = 0;
        HostWeight lowerNode = null;
        List<HostWeight> weights = canAssignTaskHost(sources);
        for (HostWeight hostWeight : weights) {
            totalWeight += hostWeight.getWeight();
            hostWeight.setCurrentWeight(hostWeight.getCurrentWeight() + hostWeight.getWeight());
            if (lowerNode == null || lowWeight > hostWeight.getCurrentWeight()) {
                lowerNode = hostWeight;
                lowWeight = hostWeight.getCurrentWeight();
            }
        }
        if (lowerNode != null) {
            lowerNode.setCurrentWeight(lowerNode.getCurrentWeight() + totalWeight);
        }
        return lowerNode;
    }

    private List<HostWeight> canAssignTaskHost(Collection<HostWeight> sources) {
        if (CollectionUtils.isEmpty(sources)) {
            return Collections.emptyList();
        }
        List<HostWeight> zeroWaitingTask =
                sources.stream().filter(h -> h.getWaitingTaskCount() == 0).collect(Collectors.toList());
        if (!zeroWaitingTask.isEmpty()) {
            return zeroWaitingTask;
        }
        HostWeight hostWeight = sources.stream().min(Comparator.comparing(HostWeight::getWaitingTaskCount)).get();
        List<HostWeight> waitingTask = Lists.newArrayList(hostWeight);
        List<HostWeight> equalWaitingTask = sources.stream()
                .filter(h -> !h.getHost().equals(hostWeight.getHost())
                        && h.getWaitingTaskCount() == hostWeight.getWaitingTaskCount())
                .collect(Collectors.toList());
        if (!equalWaitingTask.isEmpty()) {
            waitingTask.addAll(equalWaitingTask);
        }
        return waitingTask;
    }
}
