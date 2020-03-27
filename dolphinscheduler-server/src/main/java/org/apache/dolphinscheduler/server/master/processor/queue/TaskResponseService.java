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

import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * task manager
 */
@Component
public class TaskResponseService {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(TaskResponseService.class);

    /**
     * attemptQueue
     */
    private final BlockingQueue<TaskResponseEvent> attemptQueue = new LinkedBlockingQueue<>(5000);


    /**
     * process service
     */
    @Autowired
    private ProcessService processService;


    @PostConstruct
    public void init(){
        TaskWorker taskWorker = new TaskWorker();
        taskWorker.setName("TaskWorkerThread");
        taskWorker.start();
    }

    /**
     * put task to attemptQueue
     *
     * @param taskResponseEvent taskResponseEvent
     */
    public void addResponse(TaskResponseEvent taskResponseEvent){
        try {
            attemptQueue.put(taskResponseEvent);
        } catch (InterruptedException e) {
            logger.error("put task : {} error :{}", taskResponseEvent,e);
        }
    }


    /**
     * task worker thread
     */
    class TaskWorker extends Thread {

        @Override
        public void run() {

            while (Stopper.isRunning()){
                try {
                    // if not task , blocking here
                    TaskResponseEvent taskResponseEvent = attemptQueue.take();
                    persist(taskResponseEvent);

                }catch (Exception e){
                    logger.error("persist task error",e);
                }
            }
        }

        /**
         * persist  taskResponseEvent
         * @param taskResponseEvent taskResponseEvent
         */
        private void persist(TaskResponseEvent taskResponseEvent){
            TaskResponseEvent.Event event = taskResponseEvent.getEvent();

            switch (event){
                case ACK:
                    processService.changeTaskState(taskResponseEvent.getState(),
                            taskResponseEvent.getStartTime(),
                            taskResponseEvent.getWorkerAddress(),
                            taskResponseEvent.getExecutePath(),
                            taskResponseEvent.getLogPath(),
                            taskResponseEvent.getTaskInstanceId());
                    break;
                case RESULT:
                    processService.changeTaskState(taskResponseEvent.getState(),
                            taskResponseEvent.getEndTime(),
                            taskResponseEvent.getProcessId(),
                            taskResponseEvent.getAppIds(),
                            taskResponseEvent.getTaskInstanceId());
                    break;
                default:
                    throw new IllegalArgumentException("invalid event type : " + event);
            }
        }
    }
}
