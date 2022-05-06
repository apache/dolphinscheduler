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

import type { EditWorkflowDefinition } from '@/views/projects/workflow/components/dag/types'
import type { IOption } from '@/components/form/types'

type ProgramType = 'JAVA' | 'SCALA' | 'PYTHON' | 'SQL'
type DependentResultType = {
  [key: string]: 'SUCCESS' | 'WAITING_THREAD' | 'FAILURE'
}
type BDependentResultType = {
  [key: string]: 'SUCCESS' | 'WAITING_THREAD' | 'FAILED'
}

interface IResource {
  id: number
  name: string
  children?: IResource[]
}
interface IMainJar {
  id: number
  fullName: string
  children: IMainJar[]
}
interface TaskNodeState {
  postTaskOptions: IOption[]
  preTaskOptions: IOption[]
  preTasks: number[]
  resources: IResource[]
  mainJars: { [key in ProgramType]?: IMainJar[] }
  name: string
  dependentResult: DependentResultType
}
export {
  TaskNodeState,
  EditWorkflowDefinition,
  IOption,
  IResource,
  ProgramType,
  DependentResultType,
  BDependentResultType,
  IMainJar
}
