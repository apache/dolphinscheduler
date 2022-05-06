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
import { useI18n } from 'vue-i18n'
import { useAsyncState } from '@vueuse/core'
import { queryAllWorkerGroups } from '@/service/modules/worker-groups'
import {
  verifyEnvironment,
  createEnvironment,
  updateEnvironment
} from '@/service/modules/environment'

export function useModal(
  props: any,
  ctx: SetupContext<('cancelModal' | 'confirmModal')[]>
) {
  const { t } = useI18n()

  const variables = reactive({
    environmentFormRef: ref(),
    model: {
      code: ref<number>(-1),
      name: ref(''),
      config: ref(''),
      description: ref(''),
      workerGroups: ref<Array<string>>([]),
      generalOptions: []
    },
    saving: false,
    rules: {
      name: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.name === '') {
            return new Error(t('security.environment.environment_name_tips'))
          }
        }
      },
      config: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.config === '') {
            return new Error(t('security.environment.environment_config_tips'))
          }
        }
      },
      description: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.description === '') {
            return new Error(
              t('security.environment.environment_description_tips')
            )
          }
        }
      }
    }
  })

  const getListData = () => {
    const { state } = useAsyncState(
      queryAllWorkerGroups().then((res: any) => {
        variables.model.generalOptions = res.map((item: any) => {
          return {
            label: item,
            value: item
          }
        })
      }),
      {}
    )

    return state
  }

  const handleValidate = async (statusRef: number) => {
    await variables.environmentFormRef.validate()

    if (variables.saving) return
    variables.saving = true

    try {
      statusRef === 0
        ? await submitEnvironmentModal()
        : await updateEnvironmentModal()
      variables.saving = false
    } catch (err) {
      variables.saving = false
    }
  }

  const submitEnvironmentModal = () => {
    verifyEnvironment({ environmentName: variables.model.name }).then(() => {
      const data = {
        name: variables.model.name,
        config: variables.model.config,
        description: variables.model.description,
        workerGroups: JSON.stringify(variables.model.workerGroups)
      }

      createEnvironment(data).then(() => {
        variables.model.name = ''
        variables.model.config = ''
        variables.model.description = ''
        variables.model.workerGroups = []
        ctx.emit('confirmModal', props.showModalRef)
      })
    })
  }

  const updateEnvironmentModal = () => {
    const data = {
      code: variables.model.code,
      name: variables.model.name,
      config: variables.model.config,
      description: variables.model.description,
      workerGroups: JSON.stringify(variables.model.workerGroups)
    }

    updateEnvironment(data).then(() => {
      ctx.emit('confirmModal', props.showModalRef)
    })
  }

  return {
    variables,
    handleValidate,
    getListData
  }
}
