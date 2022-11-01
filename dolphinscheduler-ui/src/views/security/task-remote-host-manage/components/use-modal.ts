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
import {
  createTaskRemoteHost,
  updateTaskRemoteHost,
  verifyTaskRemoteHost
} from '@/service/modules/task-remote-host'

export function useModal(
  props: any,
  ctx: SetupContext<('cancelModal' | 'confirmModal')[]>
) {
  const { t } = useI18n()

  const variables = reactive({
    taskRemoteHostFormRef: ref(),
    model: {
      code: ref<number>(-1),
      name: ref(''),
      ip: ref(''),
      port: ref<number>(22),
      account: ref(''),
      password: ref(''),
      description: ref(''),
      generalOptions: []
    },
    saving: false,
    rules: {
      name: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.name === '') {
            return new Error(
              t('security.task_remote_host.task_remote_host_name_tips')
            )
          }
        }
      },
      ip: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.ip === '') {
            return new Error(
              t('security.task_remote_host.task_remote_host_ip_tips')
            )
          }
        }
      },
      account: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.account === '') {
            return new Error(
              t('security.task_remote_host.task_remote_host_account_tips')
            )
          }
        }
      },
      password: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.password === '') {
            return new Error(
              t('security.task_remote_host.task_remote_host_password_tips')
            )
          }
        }
      },
      description: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.description === '') {
            return new Error(
              t('security.task_remote_host.task_remote_host_description_tips')
            )
          }
        }
      }
    }
  })

  const handleValidate = async (statusRef: number) => {
    await variables.taskRemoteHostFormRef.validate()

    if (variables.saving) return
    variables.saving = true

    try {
      statusRef === 0
        ? await submitTaskRemoteHostModal()
        : await updateTaskRemoteHostModal()
      variables.saving = false
    } finally {
      variables.saving = false
    }
  }

  const submitTaskRemoteHostModal = () => {
    verifyTaskRemoteHost({ taskRemoteHostName: variables.model.name }).then(
      () => {
        const data = {
          name: variables.model.name,
          ip: variables.model.ip,
          port: variables.model.port,
          account: variables.model.account,
          password: variables.model.password,
          description: variables.model.description
        }

        createTaskRemoteHost(data).then(() => {
          variables.model.name = ''
          variables.model.ip = ''
          variables.model.port = 22
          variables.model.account = ''
          variables.model.password = ''
          variables.model.description = ''
          ctx.emit('confirmModal', props.showModalRef)
        })
      }
    )
  }

  const updateTaskRemoteHostModal = () => {
    const data = {
      name: variables.model.name,
      ip: variables.model.ip,
      port: variables.model.port,
      account: variables.model.account,
      password: variables.model.password,
      description: variables.model.description
    }

    updateTaskRemoteHost(data, variables.model.code).then(() => {
      ctx.emit('confirmModal', props.showModalRef)
    })
  }

  return {
    variables,
    handleValidate
  }
}
