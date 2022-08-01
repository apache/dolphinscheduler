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
import type { InfoProps } from './types'
import type { Ref } from 'vue'

export function useSetting() {
  const settingOptions: Ref<Array<InfoProps>> = ref([])

  settingOptions.value.push({
    key: "Auto Refresh Time",
    value: "10s",
  })
  settingOptions.value.push({
    key: "Time Zone",
    value: "UTC",
  })
  settingOptions.value.push({
    key: "Theme",
    value: "Light",
  })
  settingOptions.value.push({
    key: "Language",
    value: "English",
  })
  settingOptions.value.push({
    key: "Developer Mode",
    value: "Enabled",
  })


  return {
    settingOptions
  }
}
