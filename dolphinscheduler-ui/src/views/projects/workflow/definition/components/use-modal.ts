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

import _, { cloneDeep } from 'lodash'
import { reactive, SetupContext } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import type { Router } from 'vue-router'
import { format } from 'date-fns'
import {
  batchCopyByCodes,
  importProcessDefinition,
  queryProcessDefinitionByCode
} from '@/service/modules/process-definition'
import { queryAllWorkerGroups } from '@/service/modules/worker-groups'
import { queryAllEnvironmentList } from '@/service/modules/environment'
import { listAlertGroupById } from '@/service/modules/alert-group'
import { startProcessInstance } from '@/service/modules/executors'
import {
  createSchedule,
  updateSchedule,
  previewSchedule
} from '@/service/modules/schedules'
import { parseTime } from '@/common/common'
import { EnvironmentItem } from '@/service/modules/environment/types'
import { ITimingState } from './types'

export function useModal(
  state: any,
  ctx: SetupContext<('update:show' | 'update:row' | 'updateList')[]>
) {
  const { t } = useI18n()
  const router: Router = useRouter()
  const route = useRoute()

  const variables = reactive<ITimingState>({
    projectCode: Number(route.params.projectCode),
    workerGroups: [],
    alertGroups: [],
    environmentList: [],
    startParamsList: [],
    schedulePreviewList: []
  })

  const cachedStartParams = {} as {
    [key: string]: { prop: string; value: string }[]
  }

  const resetImportForm = () => {
    state.importForm.name = ''
    state.importForm.file = ''
  }

  const handleImportDefinition = async () => {
    await state.importFormRef.validate()

    if (state.saving) return
    state.saving = true
    try {
      const formData = new FormData()
      formData.append('file', state.importForm.file)
      const code = Number(router.currentRoute.value.params.projectCode)
      await importProcessDefinition(formData, code)
      window.$message.success(t('project.workflow.success'))
      state.saving = false
      ctx.emit('updateList')
      ctx.emit('update:show')
      resetImportForm()
    } catch (err) {
      state.saving = false
    }
  }

  const handleStartDefinition = async (code: number) => {
    await state.startFormRef.validate()

    if (state.saving) return
    state.saving = true
    try {
      state.startForm.processDefinitionCode = code
      if (state.startForm.startEndTime) {
        const start = format(
          new Date(state.startForm.startEndTime[0]),
          'yyyy-MM-dd HH:mm:ss'
        )
        const end = format(
          new Date(state.startForm.startEndTime[1]),
          'yyyy-MM-dd HH:mm:ss'
        )
        state.startForm.scheduleTime = `${start},${end}`
      }

      const startParams = {} as any
      for (const item of variables.startParamsList) {
        if (item.value !== '') {
          startParams[item.prop] = item.value
        }
      }
      state.startForm.startParams = !_.isEmpty(startParams)
        ? JSON.stringify(startParams)
        : ''

      await startProcessInstance(state.startForm, variables.projectCode)
      window.$message.success(t('project.workflow.success'))
      state.saving = false
      ctx.emit('updateList')
      ctx.emit('update:show')
    } catch (err) {
      state.saving = false
    }
  }

  const handleCreateTiming = async (code: number) => {
    await state.timingFormRef.validate()

    if (state.saving) return
    state.saving = true
    try {
      const data: any = getTimingData()
      data.processDefinitionCode = code

      await createSchedule(data, variables.projectCode)
      window.$message.success(t('project.workflow.success'))
      state.saving = false
      ctx.emit('updateList')
      ctx.emit('update:show')
    } catch (err) {
      state.saving = false
    }
  }

  const handleUpdateTiming = async (id: number) => {
    await state.timingFormRef.validate()

    if (state.saving) return
    state.saving = true
    try {
      const data: any = getTimingData()
      data.id = id

      await updateSchedule(data, variables.projectCode, id)
      window.$message.success(t('project.workflow.success'))
      state.saving = false
      ctx.emit('updateList')
      ctx.emit('update:show')
    } catch (err) {
      state.saving = false
    }
  }

  const handleBatchCopyDefinition = async (codes: Array<string>) => {
    await state.copyFormRef.validate()

    if (state.saving) return
    state.saving = true
    try {
      const data = {
        codes: _.join(codes, ','),
        targetProjectCode: state.copyForm.projectCode
      }
      await batchCopyByCodes(data, variables.projectCode)
      window.$message.success(t('project.workflow.success'))
      state.saving = false
      ctx.emit('updateList')
      ctx.emit('update:show')
      state.copyForm.projectCode = ''
    } catch (err) {
      state.saving = false
    }
  }

  const getTimingData = () => {
    const start = format(
      parseTime(state.timingForm.startEndTime[0]),
      'yyyy-MM-dd HH:mm:ss'
    )
    const end = format(
      parseTime(state.timingForm.startEndTime[1]),
      'yyyy-MM-dd HH:mm:ss'
    )

    const data = {
      schedule: JSON.stringify({
        startTime: start,
        endTime: end,
        crontab: state.timingForm.crontab,
        timezoneId: state.timingForm.timezoneId
      }),
      failureStrategy: state.timingForm.failureStrategy,
      warningType: state.timingForm.warningType,
      processInstancePriority: state.timingForm.processInstancePriority,
      warningGroupId: state.timingForm.warningGroupId
        ? state.timingForm.warningGroupId
        : 0,
      workerGroup: state.timingForm.workerGroup,
      environmentCode: state.timingForm.environmentCode
    }
    return data
  }

  const getWorkerGroups = () => {
    queryAllWorkerGroups().then((res: any) => {
      variables.workerGroups = res.map((item: string) => ({
        label: item,
        value: item
      }))
    })
  }

  const getEnvironmentList = () => {
    queryAllEnvironmentList().then((res: Array<EnvironmentItem>) => {
      variables.environmentList = res.map((item) => ({
        label: item.name,
        value: item.code,
        workerGroups: item.workerGroups
      }))
    })
  }

  const getAlertGroups = () => {
    listAlertGroupById().then((res: any) => {
      variables.alertGroups = res.map((item: any) => ({
        label: item.groupName,
        value: item.id
      }))
    })
  }

  const getStartParamsList = (code: number) => {
    if (cachedStartParams[code]) {
      variables.startParamsList = cloneDeep(cachedStartParams[code])
      return
    }
    queryProcessDefinitionByCode(code, variables.projectCode).then(
      (res: any) => {
        variables.startParamsList = res.processDefinition.globalParamList
        cachedStartParams[code] = cloneDeep(variables.startParamsList)
      }
    )
  }

  const getPreviewSchedule = () => {
    state.timingFormRef.validate(async (valid: any) => {
      if (!valid) {
        const projectCode = Number(router.currentRoute.value.params.projectCode)

        const start = format(
          new Date(state.timingForm.startEndTime[0]),
          'yyyy-MM-dd HH:mm:ss'
        )
        const end = format(
          new Date(state.timingForm.startEndTime[1]),
          'yyyy-MM-dd HH:mm:ss'
        )

        const schedule = JSON.stringify({
          startTime: start,
          endTime: end,
          crontab: state.timingForm.crontab,
          timezoneId: state.timingForm.timezoneId
        })
        previewSchedule({ schedule }, projectCode).then((res: any) => {
          variables.schedulePreviewList = res
        })
      }
    })
  }

  return {
    variables,
    handleImportDefinition,
    handleStartDefinition,
    handleCreateTiming,
    handleUpdateTiming,
    handleBatchCopyDefinition,
    getWorkerGroups,
    getAlertGroups,
    getEnvironmentList,
    getStartParamsList,
    getPreviewSchedule
  }
}
