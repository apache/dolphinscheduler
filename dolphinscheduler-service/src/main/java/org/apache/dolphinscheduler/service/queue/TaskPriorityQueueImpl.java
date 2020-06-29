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
package org.apache.dolphinscheduler.service.queue;


import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

import static org.apache.dolphinscheduler.common.Constants.*;

/**
 * A singleton of a task queue implemented with zookeeper
 * tasks queue implementation
 */
@Service
public class TaskPriorityQueueImpl implements TaskPriorityQueue {
    /**
     * queue size
     */
    private static final Integer QUEUE_MAX_SIZE = 3000;

    /**
     * queue
     */
    private PriorityBlockingQueue<String> queue = new PriorityBlockingQueue<>(QUEUE_MAX_SIZE, new TaskInfoComparator());

    /**
     * put task takePriorityInfo
     *
     * @param taskPriorityInfo takePriorityInfo
     * @throws Exception
     */
    @Override
    public void put(String taskPriorityInfo) throws Exception {
        queue.put(taskPriorityInfo);
    }

    /**
     * take taskInfo
     * @return taskInfo
     * @throws Exception
     */
    @Override
    public String take() throws Exception {
        return queue.take();
    }

    /**
     * queue size
     * @return size
     * @throws Exception
     */
    @Override
    public int size() throws Exception {
        return queue.size();
    }

    /**
     * TaskInfoComparator
     */
    private class TaskInfoComparator implements Comparator<String>{

        /**
         * compare o1 o2
         * @param o1 o1
         * @param o2 o2
         * @return compare result
         */
        @Override
        public int compare(String o1, String o2) {
            String s1 = o1;
            String s2 = o2;
            String[] s1Array = s1.split(UNDERLINE);
            if(s1Array.length > TASK_INFO_LENGTH){
                // warning: if this length > 5, need to be changed
                s1 = s1.substring(0, s1.lastIndexOf(UNDERLINE) );
            }

            String[] s2Array = s2.split(UNDERLINE);
            if(s2Array.length > TASK_INFO_LENGTH){
                // warning: if this length > 5, need to be changed
                s2 = s2.substring(0, s2.lastIndexOf(UNDERLINE) );
            }

            return s1.compareTo(s2);
        }
    }
}
