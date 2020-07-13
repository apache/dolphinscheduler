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
package org.apache.dolphinscheduler.server.master.future;


import org.apache.dolphinscheduler.remote.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 *  task fulture
 */
public class TaskFuture {

    private final static Logger LOGGER = LoggerFactory.getLogger(TaskFuture.class);

    private final static ConcurrentHashMap<Long,TaskFuture> FUTURE_TABLE = new ConcurrentHashMap<>(256);

    /**
     *  request unique identification
     */
    private final long opaque;

    /**
     *  timeout
     */
    private final long timeoutMillis;

    private final CountDownLatch latch = new CountDownLatch(1);

    private final long beginTimestamp = System.currentTimeMillis();

    /**
     *  response command
     */
    private volatile Command responseCommand;

    private volatile boolean sendOk = true;

    private volatile Throwable cause;

    public TaskFuture(long opaque, long timeoutMillis) {
        this.opaque = opaque;
        this.timeoutMillis = timeoutMillis;
        FUTURE_TABLE.put(opaque, this);
    }

    /**
     * wait for response
     * @return command
     * @throws InterruptedException if error throws InterruptedException
     */
    public Command waitResponse() throws InterruptedException {
        this.latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        return this.responseCommand;
    }

    /**
     *  put response
     *
     * @param responseCommand responseCommand
     */
    public void putResponse(final Command responseCommand) {
        this.responseCommand = responseCommand;
        this.latch.countDown();
        FUTURE_TABLE.remove(opaque);
    }

    /**
     *  whether timeout
     * @return timeout
     */
    public boolean isTimeout() {
        long diff = System.currentTimeMillis() - this.beginTimestamp;
        return diff > this.timeoutMillis;
    }

    public static void notify(final Command responseCommand){
        TaskFuture taskFuture = FUTURE_TABLE.remove(responseCommand.getOpaque());
        if(taskFuture != null){
            taskFuture.putResponse(responseCommand);
        }
    }


    public boolean isSendOK() {
        return sendOk;
    }

    public void setSendOk(boolean sendOk) {
        this.sendOk = sendOk;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public Throwable getCause() {
        return cause;
    }

    public long getOpaque() {
        return opaque;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public long getBeginTimestamp() {
        return beginTimestamp;
    }

    public Command getResponseCommand() {
        return responseCommand;
    }

    public void setResponseCommand(Command responseCommand) {
        this.responseCommand = responseCommand;
    }


    /**
     * scan future table
     */
    public static void scanFutureTable(){
        final List<TaskFuture> futureList = new LinkedList<>();
        Iterator<Map.Entry<Long, TaskFuture>> it = FUTURE_TABLE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, TaskFuture> next = it.next();
            TaskFuture future = next.getValue();
            if ((future.getBeginTimestamp() + future.getTimeoutMillis() + 1000) <= System.currentTimeMillis()) {
                futureList.add(future);
                it.remove();
                LOGGER.warn("remove timeout request : {}", future);
            }
        }
    }

    @Override
    public String toString() {
        return "TaskFuture{" +
                "opaque=" + opaque +
                ", timeoutMillis=" + timeoutMillis +
                ", latch=" + latch +
                ", beginTimestamp=" + beginTimestamp +
                ", responseCommand=" + responseCommand +
                ", sendOk=" + sendOk +
                ", cause=" + cause +
                '}';
    }
}
