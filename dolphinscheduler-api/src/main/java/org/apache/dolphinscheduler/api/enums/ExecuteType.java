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
package org.apache.dolphinscheduler.api.enums;

/**
 * execute type
 */
public enum ExecuteType {


    /**
     * 操作类型
     * 1.重跑 2.恢复暂停 3.恢复失败 4.停止 5.暂停
     */
    NONE,REPEAT_RUNNING, RECOVER_SUSPENDED_PROCESS, START_FAILURE_TASK_PROCESS, STOP, PAUSE;


    public static ExecuteType getEnum(int value){
        for (ExecuteType e: ExecuteType.values()) {
            if(e.ordinal() == value) {
                return e;
            }
        }
        return null;//For values out of enum scope
    }
}
