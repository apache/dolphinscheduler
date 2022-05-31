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

package org.apache.dolphinscheduler.server.worker.cache;

import org.apache.dolphinscheduler.common.enums.Event;
import org.apache.dolphinscheduler.remote.command.Command;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Response Cache : cache worker send master result
 */
public class ResponseCache {

    private static final ResponseCache instance = new ResponseCache();

    private ResponseCache() {
    }

    public static ResponseCache get() {
        return instance;
    }

    private final Map<Integer, Command> runningCache = new ConcurrentHashMap<>();
    private final Map<Integer, Command> responseCache = new ConcurrentHashMap<>();
    private final Map<Integer,Command> recallCache = new ConcurrentHashMap<>();

    /**
     * cache response
     *
     * @param taskInstanceId taskInstanceId
     * @param command command
     * @param event event ACK/RESULT
     */
    public void cache(Integer taskInstanceId, Command command, Event event) {
        switch (event) {
            case RUNNING:
                runningCache.put(taskInstanceId, command);
                break;
            case RESULT:
                responseCache.put(taskInstanceId, command);
                break;
            case WORKER_REJECT:
                recallCache.put(taskInstanceId, command);
                break;
            default:
                throw new IllegalArgumentException("invalid event type : " + event);
        }
    }

    /**
     * recall response cache
     *
     * @param taskInstanceId taskInstanceId
     */
    public void removeRecallCache(Integer taskInstanceId) {
        recallCache.remove(taskInstanceId);
    }

    public Map<Integer, Command> getRecallCache() {
        return recallCache;
    }

    /**
     * remove running cache
     *
     * @param taskInstanceId taskInstanceId
     */
    public void removeRunningCache(Integer taskInstanceId) {
        runningCache.remove(taskInstanceId);
    }

    /**
     * remove response cache
     *
     * @param taskInstanceId taskInstanceId
     */
    public void removeResponseCache(Integer taskInstanceId) {
        responseCache.remove(taskInstanceId);
    }

    /**
     * get running cache
     *
     * @return getAckCache
     */
    public Map<Integer, Command> getRunningCache() {
        return runningCache;
    }

    /**
     * getResponseCache
     *
     * @return getResponseCache
     */
    public Map<Integer, Command> getResponseCache() {
        return responseCache;
    }
}
