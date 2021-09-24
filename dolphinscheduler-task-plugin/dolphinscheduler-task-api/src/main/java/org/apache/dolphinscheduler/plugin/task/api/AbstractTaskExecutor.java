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

package org.apache.dolphinscheduler.plugin.task.api;

import org.apache.dolphinscheduler.spi.task.AbstractTask;
import org.apache.dolphinscheduler.spi.task.request.TaskRequest;

import java.util.StringJoiner;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public abstract class AbstractTaskExecutor extends AbstractTask {

    public static final Marker FINALIZE_SESSION_MARKER = MarkerFactory.getMarker("FINALIZE_SESSION");

    protected Logger logger;

    /**
     * constructor
     *
     * @param taskRequest taskRequest
     */
    protected AbstractTaskExecutor(TaskRequest taskRequest) {
        super(taskRequest);
        logger = LoggerFactory.getLogger(taskRequest.getTaskLogName());
    }

    /**
     * log handle
     *
     * @param logs log list
     */
    public void logHandle(LinkedBlockingQueue<String> logs) {
        // note that the "new line" is added here to facilitate log parsing
        if (logs.contains(FINALIZE_SESSION_MARKER.toString())) {
            logger.info(FINALIZE_SESSION_MARKER, FINALIZE_SESSION_MARKER.toString());
        } else {
            StringJoiner joiner = new StringJoiner("\n\t");
            while (!logs.isEmpty()) {
                joiner.add(logs.poll());
            }
            logger.info(" -> {}", joiner);
        }
    }
}
