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
import java.util.concurrent.ThreadLocalRandom;

/**
 * random selector
 */
public class RandomSelector extends AbstractSelector<HostWorker> {

    @Override
    public HostWorker doSelect(final Collection<HostWorker> source) {

        List<HostWorker> hosts = new ArrayList<>(source);
        int size = hosts.size();
        int[] weights = new int[size];
        int totalWeight = 0;
        int index = 0;

        for (HostWorker host : hosts) {
            totalWeight += host.getHostWeight();
            weights[index] = host.getHostWeight();
            index++;
        }

        if (totalWeight > 0) {
            int offset = ThreadLocalRandom.current().nextInt(totalWeight);

            for (int i = 0; i < size; i++) {
                offset -= weights[i];
                if (offset < 0) {
                    return hosts.get(i);
                }
            }
        }
        return hosts.get(ThreadLocalRandom.current().nextInt(size));
    }

}
