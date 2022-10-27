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
import { UISettingStore } from './types'

export const useUISettingStore = defineStore({
  id: 'ui-setting',
  state: (): UISettingStore => ({
    logTimer: 0,
    dynamicTask: false,
    apiTimer: 10000
  }),
  persist: true,
  getters: {
    getLogTimer(): number {
      return this.logTimer
    },
    getDynamicTask(): boolean {
      return this.dynamicTask
    },
    getApiTimer(): number {
      return this.apiTimer
    }
  },
  actions: {
    setLogTimer(timer: number): void {
      this.logTimer = timer
    },
    setDynamicTask(): void {
      this.dynamicTask = !this.dynamicTask
    },
    setApiTimer(timer: number): void {
      this.apiTimer = timer
    }
  }
})
