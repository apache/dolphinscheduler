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

type Execute =
  | 'NONE'
  | 'REPEAT_RUNNING'
  | 'RECOVER_SUSPENDED_PROCESS'
  | 'START_FAILURE_TASK_PROCESS'
  | 'STOP'
  | 'PAUSE'

type Exec =
  | 'START_PROCESS'
  | 'START_CURRENT_TASK_PROCESS'
  | 'RECOVER_TOLERANCE_FAULT_PROCESS'
  | 'RECOVER_SUSPENDED_PROCESS'
  | 'START_FAILURE_TASK_PROCESS'
  | 'COMPLEMENT_DATA'
  | 'SCHEDULER'
  | 'REPEAT_RUNNING'
  | 'PAUSE'
  | 'STOP'
  | 'RECOVER_WAITING_THREAD'

interface ExecuteReq {
  executeType: Execute
  processInstanceId: number
}

interface ProjectCodeReq {
  projectCode: number
}

interface ProcessDefinitionCodeReq {
  processDefinitionCode: number
}

interface ProcessInstanceReq extends ProcessDefinitionCodeReq {
  failureStrategy: 'END' | 'CONTINUE'
  processInstancePriority: 'HIGHEST' | 'HIGH' | 'MEDIUM' | 'LOW' | 'LOWEST'
  scheduleTime: string
  warningGroupId: number
  warningType: 'NONE' | 'SUCCESS' | 'FAILURE' | 'ALL'
  dryRun?: number
  environmentCode?: number
  execType?: Exec
  expectedParallelismNumber?: number
  runMode?: 'RUN_MODE_SERIAL' | 'RUN_MODE_PARALLEL'
  startNodeList?: string
  startParams?: string
  taskDependType?: 'TASK_ONLY' | 'TASK_PRE' | 'TASK_POST'
  timeout?: number
  workerGroup?: string
}

export {
  ExecuteReq,
  ProjectCodeReq,
  ProcessDefinitionCodeReq,
  ProcessInstanceReq
}
