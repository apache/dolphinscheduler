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

import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import type { FormRules } from 'naive-ui'

export function useForm() {
  const { t } = useI18n()

  const state = reactive({
    passwordFormRef: ref(),
    rPasswordFormItemRef: ref(),
    passwordForm: {
      password: '',
      confirmPassword: ''
    },
    saving: false
  })

  const rules = {
    password: {
      trigger: ['input', 'blur'],
      required: true,
      message: t('password.password_tips')
    },
    confirmPassword: [
      {
        trigger: ['input', 'blur'],
        required: true,
        message: t('password.confirm_password_tips')
      },
      {
        trigger: ['password-input', 'blur', 'input'],
        message: t('password.two_password_entries_are_inconsistent'),
        validator: (unuse: any, value: string) => {
          if (value) {
            return state.passwordForm.password === value
          }
          return true
        }
      }
    ]
  } as FormRules

  return { state, rules, t }
}
