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
package org.apache.dolphinscheduler.common.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * running status for workflow and task nodes
 *
 */
public enum ExecutionStatus {

    /**
     * status：
     * 0 submit success
     * 1 running
     * 2 ready pause
     * 3 pause
     * 4 ready stop
     * 5 stop
     * 6 failure
     * 7 success
     * 8 need fault tolerance
     * 9 kill
     * 10 waiting thread
     * 11 waiting depend node complete
     */
    SUBMITTED_SUCCESS(0, "submit success"),
    RUNNING_EXEUTION(1, "running"),
    READY_PAUSE(2, "ready pause"),
    PAUSE(3, "pause"),
    READY_STOP(4, "ready stop"),
    STOP(5, "stop"),
    FAILURE(6, "failure"),
    SUCCESS(7, "success"),
    NEED_FAULT_TOLERANCE(8, "need fault tolerance"),
    KILL(9, "kill"),
    WAITTING_THREAD(10, "waiting thread"),
    WAITTING_DEPEND(11, "waiting depend node complete");

    ExecutionStatus(int code, String descp){
        this.code = code;
        this.descp = descp;
    }

    @EnumValue
    private final int code;
    private final String descp;


 /**
  * status is success
  * @return status
  */
   public boolean typeIsSuccess(){
     return this == SUCCESS;
   }

 /**
  * status is failure
  * @return status
  */
   public boolean typeIsFailure(){
     return this == FAILURE || this == NEED_FAULT_TOLERANCE;
   }

 /**
  * status is finished
  * @return status
  */
   public boolean typeIsFinished(){

       return typeIsSuccess() || typeIsFailure() || typeIsCancel() || typeIsPause()
               || typeIsStop();
   }

    /**
     * status is waiting thread
     * @return status
     */
   public boolean typeIsWaitingThread(){
       return this == WAITTING_THREAD;
   }

    /**
     * status is pause
     * @return status
     */
   public boolean typeIsPause(){
       return this == PAUSE;
   }
    /**
     * status is pause
     * @return status
     */
    public boolean typeIsStop(){
        return this == STOP;
    }

    /**
     * status is running
     * @return status
     */
   public boolean typeIsRunning(){
       return this == RUNNING_EXEUTION || this == WAITTING_DEPEND;
   }

    /**
     * status is cancel
     * @return status
     */
    public boolean typeIsCancel(){
        return this == KILL || this == STOP ;
    }

    public int getCode() {
        return code;
    }

    public String getDescp() {
        return descp;
    }

    public static ExecutionStatus of(int status){
        for(ExecutionStatus es : values()){
            if(es.getCode() == status){
                return es;
            }
        }
        throw new IllegalArgumentException("invalid status : " + status);
    }
}
