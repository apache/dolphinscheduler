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
import { reactive, onMounted, ref } from 'vue'
import type { INodeData, IJsonItem } from './types'
import * as Fields from './fields/index'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { getOptions } from '@/service/modules/dyn-api/dyn-api'

export const dealDynForm = (data: any, query: any) => {
  const { jsonData } = data
  const { apis, forms, model, locales } = jsonData
  const dynModel = reactive({
    ...model
  } as INodeData)
  const json = [] as IJsonItem[]
  forms.forEach((formItem: any) => {
    json.push(...formFilter(dynModel, formItem, query, locales, apis))
  })

  return { json, model: dynModel }
}

function formFilter(
  model: INodeData,
  formItem: any,
  query: any,
  locales: any,
  apis: any
): IJsonItem[] {
  const router = useRouter()
  const workflowCode = router.currentRoute.value.params.code
  const { type, field, isSimple } = formItem
  const { projectCode, from, data, readonly } = query
  let extra: IJsonItem[] = []
  if (from === 1) {
    extra = [
      Fields.useTaskType(model, readonly),
      Fields.useProcessName({
        model,
        projectCode,
        isCreate: !data?.id,
        from,
        processName: data?.processName
      })
    ]
  }
  useDynamicLocales(locales)
  switch (type) {
    case 'common':
      return [
        Fields.useName(from),
        ...extra,
        Fields.useRunFlag(),
        Fields.useDescription(),
        Fields.useTaskPriority(),
        Fields.useWorkerGroup(),
        Fields.useEnvironmentName(model, !data?.id),
        ...Fields.useTimeoutAlarm(model)
      ] as IJsonItem[]
    case 'taskGroup':
      return [...Fields.useTaskGroup(model, projectCode)] as IJsonItem[]
    case 'taskPriority':
      return [Fields.useTaskPriority()] as IJsonItem[]
    case 'failed':
      return [...Fields.useFailed()] as IJsonItem[]
    case 'delayTime':
      return [Fields.useDelayTime(model)] as IJsonItem[]
    case 'shell':
      return [...Fields.useShell(model)] as IJsonItem[]
    case 'childNode':
      return [
        Fields.useChildNode({
          model,
          projectCode,
          from,
          processName: data?.processName,
          code: from === 1 ? 0 : Number(workflowCode)
        })
      ] as IJsonItem[]
    case 'remoteConnect':
    case 'preTask':
      return [Fields.usePreTasks()] as IJsonItem[]
    case 'customerParams':
      return [
        ...Fields.useCustomParams({
          model,
          field,
          isSimple
        })
      ]
    case '':
      return [] as IJsonItem[]
    default:
      return normalForm(formItem, type, apis, query) as IJsonItem[]
  }
}

const normalForm = (
  formItem: any,
  type: string,
  apis: any,
  commonQuery: any
): IJsonItem[] => {
  const { field, defaultValue, label, validate, props } = formItem
  const { t } = useI18n()
  let getOptionsFun: any
  const dynOptions = ref(
    [] as {
      label: string
      value: string
    }[]
  )
  const formProp = {
    ...props,
    placeholder: t(formItem.placeholder || '')
  }
  if (formItem && formItem.optionsLocale === false && formItem.apiKey) {
    getOptionsFun = async () => {
      const { url, method } = apis[formItem.apiKey]
      const getOptionsApi = getOptions(url, method, {
        projectCode: commonQuery.projectCode
      })
      dynOptions.value = []

      getOptionsApi.then((res: any) => {
        res.map((item: any) => {
          if (res && res.length > 0) {
            dynOptions.value.push({
              label: item.taskName,
              value: String(item.taskId)
            })
          }
        })
      })
    }
  }

  onMounted(() => {
    if (formItem.type == 'select') {
      if (formItem && formItem.optionsLocale === false && formItem.apiKey) {
        getOptionsFun()
      } else {
        dynOptions.value = formItem.options.map((item: any) => {
          return {
            label: t(item.label),
            value: String(item.value)
          }
        })
      }
    }
  })

  const commonForm = {
    name: t(label),
    props: formProp,
    field,
    value: defaultValue || '',
    span: formItem.span || 24,
    validate: setValidate(validate, t)
  }

  switch (type) {
    case 'input':
      return [
        {
          type: 'input',
          ...commonForm
        }
      ] as IJsonItem[]
    case 'select':
      return [
        {
          type: 'select',
          ...commonForm,
          options: dynOptions
        }
      ] as IJsonItem[]
    case 'switch':
      return [
        {
          type: 'switch',
          ...commonForm
        }
      ] as IJsonItem[]
    case 'checkbox':
      return [
        {
          type: 'checkbox',
          ...commonForm,
          options: dynOptions
        }
      ] as IJsonItem[]
    case 'tree-select':
      return [
        {
          type: 'tree-select',
          ...commonForm
        }
      ] as IJsonItem[]
    case 'radio':
      return [
        {
          type: 'radio',
          ...commonForm,
          options: dynOptions
        }
      ] as IJsonItem[]
    case 'input-number':
      return [
        {
          type: 'input-number',
          ...commonForm,
          slots: {
            suffix: () => t(formItem.props.suffix || '')
          }
        }
      ] as IJsonItem[]
    default:
      return [] as IJsonItem[]
  }
}

export function useDynamicLocales(locales: any): void {
  useI18n().mergeLocaleMessage('zh_CN', { task_components: locales.zh_CN })
  useI18n().mergeLocaleMessage('en_US', { task_components: locales.en_US })
}

const setValidate = (validate: any, t: any) => {
  if (validate) {
    return {
      ...validate,
      message: t(validate.message)
    }
  } else {
    return undefined
  }
}
