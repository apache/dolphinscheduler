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
package org.apache.dolphinscheduler.common.queue;

import java.util.List;
import java.util.Set;

public interface ITaskQueue {

    /**
     * take out all the elements
     *
     *
     * @param key
     * @return
     */
    List<String> getAllTasks(String key);

    /**
     * check task exists in the task queue or not
     *
     * @param key queue name
     * @param task ${processInstancePriority}_${processInstanceId}_${taskInstancePriority}_${taskId}
     * @return true if exists in the queue
     */
    boolean checkTaskExists(String key, String task);

    /**
     * add an element to the queue
     *
     * @param key  queue name
     * @param value
     */
    void add(String key, String value);

    /**
     * an element pops out of the queue
     *
     * @param key  queue name
     * @param n    how many elements to poll
     * @return
     */
    List<String> poll(String key, int n);

    /**
     * remove a element from queue
     * @param key
     * @param value
     */
    void removeNode(String key, String value);

    /**
     * add an element to the set
     *
     * @param key
     * @param value
     */
    void sadd(String key, String value);

    /**
     * delete the value corresponding to the key in the set
     *
     * @param key
     * @param value
     */
    void srem(String key, String value);

    /**
     * gets all the elements of the set based on the key
     *
     * @param key
     * @return
     */
    Set<String> smembers(String key);


    /**
     * clear the task queue for use by junit tests only
     */
    void delete();
}