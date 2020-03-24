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

package org.apache.dolphinscheduler.server.master.manager;

import org.apache.dolphinscheduler.common.Constants;
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
public class TaskManager {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);

    /**
     * attemptQueue
     */
    private final BlockingQueue<TaskEvent> attemptQueue = new LinkedBlockingQueue<>(5000);


    /**
     * process service
     */
    @Autowired
    private ProcessService processService;


    @PostConstruct
    public void init(){
        TaskWorker taskWorker = new TaskWorker();
        taskWorker.start();
    }

    /**
     * put task to attemptQueue
     *
     * @param taskEvent taskEvent
     */
    public void putTask(TaskEvent taskEvent){
        try {
            attemptQueue.put(taskEvent);
        } catch (InterruptedException e) {
            logger.error("put task : {} error :{}",taskEvent,e);
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
                    if (attemptQueue.size() == 0){
                        Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                        continue;
                    }
                    TaskEvent taskEvent = attemptQueue.take();

                    persist(taskEvent);

                }catch (Exception e){
                    logger.error("persist task error",e);
                }
            }
        }

        /**
         * persist  taskEvent
         * @param taskEvent taskEvent
         */
        private void persist(TaskEvent taskEvent){
            if (TaskEvent.ACK.equals(taskEvent.getType())){
                processService.changeTaskState(taskEvent.getState(),
                        taskEvent.getStartTime(),
                        taskEvent.getWorkerAddress(),
                        taskEvent.getExecutePath(),
                        taskEvent.getLogPath(),
                        taskEvent.getTaskInstanceId());
            }else if (TaskEvent.RESPONSE.equals(taskEvent.getType())){
                processService.changeTaskState(taskEvent.getState(),
                        taskEvent.getEndTime(),
                        taskEvent.getProcessId(),
                        taskEvent.getAppIds(),
                        taskEvent.getTaskInstanceId());
            }
        }
    }
}
