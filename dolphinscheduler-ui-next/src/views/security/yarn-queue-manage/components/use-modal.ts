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
import { verifyQueue, createQueue, updateQueue } from '@/service/modules/queues'

export function useModal(
  props: any,
  ctx: SetupContext<('cancelModal' | 'confirmModal')[]>
) {
  const { t } = useI18n()

  const variables = reactive({
    yarnQueueFormRef: ref(),
    model: {
      id: ref<number>(-1),
      queue: ref(''),
      queueName: ref('')
    },
    saving: false,
    rules: {
      queue: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.queue === '') {
            return new Error(t('security.yarn_queue.queue_value_tips'))
          }
        }
      },
      queueName: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.queueName === '') {
            return new Error(t('security.yarn_queue.queue_name_tips'))
          }
        }
      }
    }
  })

  const handleValidate = async (statusRef: number) => {
    await variables.yarnQueueFormRef.validate()

    if (variables.saving) return
    variables.saving = true

    try {
      statusRef === 0
        ? await submitYarnQueueModal()
        : await updateYarnQueueModal()

      variables.saving = false
    } catch (err) {
      variables.saving = false
    }
  }

  const submitYarnQueueModal = () => {
    verifyQueue({ ...variables.model }).then(() => {
      createQueue({ ...variables.model }).then(() => {
        variables.model.queue = ''
        variables.model.queueName = ''
        ctx.emit('confirmModal', props.showModalRef)
      })
    })
  }

  const updateYarnQueueModal = () => {
    updateQueue({ ...variables.model }, { id: variables.model.id }).then(
      (ignored: any) => {
        ctx.emit('confirmModal', props.showModalRef)
      }
    )
  }

  return {
    variables,
    handleValidate
  }
}
