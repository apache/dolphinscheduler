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

package org.apache.dolphinscheduler.server.master.runner.operator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskOperatorManager {

    @Autowired
    private TaskKillOperator taskKillOperator;

    @Autowired
    private TaskPauseOperator taskPauseOperator;

    @Autowired
    private TaskDispatchOperator taskDispatchOperator;

    @Autowired
    private TaskTimeoutOperator taskTimeoutOperator;

    public TaskOperator getTaskKillOperator() {
        return taskKillOperator;
    }

    public TaskPauseOperator getTaskPauseOperator() {
        return taskPauseOperator;
    }

    public TaskDispatchOperator getTaskDispatchOperator() {
        return taskDispatchOperator;
    }

    public TaskTimeoutOperator getTaskTimeoutOperator() {
        return taskTimeoutOperator;
    }

}
