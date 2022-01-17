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

package org.apache.dolphinscheduler.server.worker.runner;

import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.server.worker.cache.ResponceCache;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.processor.TaskCallbackService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Retry Report Task Status Thread
 */
@Component
public class RetryReportTaskStatusThread implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(RetryReportTaskStatusThread.class);

    @Autowired
    WorkerConfig workerConfig;

    /**
     *  task callback service
     */
    private final TaskCallbackService taskCallbackService;

    public void start(){
        Thread thread = new Thread(this,"RetryReportTaskStatusThread");
        thread.setDaemon(true);
        thread.start();
    }

    public RetryReportTaskStatusThread(){
        this.taskCallbackService = SpringApplicationContext.getBean(TaskCallbackService.class);
    }

    /**
     * retry ack/response
     */
    @Override
    public void run() {
        ResponceCache responceCache = ResponceCache.get();

        while (Stopper.isRunning()){

            // sleep 5 minutes
            ThreadUtils.sleep(workerConfig.getRetryReportTaskStatusInterval() * 1000);

            try {
                if (!responceCache.getAckCache().isEmpty()){
                    Map<Integer,Command> ackCache =  responceCache.getAckCache();
                    for (Map.Entry<Integer, Command> entry : ackCache.entrySet()){
                        Integer taskInstanceId = entry.getKey();
                        Command ackCommand = entry.getValue();
                        taskCallbackService.sendAck(taskInstanceId,ackCommand);
                    }
                }

                if (!responceCache.getResponseCache().isEmpty()){
                    Map<Integer,Command> responseCache =  responceCache.getResponseCache();
                    for (Map.Entry<Integer, Command> entry : responseCache.entrySet()){
                        Integer taskInstanceId = entry.getKey();
                        Command responseCommand = entry.getValue();
                        taskCallbackService.sendResult(taskInstanceId,responseCommand);
                    }
                }
                if (!responceCache.getKillResponseCache().isEmpty()) {
                    Map<Integer, Command> killResponseCache = responceCache.getKillResponseCache();
                    for (Map.Entry<Integer, Command> entry : killResponseCache.entrySet()) {
                        Integer taskInstanceId = entry.getKey();
                        Command killResponseCommand = entry.getValue();
                        taskCallbackService.sendResult(taskInstanceId, killResponseCommand);
                    }
                }
            }catch (Exception e){
                logger.warn("retry report task status error", e);
            }
        }
    }
}
