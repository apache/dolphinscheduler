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

import { VNode } from 'vue'
import type { SelectOption } from 'naive-ui'
import type { IFormItem, IJsonItem } from '@/components/form/types'
import type { TaskType } from '@/views/projects/task/constants/task-type'

interface ITaskPriorityOption extends SelectOption {
  icon: VNode
  color: string
}
interface IEnvironmentNameOption {
  label: string
  value: string
  workerGroups?: string[]
}
interface ILocalParam {
  prop: string
  direct: string
  type: string
  value?: string
}

interface ISourceItem {
  id: number
}

interface ITaskParams {
  resourceList?: ISourceItem[]
  mainJar?: ISourceItem
  localParams?: ILocalParam[]
  rawScript?: string
  programType?: string
  sparkVersion?: string
  flinkVersion?: string
  jobManagerMemory?: string
  taskManagerMemory?: string
  slot?: number
  taskManager?: number
  parallelism?: number
  mainClass?: string
  deployMode?: string
  appName?: string
  driverCores?: number
  driverMemory?: string
  numExecutors?: number
  executorMemory?: string
  executorCores?: number
  mainArgs?: string
  others?: string
  httpMethod?: string
  httpCheckCondition?: string
  httpParams?: []
  url?: string
  condition?: string
  connectTimeout?: number
  socketTimeout?: number
  type?: string
  datasource?: string
  sql?: string
  sqlType?: string
  preStatements?: string[]
  postStatements?: string[]
  method?: string
  queue?: string
  master?: string
}

type ITaskType = TaskType

interface INodeData extends Omit<ITaskParams, 'resourceList' | 'mainJar'> {
  id?: string
  taskType?: ITaskType
  processName?: number
  delayTime?: number
  description?: string
  environmentCode?: number | null
  failRetryInterval?: number
  failRetryTimes?: number
  flag?: 'YES' | 'NO'
  taskGroupId?: number
  taskGroupPriority?: number
  taskPriority?: string
  timeout?: number
  timeoutFlag?: boolean
  timeoutNotifyStrategy?: string[]
  workerGroup?: string
  code?: number
  name?: string
  preTasks?: []
  preTaskOptions?: []
  postTaskOptions?: []
  resourceList?: number[]
  mainJar?: number
  timeoutSetting?: boolean
  type?: string
  datasource?: string
  sql?: string
  sqlType?: string
  preStatements?: string[]
  postStatements?: string[]
  method?: string
  masterUrl?: string
  resourceFiles?: {id: number, fullName: string}[] | null
}

interface ITaskData
  extends Omit<
    INodeData,
    'timeoutFlag' | 'taskPriority' | 'timeoutNotifyStrategy'
  > {
  name?: string
  taskPriority?: string
  timeoutFlag: 'OPEN' | 'CLOSE'
  timeoutNotifyStrategy?: string | []
  taskParams?: ITaskParams
}

export {
  ITaskPriorityOption,
  IEnvironmentNameOption,
  ILocalParam,
  ITaskType,
  ITaskData,
  INodeData,
  IFormItem,
  IJsonItem,
  ITaskParams
}
