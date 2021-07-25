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

package org.apache.dolphinscheduler.server.master.dispatch.host;

import org.apache.dolphinscheduler.server.master.dispatch.host.assign.HostWorker;
import org.apache.dolphinscheduler.server.master.dispatch.host.assign.RandomSelector;

import java.util.Collection;

/**
 *  random host manager
 */
public class RandomHostManager extends CommonHostManager {

    /**
     * selector
     */
    private final RandomSelector selector;

    /**
     * set round robin
     */
    public RandomHostManager() {
        this.selector = new RandomSelector();
    }

    @Override
    public HostWorker select(Collection<HostWorker> nodes) {
        return selector.select(nodes);
    }

}
