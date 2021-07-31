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


import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;

import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 1. 循环检测超时问题
 * 2.
 */
public class StateWheelExecuteThread extends Thread {


    ConcurrentLinkedQueue<ProcessInstance>  processInstances = new ConcurrentLinkedQueue<>();
    ConcurrentLinkedQueue<TaskInstance> taskInstances = new ConcurrentLinkedQueue<>();



    @Override
    public void run(){

        while(Stopper.isRunning()){
            Date now  = new Date();
            timeoutCheck(now);


        }

    }

    private void timeoutCheck(Date now) {

        for(ProcessInstance processInstance : processInstances){

        }

        for(TaskInstance taskInstance : taskInstances){
            
        }
    }


    private void setTimeoutEvent(ProcessInstance processInstance){

    }

    private void setTimeoutEvent(TaskInstance taskInstance){

    }

    private void setDependentTaskEvent(TaskInstance taskInstance){

    }
}
