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

import { updateUser } from '@/service/modules/users'
import { useUserStore } from '@/store/user/user'
import type { UserInfoRes } from '@/service/modules/users/types'

export function useUpdate(state: any) {
  const userStore = useUserStore()
  const userInfo = userStore.userInfo as UserInfoRes

  const handleUpdate = async () => {
    await state.profileFormRef.validate()

    if (state.saving === true) return
    state.saving = true

    try {
      await updateUser({
        userPassword: '',
        id: userInfo.id,
        userName: state.profileForm.username,
        tenantId: userInfo.tenantId,
        email: state.profileForm.email,
        phone: state.profileForm.phone,
        state: userInfo.state,
        queue: userInfo.queue
      })
      state.saving = false
    } catch (err) {
      state.saving = false
    }
  }

  return {
    handleUpdate
  }
}
