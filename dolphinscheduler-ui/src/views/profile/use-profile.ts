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

import { ref } from 'vue'
import { useUserStore } from '@/store/user/user'
import { useI18n } from 'vue-i18n'
import type { UserInfoRes } from '@/service/modules/users/types'
import type { InfoProps } from './types'
import type { Ref } from 'vue'

export function useProfile() {
  const { t } = useI18n()
  const userStore = useUserStore()
  const userInfo = userStore.getUserInfo as UserInfoRes
  const infoOptions: Ref<Array<InfoProps>> = ref([])

  infoOptions.value.push({
    key: t('profile.username'),
    value: userInfo.userName
  })
  infoOptions.value.push({ key: t('profile.email'), value: userInfo.email })
  infoOptions.value.push({ key: t('profile.phone'), value: userInfo.phone })
  infoOptions.value.push({
    key: t('profile.permission'),
    value:
      userInfo.userType === 'ADMIN_USER'
        ? t('profile.administrator')
        : t('profile.ordinary_user')
  })
  infoOptions.value.push({
    key: t('profile.create_time'),
    value: userInfo.createTime
  })
  infoOptions.value.push({
    key: t('profile.update_time'),
    value: userInfo.updateTime
  })

  return {
    infoOptions
  }
}
