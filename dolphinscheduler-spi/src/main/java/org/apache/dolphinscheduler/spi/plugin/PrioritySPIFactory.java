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

package org.apache.dolphinscheduler.spi.plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrioritySPIFactory<T extends PrioritySPI> {

    private final Map<String, T> map = new HashMap<>();

    public PrioritySPIFactory(Class<T> spiClass) {
        for (T t : ServiceLoader.load(spiClass)) {
            if (map.containsKey(t.getIdentify().getName())) {
                resolveConflict(t);
            } else {
                map.put(t.getIdentify().getName(), t);
            }
        }
    }

    public Map<String, T> getSPIMap() {
        return Collections.unmodifiableMap(map);
    }

    private void resolveConflict(T newSPI) {
        SPIIdentify identify = newSPI.getIdentify();
        T oldSPI = map.get(identify.getName());

        if (newSPI.compareTo(oldSPI.getIdentify().getPriority()) == 0) {
            throw new IllegalArgumentException(
                    String.format("These two spi plugins has conflict identify name with the same priority: %s, %s",
                            oldSPI.getIdentify(), newSPI.getIdentify()));
        } else if (newSPI.compareTo(oldSPI.getIdentify().getPriority()) > 0) {
            log.info("The {} plugin has high priority, will override {}", newSPI.getIdentify(), oldSPI);
            map.put(identify.getName(), newSPI);
        } else {
            log.info("The low plugin {} will be skipped", newSPI);
        }
    }
}
