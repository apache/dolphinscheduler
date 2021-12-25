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
import {
  HomeOutlined,
  ProfileOutlined,
  FolderOutlined,
  DatabaseOutlined,
  DesktopOutlined,
  SafetyCertificateOutlined,
} from '@vicons/antd'

export function useDataList() {
  const renderIcon = (icon: any) => {
    return () => h(NIcon, null, { default: () => h(icon) })
  }

  const menuOptions = [
    {
      label: '首页',
      key: 'home',
      icon: renderIcon(HomeOutlined),
    },
    {
      label: '项目管理',
      key: 'project',
      icon: renderIcon(ProfileOutlined),
    },
    {
      label: '资源中心',
      key: 'resources',
      icon: renderIcon(FolderOutlined),
    },
    {
      label: '数据源中心',
      key: 'datasource',
      icon: renderIcon(DatabaseOutlined),
    },
    {
      label: '监控中心',
      key: 'monitor',
      icon: renderIcon(DesktopOutlined),
    },
    {
      label: '安全中心',
      key: 'security',
      icon: renderIcon(SafetyCertificateOutlined),
    },
  ]

  const state = reactive({
    activeKey: ref('home'),
    menuOptions: menuOptions,
  })

  return {
    state,
  }
}
