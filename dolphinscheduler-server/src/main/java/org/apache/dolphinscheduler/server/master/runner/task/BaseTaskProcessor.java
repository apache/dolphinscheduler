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

package org.apache.dolphinscheduler.server.master.runner.task;

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseTaskProcessor implements ITaskProcessor {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected boolean killed = false;

    protected boolean paused = false;

    protected boolean timeout = false;

    protected TaskInstance taskInstance = null;

    protected ProcessInstance processInstance;

    /**
     * pause task, common tasks donot need this.
     *
     * @return
     */
    protected abstract boolean pauseTask();

    /**
     * kill task, all tasks need to realize this function
     *
     * @return
     */
    protected abstract boolean killTask();

    /**
     * task timeout process
     * @return
     */
    protected abstract boolean taskTimeout();

    @Override
    public void run() {
    }

    @Override
    public boolean action(TaskAction taskAction) {

        switch (taskAction) {
            case STOP:
                return stop();
            case PAUSE:
                return pause();
            case TIMEOUT:
                return timeout();
            default:
                logger.error("unknown task action: {}", taskAction.toString());

        }
        return false;
    }

    protected boolean timeout() {
        if (timeout) {
            return true;
        }
        timeout = taskTimeout();
        return timeout;
    }

    /**
     * @return
     */
    protected boolean pause() {
        if (paused) {
            return true;
        }
        paused = pauseTask();
        return paused;
    }

    protected boolean stop() {
        if (killed) {
            return true;
        }
        killed = killTask();
        return killed;
    }

    @Override
    public String getType() {
        return null;
    }
}