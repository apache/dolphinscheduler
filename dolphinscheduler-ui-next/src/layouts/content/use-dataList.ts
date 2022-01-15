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

import { reactive, h } from 'vue'
import { NIcon } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import {
  HomeOutlined,
  ProfileOutlined,
  FolderOutlined,
  DatabaseOutlined,
  DesktopOutlined,
  SafetyCertificateOutlined,
  UserOutlined,
  LogoutOutlined,
  FundProjectionScreenOutlined,
  PartitionOutlined,
  SettingOutlined,
  FileSearchOutlined,
  RobotOutlined,
  AppstoreOutlined,
  UsergroupAddOutlined,
  UserAddOutlined,
  WarningOutlined,
  InfoCircleOutlined,
  ControlOutlined,
  SlackOutlined,
  EnvironmentOutlined,
  KeyOutlined,
  SafetyOutlined,
  GroupOutlined
} from '@vicons/antd'

export function useDataList() {
  const { t } = useI18n()

  const renderIcon = (icon: any) => {
    return () => h(NIcon, null, { default: () => h(icon) })
  }

  const localesOptions = [
    {
      label: 'English',
      key: 'en_US'
    },
    {
      label: '中文',
      key: 'zh_CN'
    }
  ]

  const state = reactive({
    isShowSide: false,
    localesOptions,
    userDropdownOptions: [],
    menuOptions: [],
    headerMenuOptions: [],
    sideMenuOptions: []
  })

  const changeMenuOption = (state: any) => {
    state.menuOptions = [
      {
        label: t('menu.home'),
        key: 'home',
        icon: renderIcon(HomeOutlined),
        isShowSide: false
      },
      {
        label: t('menu.project'),
        key: 'projects',
        icon: renderIcon(ProfileOutlined),
        isShowSide: false,
        children: [
          {
            label: t('menu.workflow_monitoring'),
            key: 'workflow-monitoring',
            icon: renderIcon(FundProjectionScreenOutlined)
          },
          {
            label: t('menu.workflow_relationships'),
            key: 'workflow-relationships',
            icon: renderIcon(PartitionOutlined)
          },
          {
            label: t('menu.workflow'),
            key: 'workflow',
            icon: renderIcon(SettingOutlined),
            children: [
              {
                label: t('menu.workflow_definition'),
                key: 'workflow-definition'
              },
              {
                label: t('menu.workflow_instance'),
                key: 'workflow-instance'
              },
              {
                label: t('menu.task_instance'),
                key: 'task-instance'
              },
              {
                label: t('menu.task_definition'),
                key: 'task-definition'
              }
            ]
          }
        ]
      },
      {
        label: t('menu.resources'),
        key: 'resource',
        icon: renderIcon(FolderOutlined),
        isShowSide: true,
        children: [
          {
            label: t('menu.file_manage'),
            key: 'file-manage',
            icon: renderIcon(FileSearchOutlined)
          },
          {
            label: t('menu.udf_manage'),
            key: 'udf-manage',
            icon: renderIcon(RobotOutlined),
            children: [
              {
                label: t('menu.resource_manage'),
                key: 'resource-manage'
              },
              {
                label: t('menu.function_manage'),
                key: 'function-manage'
              }
            ]
          },
          {
            label: t('menu.task_group_manage'),
            key: 'task-group-manage',
            icon: renderIcon(GroupOutlined),
            children: [
              {
                label: t('menu.task_group_option'),
                key: 'task-group-option'
              }
            ]
          }
        ]
      },
      {
        label: t('menu.datasource'),
        key: 'datasource',
        icon: renderIcon(DatabaseOutlined),
        isShowSide: false,
        children: []
      },
      {
        label: t('menu.monitor'),
        key: 'monitor',
        icon: renderIcon(DesktopOutlined),
        isShowSide: true,
        children: [
          {
            label: t('menu.service_manage'),
            key: 'service-manage',
            icon: renderIcon(AppstoreOutlined),
            children: [
              {
                label: t('menu.master'),
                key: 'master'
              },
              {
                label: t('menu.worker'),
                key: 'worker'
              },
              {
                label: t('menu.db'),
                key: 'db'
              }
            ]
          },
          {
            label: t('menu.statistical_manage'),
            key: 'statistical-manage',
            icon: renderIcon(AppstoreOutlined),
            children: [
              {
                label: t('menu.statistics'),
                key: 'statistics'
              }
            ]
          }
        ]
      },
      {
        label: t('menu.security'),
        key: 'security',
        icon: renderIcon(SafetyCertificateOutlined),
        isShowSide: true,
        children: [
          {
            label: t('menu.tenant_manage'),
            key: 'tenant-manage',
            icon: renderIcon(UsergroupAddOutlined)
          },
          {
            label: t('menu.user_manage'),
            key: 'user-manage',
            icon: renderIcon(UserAddOutlined)
          },
          {
            label: t('menu.alarm_group_manage'),
            key: 'alarm-group-manage',
            icon: renderIcon(WarningOutlined)
          },
          {
            label: t('menu.alarm_instance_manage'),
            key: 'alarm-instance-manage',
            icon: renderIcon(InfoCircleOutlined)
          },
          {
            label: t('menu.worker_group_manage'),
            key: 'worker-group-manage',
            icon: renderIcon(ControlOutlined)
          },
          {
            label: t('menu.yarn_queue_manage'),
            key: 'yarn-queue-manage',
            icon: renderIcon(SlackOutlined)
          },
          {
            label: t('menu.environment_manage'),
            key: 'environment-manage',
            icon: renderIcon(EnvironmentOutlined)
          },
          {
            label: t('menu.token_manage'),
            key: 'token-manage',
            icon: renderIcon(SafetyOutlined)
          }
        ]
      }
    ]
  }

  const changeHeaderMenuOptions = (state: any) => {
    state.headerMenuOptions = state.menuOptions.map(
      (item: {
        label: string
        key: string
        icon: any
        isShowSide: boolean
      }) => {
        return {
          label: item.label,
          key: item.key,
          icon: item.icon,
          isShowSide: item.isShowSide
        }
      }
    )
  }

  const changeUserDropdown = (state: any) => {
    state.userDropdownOptions = [
      {
        label: t('userDropdown.profile'),
        key: 'profile',
        icon: renderIcon(UserOutlined)
      },
      {
        label: t('userDropdown.password'),
        key: 'password',
        icon: renderIcon(KeyOutlined)
      },
      {
        label: t('userDropdown.logout'),
        key: 'logout',
        icon: renderIcon(LogoutOutlined)
      }
    ]
  }

  return {
    state,
    changeHeaderMenuOptions,
    changeMenuOption,
    changeUserDropdown
  }
}
