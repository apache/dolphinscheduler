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

package org.apache.dolphinscheduler.server.master.runner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class MasterExecService {

    /**
     * logger of MasterExecService
     */
    private static final Logger logger = LoggerFactory.getLogger(MasterExecService.class);

    /**
     * master exec service
     */
    private final ThreadPoolExecutor execService;

    private final ListeningExecutorService listeningExecutorService;

    /**
     * start process failed map
     */
    private final ConcurrentHashMap<Integer, WorkflowExecuteThread> startProcessFailedMap;

    private final ConcurrentHashMap<Integer, WorkflowExecuteThread> filterMap = new ConcurrentHashMap<>();

    public MasterExecService(ConcurrentHashMap<Integer, WorkflowExecuteThread> startProcessFailedMap,ThreadPoolExecutor execService) {
        this.startProcessFailedMap = startProcessFailedMap;
        this.execService = execService;
        this.listeningExecutorService = MoreExecutors.listeningDecorator(this.execService);
    }

    public void execute(WorkflowExecuteThread workflowExecuteThread) {
        if (workflowExecuteThread == null
                || workflowExecuteThread.getProcessInstance() == null
                || workflowExecuteThread.isStart()
                || filterMap.containsKey(workflowExecuteThread.getProcessInstance().getId())) {
            return;
        }
        Integer processInstanceId = workflowExecuteThread.getProcessInstance().getId();
        filterMap.put(processInstanceId, workflowExecuteThread);
        ListenableFuture future = this.listeningExecutorService.submit(workflowExecuteThread);
        FutureCallback futureCallback = new FutureCallback() {
            @Override
            public void onSuccess(Object o) {
                if (!workflowExecuteThread.isStart()) {
                    startProcessFailedMap.putIfAbsent(processInstanceId, workflowExecuteThread);
                } else {
                    startProcessFailedMap.remove(processInstanceId);
                }
                filterMap.remove(processInstanceId);
            }

            @Override
            public void onFailure(Throwable throwable) {
                logger.error("handle events {} failed", processInstanceId, throwable);
                if (!workflowExecuteThread.isStart()) {
                    startProcessFailedMap.putIfAbsent(processInstanceId, workflowExecuteThread);
                } else {
                    startProcessFailedMap.remove(processInstanceId);
                }
                filterMap.remove(processInstanceId);
            }
        };
        Futures.addCallback(future, futureCallback, this.listeningExecutorService);
    }

    public void shutdown() {
        this.execService.shutdown();
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return this.execService.awaitTermination(timeout, unit);
    }
}