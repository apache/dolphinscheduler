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

package org.apache.dolphinscheduler.server.master.processor.queue;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.Stopper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * task manager
 */
@Component
public class TaskEventService {

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(TaskEventService.class);

    /**
     * attemptQueue
     */
    private final BlockingQueue<TaskEvent> eventQueue = new LinkedBlockingQueue<>();

    /**
     * task event worker
     */
    private Thread taskEventThread;

    private Thread taskEventHandlerThread;

    @Autowired
    private TaskExecuteThreadPool taskExecuteThreadPool;

    @PostConstruct
    public void start() {
        this.taskEventThread = new TaskEventThread();
        this.taskEventThread.start();

        this.taskEventHandlerThread = new TaskEventHandlerThread();
        this.taskEventHandlerThread.start();
    }

    @PreDestroy
    public void stop() {
        try {
            this.taskEventThread.interrupt();
            this.taskEventHandlerThread.interrupt();
            if (!eventQueue.isEmpty()) {
                List<TaskEvent> remainEvents = new ArrayList<>(eventQueue.size());
                eventQueue.drainTo(remainEvents);
                for (TaskEvent taskEvent : remainEvents) {
                    taskExecuteThreadPool.submitTaskEvent(taskEvent);
                }
                taskExecuteThreadPool.eventHandler();
            }
        } catch (Exception e) {
            logger.error("TaskEventService stop error:", e);
        }
    }

    /**
     * add event
     *
     * @param taskEvent taskEvent
     */
    public void addEvent(TaskEvent taskEvent) {
        taskExecuteThreadPool.submitTaskEvent(taskEvent);
    }

    /**
     * task worker thread
     */
    class TaskEventThread extends BaseDaemonThread {
        protected TaskEventThread() {
            super("TaskEventLoopThread");
        }

        @Override
        public void run() {
            while (Stopper.isRunning()) {
                try {
                    // if not task , blocking here
                    TaskEvent taskEvent = eventQueue.take();
                    taskExecuteThreadPool.submitTaskEvent(taskEvent);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("persist task error", e);
                }
            }
            logger.info("StateEventResponseWorker stopped");
        }
    }

    /**
     * event handler thread
     */
    class TaskEventHandlerThread extends BaseDaemonThread {

        protected TaskEventHandlerThread() {
            super("TaskEventHandlerThread");
        }

        @Override
        public void run() {
            logger.info("event handler thread started");
            while (Stopper.isRunning()) {
                try {
                    taskExecuteThreadPool.eventHandler();
                    TimeUnit.MILLISECONDS.sleep(Constants.SLEEP_TIME_MILLIS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("event handler thread error", e);
                }
            }
        }
    }
}