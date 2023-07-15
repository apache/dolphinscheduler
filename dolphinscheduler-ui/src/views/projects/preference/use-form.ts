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
import {onMounted, reactive, ref, Ref, unref} from 'vue'
import getElementByJson from '@/components/form/get-elements-by-json'
import type {
  IFormItem,
  IJsonItem,
  INodeData,
} from '../task/components/node/types'
import * as Fields from "@/views/projects/task/components/node/fields";
import { Router, useRouter } from "vue-router";
import {
  queryProjectPreferenceByProjectCode,
  updateProjectPreference
} from "@/service/modules/projects-preference";
import {useI18n} from "vue-i18n";
import { UpdateProjectPreferenceReq, ProjectPreferenceRes } from "@/service/modules/projects-preference/types";

export function useForm() {

  const router: Router = useRouter()
  const { t } = useI18n()

  const projectCode = Number(router.currentRoute.value.params.projectCode)

  const formRef = ref()
  const jsonRef = ref([]) as Ref<IJsonItem[]>
  const elementsRef = ref([]) as Ref<IFormItem[]>
  const rulesRef = ref({})
  const formProps = ref({})

  formProps.value = {
    labelPlacement: 'left',
    labelWidth: 'auto',
    size: 'large'
  }

  const data = reactive({
    model: {
      workerGroup: 'default',
      environmentCode: null,
      failRetryInterval: 1,
      failRetryTimes: 0
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
    updateProjectPreference(requestData, projectCode)
  }

  const preferencesItems: IJsonItem[] = [
    Fields.useWorkerGroup(),
  ]

  jsonRef.value = preferencesItems

  const getElements = () => {
    const { rules, elements } = getElementByJson(jsonRef.value, data.model)
    if (rules) {
      for (let key in rules) {
        rules[key].required = false
      }
    }
    elementsRef.value = elements
    rulesRef.value = rules
    console.log('elements')
    console.log(rules)
  }

  getElements()

  return { formRef, elementsRef, rulesRef, model: data.model, formProps, t, handleUpdate }
}
