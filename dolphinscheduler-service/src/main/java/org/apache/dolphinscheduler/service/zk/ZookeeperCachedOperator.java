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
package org.apache.dolphinscheduler.service.zk;

import org.apache.curator.framework.recipes.cache.TreeCache;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ZookeeperCachedOperator extends ZookeeperOperator {

    private final Logger logger = LoggerFactory.getLogger(ZookeeperCachedOperator.class);

    private TreeCache treeCache;

    /**
     * The main point to define a listener list here is to execute the listener at customize order.
     */
    private List<AbstractListener> listenerList = new CopyOnWriteArrayList<>();

    /**
     * register a unified listener of /${dsRoot},
     */
    @Override
    public void registerListener(AbstractListener abstractListener) {
        logger.info("register zookeeper listener: {}", abstractListener.getClass().getName());
        listenerList.add(abstractListener);
        listenerList.sort(AbstractListener::compareTo);
    }

    @Override
    protected void treeCacheStart() {
        treeCache = new TreeCache(zkClient, getZookeeperConfig().getDsRoot() + "/nodes");
        logger.info("add listener to zk path: {}", getZookeeperConfig().getDsRoot());
        try {
            treeCache.start();
        } catch (Exception e) {
            logger.error("add listener to zk path: {} failed", getZookeeperConfig().getDsRoot());
            throw new RuntimeException(e);
        }
        treeCache.getListenable().addListener(((client, event) -> {
            for (AbstractListener abstractListener : listenerList) {
                logger.debug("zookeeperListener:{} triggered", abstractListener.getClass().getName());
                abstractListener.childEvent(client, event);
            }
        }));
    }

    @Override
    public void close() {
        treeCache.close();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignore) {
        }
        super.close();
    }
}
