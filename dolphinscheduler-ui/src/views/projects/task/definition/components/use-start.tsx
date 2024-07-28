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

import { startTaskDefinition } from '@/service/modules/task-definition'
import _ from 'lodash'
import { reactive, ref, SetupContext } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { queryProcessDefinitionByCode } from '@/service/modules/process-definition'
import { queryAllWorkerGroups } from '@/service/modules/worker-groups'
import { queryTenantList } from '@/service/modules/tenants'
import { queryAllEnvironmentList } from '@/service/modules/environment'
import { listAlertGroupById } from '@/service/modules/alert-group'
import type { EnvironmentItem } from '@/service/modules/environment/types'
import type { IStartState } from '../types'

export const useStart = (
  ctx: SetupContext<('update:show' | 'update:row' | 'updateList')[]>
) => {
  const { t } = useI18n()
  const route = useRoute()

  const variables = reactive({
    startFormRef: ref(),
    startForm: {
      version: 1,
      warningType: 'NONE',
      warningGroupId: null,
      workerGroup: 'default',
      tenantCode: 'default',
      environmentCode: null,
      startParams: null as null | string,
      dryRun: 0
    },
    startState: {
      projectCode: Number(route.params.projectCode),
      workerGroups: [],
      tenantList: [],
      alertGroups: [],
      environmentList: [],
      startParamsList: []
    } as IStartState,
    saving: false
  })

  const cachedStartParams = {} as {
    [key: string]: { prop: string; value: string }[]
  }

  const getWorkerGroups = () => {
    queryAllWorkerGroups().then((res: any) => {
      variables.startState.workerGroups = res.map((item: string) => ({
        label: item,
        value: item
      }))
    })
  }

  const getTenantList = () => {
    queryTenantList().then((res: any) => {
      variables.startState.tenantList = res.map((item: any) => ({
        label: item.tenantCode,
        value: item.tenantCode
      }))
    })
  }

  const getEnvironmentList = () => {
    queryAllEnvironmentList().then((res: Array<EnvironmentItem>) => {
      variables.startState.environmentList = res.map((item) => ({
        label: item.name,
        value: item.code,
        workerGroups: item.workerGroups
      }))
    })
  }

  const getAlertGroups = () => {
    listAlertGroupById().then((res: any) => {
      variables.startState.alertGroups = res.map((item: any) => ({
        label: item.groupName,
        value: item.id
      }))
    })
  }

  const getStartParamsList = (code: number) => {
    if (cachedStartParams[code]) {
      variables.startState.startParamsList = _.cloneDeep(
        cachedStartParams[code]
      )
      return
    }
    queryProcessDefinitionByCode(code, variables.startState.projectCode).then(
      (res: any) => {
        variables.startState.startParamsList =
          res.processDefinition.globalParamList
        cachedStartParams[code] = _.cloneDeep(
          variables.startState.startParamsList
        )
      }
    )
  }

  const handleStartDefinition = async (code: number) => {
    await variables.startFormRef.validate()

    if (variables.saving) return
    variables.saving = true
    try {
      const startParams = {} as any
      for (const item of variables.startState.startParamsList) {
        if (item.value !== '') {
          startParams[item.prop] = item.value
        }
      }
      variables.startForm.startParams = !_.isEmpty(startParams)
        ? JSON.stringify(startParams)
        : ''

      await startTaskDefinition(variables.startState.projectCode, code, {
        ...variables.startForm
      } as any)
      window.$message.success(t('project.task.success'))
      variables.saving = false
      ctx.emit('updateList')
      ctx.emit('update:show')
    } catch (err) {
      variables.saving = false
    }
  }

  return {
    variables,
    getWorkerGroups,
    getTenantList,
    getEnvironmentList,
    getAlertGroups,
    getStartParamsList,
    handleStartDefinition
  }
}
