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
type ITaskType = TaskType

interface INodeData {
  id?: string
  taskType?: ITaskType
  processCode?: string
  delayTime?: number
  description?: string
  environmentCode?: number | null
  failRetryInterval?: number
  failRetryTimes?: number
  flag?: 'YES' | 'NO'
  taskGroupId?: number
  taskGroupPriority?: number
  localParams?: ILocalParam[]
  rawScript?: string
  taskPriority?: string
  timeout?: number
  timeoutFlag?: boolean
  timeoutNotifyStrategy?: string[]
  workerGroup?: string
  resourceList?: number[]
  code?: number
  name?: string
}

interface ITaskData
  extends Omit<
    INodeData,
    'timeoutFlag' | 'taskPriority' | 'timeoutNotifyStrategy'
  > {
  name?: string
  processName?: number
  taskPriority?: string
  timeoutFlag: 'OPEN' | 'CLOSE'
  timeoutNotifyStrategy?: string | []
  taskParams: {
    resourceList: []
    rawScript: string
    localParams: ILocalParam[]
  }
  preTasks?: []
}

export {
  ITaskPriorityOption,
  IEnvironmentNameOption,
  ILocalParam,
  ITaskType,
  ITaskData,
  INodeData,
  IFormItem,
  IJsonItem
}
