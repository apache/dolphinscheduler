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
package cn.escheduler.common.enums;


/**
 * runing status for workflow and task nodes
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
    SUBMITTED_SUCCESS,RUNNING_EXEUTION,READY_PAUSE,PAUSE,READY_STOP,STOP,FAILURE,SUCCESS,
    NEED_FAULT_TOLERANCE,KILL,WAITTING_THREAD,WAITTING_DEPEND;


 /**
  * status is success
  * @return
  */
   public boolean typeIsSuccess(){
     return this == SUCCESS;
   }

 /**
  * status is failure
  * @return
  */
   public boolean typeIsFailure(){
     return this == FAILURE || this == NEED_FAULT_TOLERANCE;
   }

 /**
  * status is finished
  * @return
  */
   public boolean typeIsFinished(){

       return typeIsSuccess() || typeIsFailure() || typeIsCancel() || typeIsPause()
               || typeIsWaittingThread();
   }

    /**
     * status is waiting thread
     * @return
     */
   public boolean typeIsWaittingThread(){
       return this == WAITTING_THREAD;
   }

    /**
     * status is pause
     * @return
     */
   public boolean typeIsPause(){
       return this == PAUSE;
   }

    /**
     * status is running
     * @return
     */
   public boolean typeIsRunning(){
       return this == RUNNING_EXEUTION || this == WAITTING_DEPEND;
   }

    /**
     * status is cancel
     */
    public boolean typeIsCancel(){ return this == KILL || this == STOP ;}


}
