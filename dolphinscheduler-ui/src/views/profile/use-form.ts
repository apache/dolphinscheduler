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

import { reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/store/user/user'
import utils from '@/utils'
import type { FormRules } from 'naive-ui'
import type { UserInfoRes } from '@/service/modules/users/types'

export function useForm() {
  // todo: is "t": some kind of function to internationalize text?
  const { t, locale } = useI18n()
  const userInfo = useUserStore().userInfo as UserInfoRes

  const state = reactive({
    profileFormRef: ref(),
    profileForm: {
      username: userInfo.userName,
      email: userInfo.email,
      phone: userInfo.phone,
    },
    saving: false,
    rules: {
      username: {
        trigger: ['input', 'blur'],
        required: true,
        validator() {
          if (state.profileForm.username === '') {
            return new Error(t('profile.username_tips'))
          }
        }
      },
      email: {
        trigger: ['input', 'blur'],
        required: true,
        validator() {
          if (state.profileForm.email === '') {
            return new Error(t('profile.email_tips'))
          } else if (!utils.regex.email.test(state.profileForm.email || '')) {
            return new Error(t('profile.email_correct_tips'))
          }
        }
      }
    } as FormRules
  })

  watch(userInfo, () => {
    state.profileForm = {
      username: userInfo.userName,
      email: userInfo.email,
      phone: userInfo.phone,
    }
  })

  return {
    state,
    t,
    locale
  }
}
