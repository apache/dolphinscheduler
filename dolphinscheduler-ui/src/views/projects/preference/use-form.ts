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
import { onMounted, reactive, ref, Ref } from 'vue'
import getElementByJson from '@/components/form/get-elements-by-json'
import type {
  IFormItem,
  IJsonItem,
  INodeData
} from '../task/components/node/types'
import * as Fields from '@/views/projects/task/components/node/fields'
import { Router, useRouter } from 'vue-router'
import {
  queryProjectPreferenceByProjectCode,
  updateProjectPreference,
  updateProjectPreferenceState
} from '@/service/modules/projects-preference'
import { useI18n } from 'vue-i18n'
import {
  UpdateProjectPreferenceReq,
  UpdateProjectPreferenceStateReq
} from '@/service/modules/projects-preference/types'
import { useWarningType } from '@/views/projects/preference/components/use-warning-type'
import { useTenant } from '@/views/projects/preference/components/use-tenant'
import { useAlertGroup } from '@/views/projects/preference/components/use-alert-group'

export function useForm() {
  const router: Router = useRouter()
  const { t } = useI18n()

  const projectCode = Number(router.currentRoute.value.params.projectCode)

  const formRef = ref()
  const jsonRef = ref([]) as Ref<IJsonItem[]>
  const elementsRef = ref([]) as Ref<IFormItem[]>
  const rulesRef = ref({})
  const formProps = ref({})
  const stateRef = ref(0)

  formProps.value = {
    labelPlacement: 'left',
    labelWidth: 'auto',
    size: 'large'
  }

  const data = reactive({
    model: {
      taskPriority: 'MEDIUM',
      workerGroup: 'default',
      environmentCode: null,
      failRetryTimes: 0,
      failRetryInterval: 1,
      cpuQuota: -1,
      memoryMax: -1,
      timeoutFlag: false,
      timeoutNotifyStrategy: ['WARN'],
      timeout: 30
    } as INodeData
  })

  const setValues = (initialValues: { [field: string]: any }) => {
    Object.assign(data.model, initialValues)
  }

  const initProjectPreference = async () => {
    if (projectCode) {
      const result = await queryProjectPreferenceByProjectCode(projectCode)
      if (result?.preferences) {
        setValues(JSON.parse(result.preferences))
        stateRef.value = result.state
      }
    }
  }

  onMounted(() => {
    initProjectPreference()
  })

  const handleUpdate = () => {
    const requestData = {
      projectPreferences: JSON.stringify(data.model)
    } as UpdateProjectPreferenceReq
    updateProjectPreference(requestData, projectCode).then(() => {
      window.$message.success(t('project.preference.success'))
    })
  }

  const handleUpdateState = (value: number) => {
    const requestData = {
      state: value
    } as UpdateProjectPreferenceStateReq

    updateProjectPreferenceState(requestData, projectCode).then(() => {
      window.$message.success(t('project.preference.success'))
    })
  }

  const preferencesItems: IJsonItem[] = [
    Fields.useTaskPriority(),
    useTenant(),
    Fields.useWorkerGroup(),
    Fields.useEnvironmentName(data.model, true),
    ...Fields.useFailed(),
    useWarningType(),
    useAlertGroup(),
    ...Fields.useResourceLimit()
  ]

  const restructurePreferencesItems = (preferencesItems: any) => {
    for (let item of preferencesItems) {
      if (item.validate?.required) {
        item.validate.required = false
        item.span = 12
      }
      Object.assign(item, { props: { style: 'width: 250px' } })
    }
    return preferencesItems
  }

  jsonRef.value = restructurePreferencesItems(preferencesItems)

  const getElements = () => {
    const { rules, elements } = getElementByJson(jsonRef.value, data.model)
    elementsRef.value = elements
    rulesRef.value = rules
  }

  getElements()

  return {
    formRef,
    elementsRef,
    rulesRef,
    model: data.model,
    stateRef,
    formProps,
    t,
    handleUpdate,
    handleUpdateState
  }
}
