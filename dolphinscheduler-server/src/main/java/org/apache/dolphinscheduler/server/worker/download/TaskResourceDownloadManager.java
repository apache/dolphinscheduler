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
package org.apache.dolphinscheduler.server.worker.download;

import org.apache.dolphinscheduler.server.entity.TaskResourceDownloadContext;
import org.apache.dolphinscheduler.server.worker.download.impl.file.FileResourceCache;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * task resource download manager
 */
public class TaskResourceDownloadManager {

    /**
     * resource cache information map
     */
    private ConcurrentHashMap<String, IResourceCache> resourceCacheMap = new ConcurrentHashMap<>();

    /**
     * single manager instance for task resource download
     */
    private static TaskResourceDownloadManager instance = new TaskResourceDownloadManager();

    /**
     * get instance
     * @return TaskResourceDownloadManager
     */
    public static TaskResourceDownloadManager getInstance(){
        return instance;
    }

    /**
     * download resource for task
     * @param execLocalPath
     * @param downloadContextList
     * @param logger
     */
    public void downloadResourceForTask(String execLocalPath, List<TaskResourceDownloadContext> downloadContextList, Logger logger) {
        for(TaskResourceDownloadContext downloadContext : downloadContextList) {
            IResourceCache resourceCache = getResourceCacheObject(downloadContext);
            resourceCache.cacheResource(downloadContext, logger);
            resourceCache.makeReference(downloadContext, execLocalPath, logger);
        }
    }

    /**
     * get resource cache object
     * @param downloadContext
     * @return IResourceCache
     */
    private synchronized IResourceCache getResourceCacheObject(TaskResourceDownloadContext downloadContext) {
        String cacheKey = buildCacheKey(downloadContext);
        IResourceCache resourceCache = resourceCacheMap.get(cacheKey);
        if (resourceCache == null) {
            resourceCache = newResourceCacheObject(downloadContext);
            resourceCacheMap.put(cacheKey, resourceCache);
        }
        return resourceCache;
    }

    /**
     * build cache key
     * @param downloadContext
     * @return
     */
    private String buildCacheKey(TaskResourceDownloadContext downloadContext) {
        return String.format("%s-%d", downloadContext.getResourceType(), downloadContext.getId());
    }

    /**
     * new resource cache object
     * @param downloadContext
     * @return
     */
    private static IResourceCache newResourceCacheObject(TaskResourceDownloadContext downloadContext) {
        switch (downloadContext.getResourceType()) {
            case FILE:
                return new FileResourceCache();
            default:
                throw new RuntimeException(String.format("unknown download context type: %s", downloadContext.getClass().getName()));
        }
    }
}
