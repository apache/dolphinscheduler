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
interface ITaskParams {
  conditionResult?: string
  switchResult?: string
  delayTime?: string
  dependence?: string
  waitStartTimeout?: string
}
interface ITimeout {
  enable: boolean
  timeout?: number
  strategy?: string
}
type ITaskType = TaskType

interface ITask {
  code: number
  timeoutNotifyStrategy?: string
  taskParams: ITaskParams
  description?: string
  id: number
  delayTime?: number
  failRetryTimes?: number
  name: string
  params?: object
  failRetryInterval?: number
  flag: string
  taskPriority?: number
  timeout: ITimeout
  timeoutFlag: 'CLOSE' | 'OPEN'
  taskType?: ITaskType
  workerGroup?: string
  environmentCode?: string
  taskGroupId?: string
  taskGroupPriority?: number
}

interface IDataNode {
  id?: string
  taskType: ITaskType
}

export {
  ITaskPriorityOption,
  IEnvironmentNameOption,
  ILocalParam,
  ITaskType,
  ITask,
  IDataNode,
  IFormItem,
  IJsonItem
}
