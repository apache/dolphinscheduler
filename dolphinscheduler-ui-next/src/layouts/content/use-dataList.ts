/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the 'License'); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { reactive, h } from 'vue'
import { NIcon } from 'naive-ui'
import {
  HomeOutlined,
  ProfileOutlined,
  FolderOutlined,
  DatabaseOutlined,
  DesktopOutlined,
  SafetyCertificateOutlined,
  UserOutlined,
  LogoutOutlined
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
      children: [
        {
          label: '工作流监控',
          key: 'workflow-monitoring',
          icon: renderIcon(ProfileOutlined),
        },
        {
          label: '工作流关系',
          key: 'workflow-relationships',
          icon: renderIcon(ProfileOutlined),
        },
        {
          label: '工作流',
          key: 'workflow',
          icon: renderIcon(ProfileOutlined),
          children: [
            {
              label: '工作流定义',
              key: 'workflow-definition',
              icon: renderIcon(ProfileOutlined),
            },
            {
              label: '工作流实例',
              key: 'workflow-instance',
              icon: renderIcon(ProfileOutlined),
            },
            {
              label: '任务实例',
              key: 'task-instance',
              icon: renderIcon(ProfileOutlined),
            },
          ]
        },
      ]
    },
    {
      label: '资源中心',
      key: 'resources',
      icon: renderIcon(FolderOutlined),
      children: [
        {
          label: '文件管理',
          key: 'file-management',
          icon: renderIcon(ProfileOutlined),
        },
        {
          label: 'UDF管理',
          key: 'UDF-management',
          icon: renderIcon(ProfileOutlined),
          children: [
            {
              label: '资源管理',
              key: 'resource-management',
              icon: renderIcon(ProfileOutlined),
            },
            {
              label: '函数管理',
              key: 'function-management',
              icon: renderIcon(ProfileOutlined),
            }
          ]
        },
      ]
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
      children: [
        {
          label: '服务管理',
          key: 'service-management',
          icon: renderIcon(ProfileOutlined),
          children: [
            {
              label: 'Master',
              key: 'master',
              icon: renderIcon(ProfileOutlined),
            },
            {
              label: 'Worker',
              key: 'worker',
              icon: renderIcon(ProfileOutlined),
            },
            {
              label: 'DB',
              key: 'DB',
              icon: renderIcon(ProfileOutlined),
            }
          ]
        },
        {
          label: '统计管理',
          key: 'statistical-management',
          icon: renderIcon(ProfileOutlined),
          children: [
            {
              label: 'Statistics',
              key: 'statistics',
              icon: renderIcon(ProfileOutlined),
            },
          ]
        },
      ]
    },
    {
      label: '安全中心',
      key: 'security',
      icon: renderIcon(SafetyCertificateOutlined),
      children: [
        {
          label: '租户管理',
          key: 'tenant-management',
          icon: renderIcon(ProfileOutlined),
        },
        {
          label: '用户管理',
          key: 'user-management',
          icon: renderIcon(ProfileOutlined),
        },
        {
          label: '告警组管理',
          key: 'alarm-group-management',
          icon: renderIcon(ProfileOutlined),
        },
        {
          label: '告警实例管理',
          key: 'alarm-instance-management',
          icon: renderIcon(ProfileOutlined),
        },
        {
          label: 'Worker分组管理',
          key: 'worker-group-management',
          icon: renderIcon(ProfileOutlined),
        },
        {
          label: 'Yarn 队列管理',
          key: 'yarn-queue-management',
          icon: renderIcon(ProfileOutlined),
        },
        {
          label: '环境管理',
          key: 'environmental-management',
          icon: renderIcon(ProfileOutlined),
        },
        {
          label: '令牌管理',
          key: 'token-management',
          icon: renderIcon(ProfileOutlined),
        },
      ]
    },
  ]

  const languageOptions = [
    {
      label: 'English',
      key: 'en_US',
    },
    {
      label: '中文',
      key: 'zh_CN',
    },
  ]

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

  const getHeaderMenuOptions = (MenuOptions: any) => {
    let headerMenuOptions = []
    headerMenuOptions = MenuOptions.map((item: { label: string; key: string; icon: any }) => {
      return {
        label: item.label,
        key: item.key,
        icon: item.icon,
      }
    })
    return headerMenuOptions
  }

  const state = reactive({
    menuOptions: menuOptions,
    languageOptions: languageOptions,
    profileOptions: profileOptions,
  })

  return {
    state,
    getHeaderMenuOptions
  }
}
