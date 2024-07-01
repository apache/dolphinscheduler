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
import { queryAlertPluginInstanceList } from '@/service/modules/alert-plugin'
import {
  verifyGroupName,
  createAlertGroup,
  updateAlertGroup
} from '@/service/modules/alert-group'
import type { AlertPluginItem } from '@/service/modules/alert-plugin/types'

export function useModal(
  props: any,
  ctx: SetupContext<('cancelModal' | 'confirmModal')[]>
) {
  const { t } = useI18n()

  const variables = reactive({
    alertGroupFormRef: ref(),
    model: {
      id: ref<number>(-1),
      groupName: ref(''),
      alertInstanceIds: ref<Array<number>>([]),
      description: ref(''),
      generalOptions: []
    },
    saving: false,
    rules: {
      groupName: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.groupName === '') {
            return new Error(t('security.alarm_group.alert_group_name_tips'))
          }
        }
      },
      alertInstanceIds: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.alertInstanceIds.length < 1) {
            return new Error(
              t('security.alarm_group.alarm_plugin_instance_tips')
            )
          }
        }
      }
    }
  })

  const getListData = () => {
    const { state } = useAsyncState(
      queryAlertPluginInstanceList().then((res: Array<AlertPluginItem>) => {
        variables.model.generalOptions = res.map(
          (item): { label: string; value: number } => {
            return {
              label: item.instanceName,
              value: item.id
            }
          }
        ) as any
      }),
      {}
    )

    return state
  }

  const handleValidate = async (statusRef: number) => {
    await variables.alertGroupFormRef.validate()

    if (variables.saving) return
    variables.saving = true

    try {
      statusRef === 0
        ? await submitAlertGroupModal()
        : await updateAlertGroupModal()

      variables.saving = false
    } catch (err) {
      variables.saving = false
    }
  }

  const submitAlertGroupModal = () => {
    verifyGroupName({ groupName: variables.model.groupName }).then(() => {
      const data = {
        groupName: variables.model.groupName,
        alertInstanceIds: variables.model.alertInstanceIds.toString(),
        description: variables.model.description
      }

      createAlertGroup(data).then(() => {
        variables.model.groupName = ''
        variables.model.alertInstanceIds = []
        variables.model.description = ''
        ctx.emit('confirmModal', props.showModalRef)
      })
    })
  }

  const updateAlertGroupModal = () => {
    const data = {
      groupName: variables.model.groupName,
      alertInstanceIds: variables.model.alertInstanceIds.toString(),
      description: variables.model.description
    }

    updateAlertGroup(data, { id: variables.model.id }).then(() => {
      ctx.emit('confirmModal', props.showModalRef)
    })
  }

  return {
    variables,
    handleValidate,
    getListData
  }
}
