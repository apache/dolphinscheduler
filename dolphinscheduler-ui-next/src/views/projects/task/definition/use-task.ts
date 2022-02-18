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

import { reactive } from 'vue'
import {
  genTaskCodeList,
  saveSingle,
  queryTaskDefinitionByCode,
  updateWithUpstream
} from '@/service/modules/task-definition'
import { formatParams as formatData } from '../components/node/format-data'
import type { ITaskData, INodeData, ISingleSaveReq, IRecord } from './types'

export function useTask(projectCode: number) {
  const task = reactive({
    taskShow: false,
    taskData: {
      taskType: 'SHELL'
    },
    taskSaving: false,
    taskReadonly: false
  } as { taskShow: boolean; taskData: ITaskData; taskSaving: boolean; taskReadonly: boolean })

  const formatParams = (data: INodeData): ISingleSaveReq => {
    const params = formatData(data)
    return {
      processDefinitionCode: params.processDefinitionCode,
      upstreamCodes: params.upstreamCodes,
      taskDefinitionJsonObj: JSON.stringify(params.taskDefinitionJsonObj)
    }
  }

  const getTaskCode = async () => {
    const result = await genTaskCodeList(1, projectCode)
    return result[0]
  }

  const onToggleShow = (show: boolean) => {
    task.taskShow = show
  }
  const onTaskSave = async (data: INodeData) => {
    try {
      if (task.taskSaving) return
      task.taskSaving = true
      if (data.id) {
        data.code &&
          (await updateWithUpstream(
            projectCode,
            data.code,
            formatParams({ ...data, code: data.code })
          ))
      } else {
        console.log('onTaskSave...')
        const taskCode = await getTaskCode()
        console.log(taskCode)
        console.log(data)
        await saveSingle(projectCode, formatParams({ ...data, code: taskCode }))
      }

      task.taskSaving = false
      return true
    } catch (e) {
      window.$message.error((e as Error).message)
      task.taskSaving = false
      return false
    }
  }

  const onEditTask = async (row: IRecord, readonly: boolean) => {
    try {
      const result = await queryTaskDefinitionByCode(row.taskCode, projectCode)
      task.taskData = { ...result, processName: row.processDefinitionCode }
      task.taskShow = true
      task.taskReadonly = readonly
    } catch (e) {
      window.$message.error((e as Error).message)
    }
  }

  return {
    task,
    onToggleShow,
    onTaskSave,
    onEditTask
  }
}
