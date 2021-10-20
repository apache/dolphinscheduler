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

import org.apache.dolphinscheduler.common.enums.Event;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.StateEvent;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.command.DBTaskAckCommand;
import org.apache.dolphinscheduler.remote.command.DBTaskResponseCommand;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThread;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

/**
 * task manager
 */
@Component
public class TaskResponseService {

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(TaskResponseService.class);

    /**
     * attemptQueue
     */
    private final BlockingQueue<TaskResponseEvent> eventQueue = new LinkedBlockingQueue<>();

    /**
     * process service
     */
    @Autowired
    private ProcessService processService;

    /**
     * task response worker
     */
    private Thread taskResponseWorker;

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    @PostConstruct
    public void start() {
        this.taskResponseWorker = new TaskResponseWorker();
        this.taskResponseWorker.setName("StateEventResponseWorker");
        this.taskResponseWorker.start();
    }

    @PreDestroy
    public void stop() {
        try {
            this.taskResponseWorker.interrupt();
            if (!eventQueue.isEmpty()) {
                List<TaskResponseEvent> remainEvents = new ArrayList<>(eventQueue.size());
                eventQueue.drainTo(remainEvents);
                for (TaskResponseEvent event : remainEvents) {
                    this.persist(event);
                }
            }
        } catch (Exception e) {
            logger.error("stop error:", e);
        }
    }

    /**
     * put task to attemptQueue
     *
     * @param taskResponseEvent taskResponseEvent
     */
    public void addResponse(TaskResponseEvent taskResponseEvent) {
        try {
            eventQueue.put(taskResponseEvent);
        } catch (InterruptedException e) {
            logger.error("put task : {} error :{}", taskResponseEvent, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * task worker thread
     */
    class TaskResponseWorker extends Thread {

        @Override
        public void run() {

            while (Stopper.isRunning()) {
                try {
                    // if not task , blocking here
                    TaskResponseEvent taskResponseEvent = eventQueue.take();
                    persist(taskResponseEvent);
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
     * persist  taskResponseEvent
     *
     * @param taskResponseEvent taskResponseEvent
     */
    private void persist(TaskResponseEvent taskResponseEvent) {
        Event event = taskResponseEvent.getEvent();
        Channel channel = taskResponseEvent.getChannel();

        TaskInstance taskInstance = processService.findTaskInstanceById(taskResponseEvent.getTaskInstanceId());
        switch (event) {
            case ACK:
                try {
                    if (taskInstance != null) {
                        ExecutionStatus status = taskInstance.getState().typeIsFinished() ? taskInstance.getState() : taskResponseEvent.getState();
                        processService.changeTaskState(taskInstance, status,
                                taskResponseEvent.getStartTime(),
                                taskResponseEvent.getWorkerAddress(),
                                taskResponseEvent.getExecutePath(),
                                taskResponseEvent.getLogPath(),
                                taskResponseEvent.getTaskInstanceId());
                    }
                    // if taskInstance is null (maybe deleted) . retry will be meaningless . so ack success
                    DBTaskAckCommand taskAckCommand = new DBTaskAckCommand(ExecutionStatus.SUCCESS.getCode(), taskResponseEvent.getTaskInstanceId());
                    channel.writeAndFlush(taskAckCommand.convert2Command());
                } catch (Exception e) {
                    logger.error("worker ack master error", e);
                    DBTaskAckCommand taskAckCommand = new DBTaskAckCommand(ExecutionStatus.FAILURE.getCode(), -1);
                    channel.writeAndFlush(taskAckCommand.convert2Command());
                }
                break;
            case RESULT:
                try {
                    if (taskInstance != null) {
                        processService.changeTaskState(taskInstance, taskResponseEvent.getState(),
                                taskResponseEvent.getEndTime(),
                                taskResponseEvent.getProcessId(),
                                taskResponseEvent.getAppIds(),
                                taskResponseEvent.getTaskInstanceId(),
                                taskResponseEvent.getVarPool()
                        );
                    }
                    // if taskInstance is null (maybe deleted) . retry will be meaningless . so response success
                    DBTaskResponseCommand taskResponseCommand = new DBTaskResponseCommand(ExecutionStatus.SUCCESS.getCode(), taskResponseEvent.getTaskInstanceId());
                    channel.writeAndFlush(taskResponseCommand.convert2Command());
                } catch (Exception e) {
                    logger.error("worker response master error", e);
                    DBTaskResponseCommand taskResponseCommand = new DBTaskResponseCommand(ExecutionStatus.FAILURE.getCode(), -1);
                    channel.writeAndFlush(taskResponseCommand.convert2Command());
                }
                break;
            default:
                throw new IllegalArgumentException("invalid event type : " + event);
        }
        WorkflowExecuteThread workflowExecuteThread = this.processInstanceExecCacheManager.getByProcessInstanceId(taskResponseEvent.getProcessInstanceId());
        if (workflowExecuteThread != null) {
            StateEvent stateEvent = new StateEvent();
            stateEvent.setProcessInstanceId(taskResponseEvent.getProcessInstanceId());
            stateEvent.setTaskInstanceId(taskResponseEvent.getTaskInstanceId());
            stateEvent.setExecutionStatus(taskResponseEvent.getState());
            stateEvent.setType(StateEventType.TASK_STATE_CHANGE);
            workflowExecuteThread.addStateEvent(stateEvent);
        }
    }

    public BlockingQueue<TaskResponseEvent> getEventQueue() {
        return eventQueue;
    }
}
