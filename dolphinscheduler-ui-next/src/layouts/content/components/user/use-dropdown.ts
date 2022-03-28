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
import { logout } from '@/service/modules/logout'
import { useUserStore } from '@/store/user/user'
import type { Router } from 'vue-router'
import { DropdownOption } from 'naive-ui'
import cookies from 'js-cookie'

export function useDropDown() {
  const router: Router = useRouter()
  const userStore = useUserStore()

  const handleSelect = (key: string | number, unused: DropdownOption) => {
    if (key === 'logout') {
      useLogout()
    } else if (key === 'password') {
      router.push({ path: '/password' })
    } else if (key === 'profile') {
      router.push({ path: '/profile' })
    }
  }

  const useLogout = () => {
    logout().then(() => {
      userStore.setSessionId('')
      userStore.setUserInfo({})
      cookies.remove('sessionId')

      router.push({ path: '/login' })
    })
  }

  return {
    handleSelect
  }
}
