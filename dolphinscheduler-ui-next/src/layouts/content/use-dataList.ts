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
  GroupOutlined,
  ContainerOutlined,
  ApartmentOutlined,
  BarsOutlined
} from '@vicons/antd'
import { useMenuStore } from '@/store/menu/menu'

export function useDataList() {
  const { t } = useI18n()
  const menuStore = useMenuStore()

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
            label: t('menu.project_overview'),
            key: `/projects/${menuStore.getProjectCode}`,
            icon: renderIcon(FundProjectionScreenOutlined)
          },
          {
            label: t('menu.workflow'),
            key: 'workflow',
            icon: renderIcon(PartitionOutlined),
            children: [
              {
                label: t('menu.workflow_relation'),
                key: `/projects/${menuStore.getProjectCode}/workflow/relation`
              },
              {
                label: t('menu.workflow_definition'),
                key: `/projects/${menuStore.getProjectCode}/workflow-definition`
              },
              {
                label: t('menu.workflow_instance'),
                key: `/projects/${menuStore.getProjectCode}/workflow/instances`
              }
            ]
          },
          {
            label: t('menu.task'),
            key: 'task',
            icon: renderIcon(SettingOutlined),
            children: [
              {
                label: t('menu.task_definition'),
                key: `/projects/${menuStore.getProjectCode}/task/definitions`
              },
              {
                label: t('menu.task_instance'),
                key: `/projects/${menuStore.getProjectCode}/task/instances`
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
            key: `/resource/file-manage`,
            icon: renderIcon(FileSearchOutlined)
          },
          {
            label: t('menu.udf_manage'),
            key: 'udf-manage',
            icon: renderIcon(RobotOutlined),
            children: [
              {
                label: t('menu.resource_manage'),
                key: `/resource/resource-manage`
              },
              {
                label: t('menu.function_manage'),
                key: `/resource/function-manage`
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
              },
              {
                label: t('menu.task_group_queue'),
                key: 'task-group-queue'
              }
            ]
          }
        ]
      },
      {
        label: t('menu.data_quality'),
        key: 'data-quality',
        icon: renderIcon(ContainerOutlined),
        isShowSide: true,
        children: [
          {
            label: t('menu.task_result'),
            key: `/data-quality/task-result`,
            icon: renderIcon(ApartmentOutlined)
          },
          {
            label: t('menu.rule'),
            key: `/data-quality/rule`,
            icon: renderIcon(BarsOutlined)
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
                key: `/monitor/master`
              },
              {
                label: t('menu.worker'),
                key: `/monitor/worker`
              },
              {
                label: t('menu.db'),
                key: `/monitor/db`
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
                key: `/monitor/statistics`
              },
              {
                label: t('menu.audit_log'),
                key: `/monitor/audit-log`
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
            key: `/security/tenant-manage`,
            icon: renderIcon(UsergroupAddOutlined)
          },
          {
            label: t('menu.user_manage'),
            key: `/security/user-manage`,
            icon: renderIcon(UserAddOutlined)
          },
          {
            label: t('menu.alarm_group_manage'),
            key: `/security/alarm-group-manage`,
            icon: renderIcon(WarningOutlined)
          },
          {
            label: t('menu.alarm_instance_manage'),
            key: `/security/alarm-instance-manage`,
            icon: renderIcon(InfoCircleOutlined)
          },
          {
            label: t('menu.worker_group_manage'),
            key: `/security/worker-group-manage`,
            icon: renderIcon(ControlOutlined)
          },
          {
            label: t('menu.yarn_queue_manage'),
            key: `/security/yarn-queue-manage`,
            icon: renderIcon(SlackOutlined)
          },
          {
            label: t('menu.environment_manage'),
            key: `/security/environment-manage`,
            icon: renderIcon(EnvironmentOutlined)
          },
          {
            label: t('menu.token_manage'),
            key: `/security/token-manage`,
            icon: renderIcon(SafetyOutlined)
          }
        ]
      }
    ]
  }

  const changeHeaderMenuOptions = (state: any) => {
    state.headerMenuOptions = state.menuOptions.map(
      (item: { label: string; key: string; icon: any }) => {
        return {
          label: item.label,
          key: item.key,
          icon: item.icon
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
