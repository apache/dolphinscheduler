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
    securityConfigType: '',
    baseResDir: '',
    baseUdfDir: '',
    userInfo: {}
  }),
  persist: true,
  getters: {
    getSessionId(): string {
      return this.sessionId
    },
    getSecurityConfigType(): string {
      return this.securityConfigType
    },
    getUserInfo(): UserInfoRes | {} {
      return this.userInfo
    },
    getBaseResDir(): string {
      return this.baseResDir
    },
    getBaseUdfDir(): string {
      return this.baseUdfDir
    }
  },
  actions: {
    setSessionId(sessionId: string): void {
      this.sessionId = sessionId
    },
    setSecurityConfigType(securityConfigType: string): void {
      this.securityConfigType = securityConfigType
    },
    setUserInfo(userInfo: UserInfoRes | {}): void {
      this.userInfo = userInfo
    },
    setBaseResDir(baseResDir: string): void {
      this.baseResDir = baseResDir
    },
    setBaseUdfDir(baseUdfDir: string): void {
      this.baseUdfDir = baseUdfDir
    }
  }
})
