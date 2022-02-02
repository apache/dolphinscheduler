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

import _ from 'lodash'
import { reactive, SetupContext } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import type { Router } from 'vue-router'
import { format } from 'date-fns'
import { importProcessDefinition } from '@/service/modules/process-definition'
import { queryAllWorkerGroups } from '@/service/modules/worker-groups'
import { queryAllEnvironmentList } from '@/service/modules/environment'
import { listAlertGroupById } from '@/service/modules/alert-group'
import { startProcessInstance } from '@/service/modules/executors'
import {
  createSchedule,
  updateSchedule,
  previewSchedule
} from '@/service/modules/schedules'

export function useModal(
  state: any,
  ctx: SetupContext<('update:show' | 'update:row' | 'updateList')[]>
) {
  const { t } = useI18n()
  const router: Router = useRouter()

  const variables = reactive({
    projectCode: Number(router.currentRoute.value.params.projectCode),
    workerGroups: [],
    alertGroups: [],
    environmentList: [],
    startParamsList: [] as Array<{ prop: string; value: string }>,
    schedulePreviewList: []
  })

  const resetImportForm = () => {
    state.importFormRef.name = ''
    state.importFormRef.file = ''
  }

  const handleImportDefinition = () => {
    state.importFormRef.validate(async (valid: any) => {
      if (!valid) {
        try {
          const formData = new FormData()
          formData.append('file', state.importForm.file)
          const code = Number(router.currentRoute.value.params.projectCode)
          await importProcessDefinition(formData, code)
          window.$message.success(t('project.workflow.success'))
          ctx.emit('updateList')
          ctx.emit('update:show')
          resetImportForm()
        } catch (error: any) {
          window.$message.error(error.message)
        }
      }
    })
  }

  const handleStartDefinition = (code: number) => {
    state.startFormRef.validate(async (valid: any) => {
      if (!valid) {
        state.startForm.processDefinitionCode = code
        if (state.startForm.startEndTime) {
          const start = format(
            new Date(state.startForm.startEndTime[0]),
            'yyyy-MM-dd hh:mm:ss'
          )
          const end = format(
            new Date(state.startForm.startEndTime[1]),
            'yyyy-MM-dd hh:mm:ss'
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

        try {
          await startProcessInstance(state.startForm, variables.projectCode)
          window.$message.success(t('project.workflow.success'))
          ctx.emit('updateList')
          ctx.emit('update:show')
        } catch (error: any) {
          window.$message.error(error.message)
        }
      }
    })
  }

  const handleCreateTiming = (code: number) => {
    state.timingFormRef.validate(async (valid: any) => {
      if (!valid) {
        const data: any = getTimingData()
        data.processDefinitionCode = code

        try {
          await createSchedule(data, variables.projectCode)
          window.$message.success(t('project.workflow.success'))
          ctx.emit('updateList')
          ctx.emit('update:show')
        } catch (error: any) {
          window.$message.error(error.message)
        }
      }
    })
  }

  const handleUpdateTiming = (id: number) => {
    state.timingFormRef.validate(async (valid: any) => {
      if (!valid) {
        const data: any = getTimingData()
        data.id = id

        try {
          await updateSchedule(data, variables.projectCode, id)
          window.$message.success(t('project.workflow.success'))
          ctx.emit('updateList')
          ctx.emit('update:show')
        } catch (error: any) {
          window.$message.error(error.message)
        }
      }
    })
  }

  const getTimingData = () => {
    const start = format(
      new Date(state.timingForm.startEndTime[0]),
      'yyyy-MM-dd hh:mm:ss'
    )
    const end = format(
      new Date(state.timingForm.startEndTime[1]),
      'yyyy-MM-dd hh:mm:ss'
    )

    const data = {
      schedule: JSON.stringify({
        startTime: start,
        endTime: end,
        crontab: state.timingForm.crontab
      }),
      failureStrategy: state.timingForm.failureStrategy,
      warningType: state.timingForm.warningType,
      processInstancePriority: state.timingForm.processInstancePriority,
      warningGroupId:
        state.timingForm.warningGroupId === ''
          ? 0
          : state.timingForm.warningGroupId,
      workerGroup: state.timingForm.workerGroups,
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
    queryAllEnvironmentList().then((res: any) => {
      variables.environmentList = res.map((item: any) => ({
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

  const getPreviewSchedule = () => {
    state.timingFormRef.validate(async (valid: any) => {
      if (!valid) {
        const projectCode = Number(router.currentRoute.value.params.projectCode)

        const start = format(
          new Date(state.timingForm.startEndTime[0]),
          'yyyy-MM-dd hh:mm:ss'
        )
        const end = format(
          new Date(state.timingForm.startEndTime[1]),
          'yyyy-MM-dd hh:mm:ss'
        )

        const schedule = JSON.stringify({
          startTime: start,
          endTime: end,
          crontab: state.timingForm.crontab
        })
        previewSchedule({ schedule }, projectCode)
          .then((res: any) => {
            variables.schedulePreviewList = res
          })
          .catch((error: any) => {
            window.$message.error(error.message)
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
    getWorkerGroups,
    getAlertGroups,
    getEnvironmentList,
    getPreviewSchedule
  }
}
