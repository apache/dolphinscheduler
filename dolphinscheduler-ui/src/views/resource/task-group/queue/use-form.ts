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

import { useI18n } from 'vue-i18n'
import { reactive, ref } from 'vue'
import type { FormRules } from 'naive-ui'
import type { TaskGroupQueuePriorityUpdateReq } from '@/service/modules/task-group/types'

export function useForm() {
  const { t } = useI18n()

  const state = reactive({
    formRef: ref(),
    formData: {
      queueId: 0,
      priority: 0
    } as TaskGroupQueuePriorityUpdateReq,
    saving: false,
    rules: {
      priority: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          const value = state.formData.priority + ''
          if (value && state.formData.priority >= 0) {
          } else {
            return new Error(t('resource.task_group_queue.priority_not_empty'))
          }
        }
      }
    } as FormRules
  })

  return { state, t }
}
