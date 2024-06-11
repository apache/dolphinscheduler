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
  updateTrigger,
  genTaskCodeList,
  saveSingle,
  queryTriggerDefinitionByCode,
  updateWithUpstream
} from '@/service/modules/trigger-definition'
import { formatParams as formatData } from '../components/node/format-data'
import type { ITriggerData, INodeData, ISingleSaveReq, IRecord } from './types'
import { Connect } from '../../workflow/components/dag/types'

export function useTrigger(projectCode: number) {
  const initialTrigger = {
    taskType: 'SIMPLE'
  } as ITriggerData
  const trigger = reactive({
    triggerShow: false,
    triggerData: { ...initialTrigger },
    triggerSaving: false,
    triggerReadonly: false
  } as { triggerShow: boolean; triggerData: ITriggerData; triggerSaving: boolean; triggerReadonly: boolean })

  const formatParams = (data: INodeData, isCreate: boolean): ISingleSaveReq => {
    const params = formatData(data)
    if (isCreate) {
      return {
        processDefinitionCode: params.processDefinitionCode,
        upstreamCodes: params.upstreamCodes,
        taskDefinitionJsonObj: JSON.stringify(params.taskDefinitionJsonObj)
      }
    }
    return {
      upstreamCodes: params.upstreamCodes,
      taskDefinitionJsonObj: JSON.stringify(params.taskDefinitionJsonObj)
    }
  }

  const getTaskCode = async () => {
    const result = await genTaskCodeList(1, projectCode)
    return result[0]
  }

  const onToggleShow = (show: boolean) => {
    trigger.triggerShow = show
  }
  const onTriggerSave = async (data: INodeData) => {
    if (trigger.triggerSaving) return
    trigger.triggerSaving = true
    try {
      if (data.id) {
        data.code &&
          (await updateWithUpstream(
            projectCode,
            data.code,
            formatParams({ ...data, code: data.code }, false)
          ))
      } else {
        const taskCode = await getTaskCode()
        await saveSingle(
          projectCode,
          formatParams({ ...data, code: taskCode }, true)
        )
      }

      trigger.triggerSaving = false
      return true
    } catch (err) {
      trigger.triggerSaving = false
      return false
    }
  }

  const onEditTrigger = async (row: IRecord, readonly: boolean) => {
    const result = await queryTriggerDefinitionByCode(
      row.triggerCode,
      projectCode
    )
    trigger.triggerData = {
      ...result,
      processName: row.processDefinitionCode,
      preTasks:
        result?.processTaskRelationList?.map(
          (item: Connect) => item.preTaskCode
        ) || []
    }
    trigger.triggerShow = true
    trigger.triggerReadonly = readonly
  }

  const onInitTrigger = () => {
    trigger.triggerData = { ...initialTrigger }
    trigger.triggerReadonly = false
  }

  const onUpdateTrigger = async (data: INodeData) => {
    if (trigger.triggerSaving || !data.code) return
    trigger.triggerSaving = true

    const params = {
      taskExecuteType: 'STREAM',
      taskDefinitionJsonObj: JSON.stringify(
        formatData(data).taskDefinitionJsonObj
      )
    }

    try {
      await updateTrigger(projectCode, data.code, params)
      trigger.triggerSaving = false
      return true
    } catch (err) {
      trigger.triggerSaving = false
      return false
    }
  }

  return {
    trigger: trigger,
    onToggleShow,
    onTriggerSave,
    onEditTrigger,
    onInitTrigger,
    onUpdateTrigger
  }
}
