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
import type { TaskGroupUpdateReq } from '@/service/modules/task-group/types'

export function useForm() {
  const { t } = useI18n()

  const state = reactive({
    formRef: ref(),
    formData: {
      id: 0,
      name: '',
      projectCode: null as string | null,
      groupSize: '',
      status: 1,
      description: ''
    } as TaskGroupUpdateReq,
    saving: false,
    rules: {
      name: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (!state.formData.name) {
            return new Error(t('resource.task_group_option.please_enter_name'))
          }
        }
      },
      groupSize: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (!state.formData.groupSize) {
            return new Error(
              t('resource.task_group_option.please_enter_resource_pool_size')
            )
          }
          if (!/^[1-9]\d*$/.test(state.formData.groupSize)) {
            return new Error(
              t('resource.task_group_option.resource_pool_size') +
                t('resource.task_group_option.positive_integer_tips')
            )
          }
        }
      },
      projectCode: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (!state.formData.projectCode) {
            return new Error(
              t('resource.task_group_option.please_select_project')
            )
          }
        }
      }
    } as FormRules
  })

  return { state, t }
}
