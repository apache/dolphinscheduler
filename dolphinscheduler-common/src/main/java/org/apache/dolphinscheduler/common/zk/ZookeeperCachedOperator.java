package org.apache.dolphinscheduler.common.zk;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.dolphinscheduler.common.utils.Preconditions.*;
import static org.apache.dolphinscheduler.common.utils.Preconditions.checkNotNull;

@Component
public class ZookeeperCachedOperator extends ZookeeperOperator {

    private final Logger logger = LoggerFactory.getLogger(ZookeeperCachedOperator.class);

    //kay is zk path, value is TreeCache
    private ConcurrentHashMap<String, TreeCache> allCaches = new ConcurrentHashMap<>();

    /**
     * @param cachePath zk path
     * @param listener  operator
     */
    public void registerListener(final String cachePath, final TreeCacheListener listener) {
        TreeCache newCache = new TreeCache(zkClient, cachePath);
        logger.info("add listener to zk path: {}", cachePath);
        try {
            newCache.start();
        } catch (Exception e) {
            logger.error("add listener to zk path: {} failed", cachePath);
            throw new RuntimeException(e);
        }

        newCache.getListenable().addListener(listener);

        allCaches.put(cachePath, newCache);
    }

    public String getFromCache(final String cachePath, final String key) {
        ChildData resultInCache = allCaches.get(checkNotNull(cachePath)).getCurrentData(key);
        if (null != resultInCache) {
            return null == resultInCache.getData() ? null : new String(resultInCache.getData(), StandardCharsets.UTF_8);
        }
        return null;
    }

    public TreeCache getTreeCache(final String cachePath) {
        return allCaches.get(checkNotNull(cachePath));
    }

    public void close() {

        allCaches.forEach((path, cache) -> {
            cache.close();
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignore) {
            }
        });
        super.close();
    }
}
