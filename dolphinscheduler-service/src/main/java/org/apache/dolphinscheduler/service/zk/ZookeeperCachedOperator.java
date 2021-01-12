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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class ZookeeperCachedOperator extends ZookeeperOperator {

    private final Logger logger = LoggerFactory.getLogger(ZookeeperCachedOperator.class);


    private TreeCache treeCache;
    /**
     * register a unified listener of /${dsRoot},
     */
    @Override
    protected void registerListener() {

        treeCache.getListenable().addListener((client, event) -> {
            String path = null == event.getData() ? "" : event.getData().getPath();
            if (path.isEmpty()) {
                return;
            }
            dataChanged(client, event, path);
        });
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
    }

    //for sub class
    protected void dataChanged(final CuratorFramework client, final TreeCacheEvent event, final String path){}

    public String getFromCache(final String cachePath, final String key) {
        ChildData resultInCache = treeCache.getCurrentData(key);
        if (null != resultInCache) {
            return null == resultInCache.getData() ? null : new String(resultInCache.getData(), StandardCharsets.UTF_8);
        }
        return null;
    }

    public TreeCache getTreeCache(final String cachePath) {
        return treeCache;
    }

    public void addListener(TreeCacheListener listener){
        this.treeCache.getListenable().addListener(listener);
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
