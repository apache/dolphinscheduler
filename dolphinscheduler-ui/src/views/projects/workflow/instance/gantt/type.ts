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

export type DateValue = string | number | null

export interface TaskDefinition {
  code: number
  name: string
  taskType?: string
}

export interface TaskInstance {
  id: number
  taskCode: number
  name: string
  state: string
  flag?: string
  taskType?: string
  startTime?: DateValue
  endTime?: DateValue
  submitTime?: DateValue
  logPath?: string
}

export interface WorkflowInstance {
  name?: string
  state: string
  startTime?: DateValue
  endTime?: DateValue
  dagData?: { taskDefinitionList?: TaskDefinition[] }
}

export interface IGanttRes {
  taskNames: number[]
  tasks: {
    taskName: string
    startDate: number[]
    endDate: number[]
    isoStart?: string
    status?: string
  }[]
}

export interface GanttRow {
  code: number
  name: string
  taskType: string
  id?: number
  state: string
  start: number | null
  end: number | null
  duration: number | null
  percent: number
  logAvailable: boolean
}
