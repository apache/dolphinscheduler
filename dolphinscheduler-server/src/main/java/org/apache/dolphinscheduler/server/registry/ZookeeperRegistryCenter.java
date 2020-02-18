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

package org.apache.dolphinscheduler.server.registry;

import org.apache.dolphinscheduler.service.zk.ZookeeperCachedOperator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ZookeeperRegistryCenter implements InitializingBean {

    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    public static final String NAMESPACE = "/dolphinscheduler";

    public static final String NODES = NAMESPACE + "/nodes";

    public static final String MASTER_PATH = NODES + "/master";

    public static final String WORKER_PATH = NODES + "/worker";

    public static final String EMPTY = "";

    @Autowired
    protected ZookeeperCachedOperator zookeeperCachedOperator;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    public void init() {
        if (isStarted.compareAndSet(false, true)) {
            //TODO
//            zookeeperCachedOperator.start(NODES);
            initNodes();
        }
    }

    private void initNodes() {
        zookeeperCachedOperator.persist(MASTER_PATH, EMPTY);
        zookeeperCachedOperator.persist(WORKER_PATH, EMPTY);
    }

    public void close() {
        if (isStarted.compareAndSet(true, false)) {
            if (zookeeperCachedOperator != null) {
                zookeeperCachedOperator.close();
            }
        }
    }

    public String getMasterPath() {
        return MASTER_PATH;
    }

    public String getWorkerPath() {
        return WORKER_PATH;
    }

    public ZookeeperCachedOperator getZookeeperCachedOperator() {
        return zookeeperCachedOperator;
    }

}
