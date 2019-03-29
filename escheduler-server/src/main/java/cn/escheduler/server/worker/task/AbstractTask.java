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
package cn.escheduler.server.worker.task;

import cn.escheduler.common.task.AbstractParameters;
import org.slf4j.Logger;

import java.util.List;

/**
 *  executive task
 */
public abstract class AbstractTask {

    /**
     * task props
     **/
    protected TaskProps taskProps;

    /**
     *  log record
     */
    protected Logger logger;


    /**
     *  cancel
     */
    protected volatile boolean cancel = false;

    /**
     *  exit code
     */
    protected volatile int exitStatusCode = -1;

    /**
     * @param taskProps
     * @param logger
     */
    protected AbstractTask(TaskProps taskProps, Logger logger) {
        this.taskProps = taskProps;
        this.logger = logger;
    }

    /**
     * init task
     */
    public void init() throws Exception {
    }

    /**
     * task handle
     */
    public abstract void handle() throws Exception;



    public void cancelApplication(boolean status) throws Exception {
        cancel = true;
    }

    /**
     *  log process
     */
    public void logHandle(List<String> logs) {
        // note that the "new line" is added here to facilitate log parsing
        logger.info(" -> {}", String.join("\n\t", logs));
    }


    /**
     *  exit code
     */
    public int getExitStatusCode() {
        return exitStatusCode;
    }


    /**
     * get task parameters
     */
    public abstract AbstractParameters getParameters();


}