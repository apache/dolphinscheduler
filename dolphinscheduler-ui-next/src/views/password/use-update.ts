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

import { useRouter } from 'vue-router'
import { updateUser } from '@/service/modules/users'
import { useUserStore } from '@/store/user/user'
import type { Router } from 'vue-router'
import type { UserInfoRes } from '@/service/modules/users/types'

export function useUpdate(state: any) {
  const router: Router = useRouter()
  const userStore = useUserStore()
  const userInfo = userStore.userInfo as UserInfoRes

  const handleUpdate = () => {
    state.passwordFormRef.validate(async (valid: any) => {
      if (!valid) {
        await updateUser({
          userPassword: state.passwordForm.password,
          id: userInfo.id,
          userName: userInfo.userName,
          tenantId: userInfo.tenantId,
          email: userInfo.email,
          phone: userInfo.phone,
          state: userInfo.state
        })

        await userStore.setSessionId('')
        await userStore.setUserInfo({})
        await router.push({ path: 'login' })
      }
    })
  }

  return {
    handleUpdate
  }
}
