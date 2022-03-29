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

import { reactive, ref, Ref } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  queryUiPluginsByType,
  queryUiPluginDetailById
} from '@/service/modules/ui-plugins'
import type {
  IPluginId,
  IPlugin,
  FormRules,
  IMeta,
  IJsonItem,
  IRecord
} from './types'

export function useForm() {
  const { t } = useI18n()

  const initialValues = {
    instanceName: '',
    pluginDefineId: null
  }

  const state = reactive({
    detailFormRef: ref(),
    detailForm: { ...initialValues },
    uiPlugins: [],
    pluginsLoading: false,
    json: []
  } as { detailFormRef: Ref; json: IJsonItem[]; detailForm: { instanceName: string; pluginDefineId: number | null }; pluginsLoading: boolean; uiPlugins: [] })

  const meta = {
    model: state.detailForm,
    requireMarkPlacement: 'left',
    labelPlacement: 'left',
    labelWidth: 180,
    rules: {
      instanceName: {
        trigger: 'input',
        required: true,
        message: t('security.alarm_instance.alarm_instance_name_tips')
      },
      pluginDefineId: {
        trigger: ['blur', 'change'],
        required: true,
        validator(validte, value) {
          if (!value && value !== 0) {
            return new Error(t('security.alarm_instance.select_plugin_tips'))
          }
        }
      }
    } as FormRules
  } as IMeta

  const getUiPluginsByType = async () => {
    if (state.pluginsLoading) return
    state.pluginsLoading = true
    const plugins = await queryUiPluginsByType({ pluginType: 'ALERT' })
    state.uiPlugins = plugins.map((plugin: IPlugin) => ({
      label: plugin.pluginName,
      value: plugin.id
    }))
    state.pluginsLoading = false
  }

  const changePlugin = async (pluginId: IPluginId) => {
    if (state.pluginsLoading) return
    state.pluginsLoading = true
    state.detailForm.pluginDefineId = pluginId
    const { pluginParams } = await queryUiPluginDetailById(pluginId)
    if (pluginParams) {
      state.json = JSON.parse(pluginParams)
    }
    state.pluginsLoading = false
  }

  const initForm = () => {
    getUiPluginsByType()
  }

  const resetForm = () => {
    state.detailFormRef.resetValues({ ...initialValues })
    state.json = []
  }

  const getFormValues = () => state.detailForm

  const setDetail = (record: IRecord) => {
    state.detailForm.instanceName = record.instanceName
    state.detailForm.pluginDefineId = record.pluginDefineId
    if (record.pluginInstanceParams)
      state.json = JSON.parse(record.pluginInstanceParams)
  }

  return {
    meta,
    state,
    setDetail,
    initForm,
    resetForm,
    getFormValues,
    changePlugin
  }
}
