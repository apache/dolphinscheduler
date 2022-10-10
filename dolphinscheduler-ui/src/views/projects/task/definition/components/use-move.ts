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

import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAsyncState } from '@vueuse/core'
import { querySimpleList } from '@/service/modules/process-definition'
import { useRoute } from 'vue-router'
import { moveRelation } from '@/service/modules/process-task-relation'
import type { SimpleListRes } from '@/service/modules/process-definition/types'

export function useMove() {
  const { t } = useI18n()
  const route = useRoute()
  const projectCode = Number(route.params.projectCode)

  const variables = reactive({
    taskCode: ref(''),
    processDefinitionCode: ref(''),
    refreshTaskDefinition: ref(false),
    taskDefinitionFormRef: ref(),
    model: {
      targetProcessDefinitionCode: ref(''),
      generalOptions: []
    },
    saving: false,
    rules: {
      targetProcessDefinitionCode: {
        required: true,
        trigger: ['change', 'blur'],
        validator() {
          if (!variables.model.targetProcessDefinitionCode) {
            return new Error(t('project.task.workflow_name_tips'))
          }
        }
      }
    }
  })

  const getListData = () => {
    const { state } = useAsyncState(
      querySimpleList(projectCode).then((res: Array<SimpleListRes>) => {
        variables.model.generalOptions = res.map(
          (item): { label: string; value: number } => {
            return {
              label: item.name,
              value: item.code
            }
          }
        ) as any
      }),
      {}
    )

    return state
  }

  const handleValidate = () => {
    variables.taskDefinitionFormRef.validate((errors: any) => {
      if (errors) {
        return
      }
      moveTask()
    })
  }

  const moveTask = async () => {
    if (variables.saving) return
    variables.saving = true
    try {
      const data = {
        targetProcessDefinitionCode:
          variables.model.targetProcessDefinitionCode,
        taskCode: variables.taskCode,
        processDefinitionCode: variables.processDefinitionCode
      }
      await moveRelation(data, projectCode)
      variables.saving = false
      variables.model.targetProcessDefinitionCode = ''
      variables.refreshTaskDefinition = true
    } catch (err) {
      variables.saving = false
    }
  }

  return {
    variables,
    handleValidate,
    getListData
  }
}
