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

import { reactive, ref, SetupContext } from 'vue'
import { useAsyncState } from '@vueuse/core'
import {
  registerListenerPlugin,
  updateListenerPlugin,
} from '@/service/modules/listener-plugin'
import { useI18n } from 'vue-i18n'

export function useModalData(
  props: any,
  ctx: SetupContext<('cancelModal' | 'confirmModal')[]>
) {
  const { t } = useI18n()
  const variables = reactive({
    listenerPluginFormRef: ref(),
    model: {
      id: ref<number>(-1),
      classPath: ref(''),
      pluginJar: ref(''),
      generalOptions: []
    },
    saving: false,
    rules: {
      classPath: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (!variables.model.classPath) {
            return new Error(t('security.listener_plugin.class_path_tips'))
          }
        }
      },
      pluginJar: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (!variables.model.pluginJar) {
            return new Error(t('security.listener_plugin.plugin_jar_tips'))
          }
        }
      }
    }
  })

  const handleValidate = async (statusRef: number) => {
    await variables.listenerPluginFormRef.validate()

    if (variables.saving) return
    variables.saving = true
    try {
      statusRef === 0 ? await submitListenerPluginModal() : await updateListenerPluginModal()
      variables.saving = false
    } catch (err) {
      variables.saving = false
    }
  }

  const submitListenerPluginModal = () => {
    const formData = new FormData()
    formData.append('classPath', variables.model.classPath)
    formData.append('pluginJar', variables.model.pluginJar)
    registerListenerPlugin(formData as any).then(
      (unused: any) => {
        ctx.emit('confirmModal', props.showModalRef)
      })
  }

  const updateListenerPluginModal = () => {
    const formData = new FormData()
    formData.append('classPath', variables.model.classPath)
    formData.append('pluginJar', variables.model.pluginJar)
    updateListenerPlugin(formData as any, { id: variables.model.id }).then((unused: any) => {
      ctx.emit('confirmModal', props.showModalRef)
    })
  }

  return {
    variables,
    handleValidate,
  }
}
