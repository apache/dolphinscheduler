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

import { reactive, ref, h } from 'vue'
import { NIcon } from 'naive-ui'
import { UserOutlined, LogoutOutlined } from '@vicons/antd'

export function useDataList() {
  const renderIcon = (icon: any) => {
    return () => h(NIcon, null, { default: () => h(icon) })
  }

  const profileOptions = [
    {
      label: '用户信息',
      key: 'profile',
      icon: renderIcon(UserOutlined),
    },
    {
      label: '退出登录',
      key: 'logout',
      icon: renderIcon(LogoutOutlined),
    },
  ]

  const state = reactive({
    profileOptions: profileOptions,
  })

  return {
    state,
  }
}
