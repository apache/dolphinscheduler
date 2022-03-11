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
import { updateUser } from '@/service/modules/users'
import { useTimezoneStore } from '@/store/timezone/timezone'
import { useUserStore } from '@/store/user/user'
import type { UserInfoRes } from '@/service/modules/users/types'

export function useDropDown(chooseVal: any) {
  const { t } = useI18n()

  const userStore = useUserStore()
  const timezoneStore = useTimezoneStore()

  const userInfo = userStore.userInfo as UserInfoRes

  const handleSelect = (value: string) => {
    updateUser({
      userPassword: '',
      id: userInfo.id,
      userName: '',
      tenantId: userInfo.tenantId,
      email: '',
      phone: userInfo.phone,
      state: userInfo.state,
      timeZone: value
    }).then(() => {
      chooseVal.value = value
      timezoneStore.setTimezone(value as string)
      window.$message.success(t('profile.timezone_success'))
    })
  }

  return {
    handleSelect
  }
}
