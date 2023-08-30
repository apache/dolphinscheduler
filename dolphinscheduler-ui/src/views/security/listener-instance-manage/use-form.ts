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
  IFormRules,
  IMeta,
  IJsonItem,
  IRecord
} from './types'
export function useForm() {
  const { t } = useI18n()

  const eventTypes = {
    SERVER_DOWN: {
      value: "SERVER_DOWN",
      label: t('security.listener_event.server_down'),
    },
    WORKFLOW_ADDED: {
      value: "WORKFLOW_ADDED",
      label: t('security.listener_event.workflow_added'),
    },
    WORKFLOW_UPDATE: {
      value: "WORKFLOW_UPDATE",
      label: t('security.listener_event.workflow_update'),
    },
    WORKFLOW_REMOVED: {
      value: "WORKFLOW_REMOVED",
      label: t('security.listener_event.workflow_removed'),
    },
    WORKFLOW_START: {
      value: "WORKFLOW_START",
      label: t('security.listener_event.workflow_start'),
    },
    WORKFLOW_END: {
      value: "WORKFLOW_END",
      label: t('security.listener_event.workflow_end'),
    },
    WORKFLOW_FAIL: {
      value: "WORKFLOW_FAIL",
      label: t('security.listener_event.workflow_fail'),
    },
    TASK_ADDED: {
      value: "TASK_ADDED",
      label: t('security.listener_event.task_added'),
    },
    TASK_UPDATE: {
      value: "TASK_UPDATE",
      label: t('security.listener_event.task_update'),
    },
    TASK_REMOVED: {
      value: "TASK_REMOVED",
      label: t('security.listener_event.task_removed'),
    },
    TASK_START: {
      value: "TASK_START",
      label: t('security.listener_event.task_start'),
    },
    TASK_END: {
      value: "TASK_END",
      label: t('security.listener_event.task_end'),
    },
    TASK_FAIL: {
      value: "TASK_FAIL",
      label: t('security.listener_event.task_fail'),
    },
  }

  const initialValues = {
    instanceName: '',
    pluginDefineId: null,
    listenerEventTypes: Object.values(
      eventTypes
    ).map((item)=>{
      return item.value;
    })
  }

  const state = reactive({
    detailFormRef: ref(),
    detailForm: { ...initialValues },
    uiPlugins: [],
    pluginsLoading: false,
    json: []
  } as { detailFormRef: Ref; json: IJsonItem[]; detailForm: { instanceName: string; pluginDefineId: number | null; listenerEventTypes: string[] }; pluginsLoading: boolean; uiPlugins: [] })

  const meta = {
    model: state.detailForm,
    requireMarkPlacement: 'left',
    labelPlacement: 'left',
    labelWidth: 180,
    rules: {
      instanceName: {
        trigger: 'input',
        required: true,
        message: t('security.listener_instance.instance_name_tips')
      },
      pluginDefineId: {
        trigger: ['blur', 'change'],
        required: true,
        validator(unused: any, value: number) {
          if (!value && value !== 0) {
            return new Error(t('security.listener_instance.select_plugin_tips'))
          }
        }
      },
      listenerEventTypes: {
        required: true,
        validator(unused: any, value: string) {
          if (!value) {
            return new Error(t('security.listener_instance.listener_event_types_tips'))
          }
        }
      }
    } as IFormRules
  } as IMeta

  const getUiPluginsByType = async () => {
    if (state.pluginsLoading) return
    state.pluginsLoading = true
    try {
      const plugins = await queryUiPluginsByType({ pluginType: 'LISTENER' })
      state.uiPlugins = plugins.map((plugin: IPlugin) => ({
        label: plugin.pluginName,
        value: plugin.id
      }))
      state.pluginsLoading = false
    } catch (e) {
      state.pluginsLoading = false
    }
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
    state.detailForm.listenerEventTypes = record.listenerEventTypes
    if (record.pluginInstanceParams)
      state.json = JSON.parse(record.pluginInstanceParams)
  }

  return {
    meta,
    state,
    eventTypes,
    setDetail,
    initForm,
    resetForm,
    getFormValues,
    changePlugin
  }
}



