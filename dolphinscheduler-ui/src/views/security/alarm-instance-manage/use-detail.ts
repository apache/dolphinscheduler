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
import { isFunction } from 'lodash'
import {
  createAlertPluginInstance,
  testAlertPluginInstance,
  updateAlertPluginInstance,
  verifyAlertInstanceName
} from '@/service/modules/alert-plugin'
import type { IJsonItem, IRecord } from './types'
import { useI18n } from 'vue-i18n'

export function useDetail(getFormValues: Function) {
  const { t } = useI18n()

  const status = reactive({
    saving: false,
    testing: false,
    loading: false
  })

  const formatParams = (
    json?: IJsonItem[],
    values: { [field: string]: any } = {}
  ): string => {
    json?.forEach((item) => {
      const mergedItem = isFunction(item) ? item() : item
      mergedItem.value = values[mergedItem.field]
    })
    return JSON.stringify(json)
  }

  const testSend = async (json?: IJsonItem[]) => {
    const values = getFormValues()

    if (status.testing) return
    status.testing = true
    try {
      const res = await testAlertPluginInstance({
        pluginDefineId: values.pluginDefineId,
        pluginInstanceParams: formatParams(json, values)
      })
      window.$message.success(
        res && res.msg
          ? res.msg
          : `${t('security.alarm_instance.test_send')} ${t('home.success')}`
      )
    } finally {
      status.testing = false
    }
  }

  const createOrUpdate = async (currentRecord: IRecord, json?: IJsonItem[]) => {
    const values = getFormValues()
    if (status.saving) return false
    status.saving = true

    try {
      if (currentRecord?.instanceName !== values.instanceName) {
        await verifyAlertInstanceName({
          alertInstanceName: values.instanceName
        })
      }

      currentRecord?.id
        ? await updateAlertPluginInstance(
            {
              alertPluginInstanceId: values.pluginDefineId,
              instanceName: values.instanceName,
              warningType: values.warningType,
              pluginInstanceParams: formatParams(json, values)
            },
            currentRecord.id
          )
        : await createAlertPluginInstance({
            instanceName: values.instanceName,
            pluginDefineId: values.pluginDefineId,
            instanceType: values.instanceType,
            warningType: values.warningType,
            pluginInstanceParams: formatParams(json, values)
          })

      status.saving = false
      return true
    } catch (err) {
      status.saving = false
      return false
    }
  }

  return { status, createOrUpdate, testSend }
}
