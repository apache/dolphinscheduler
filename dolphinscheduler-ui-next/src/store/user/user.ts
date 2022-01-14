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

import { defineStore } from 'pinia'
import type { UserState } from '@/store/user/types'
import type { UserInfoRes } from '@/service/modules/users/types'

export const useUserStore = defineStore({
  id: 'user',
  state: (): UserState => ({
    sessionId: '',
    userInfo: {}
  }),
  persist: true,
  getters: {
    getSessionId(): string {
      return this.sessionId
    },
    getUserInfo(): UserInfoRes | {} {
      return this.userInfo
    }
  },
  actions: {
    setSessionId(sessionId: string): void {
      this.sessionId = sessionId
    },
    setUserInfo(userInfo: UserInfoRes | {}): void {
      this.userInfo = userInfo
    }
  }
})
