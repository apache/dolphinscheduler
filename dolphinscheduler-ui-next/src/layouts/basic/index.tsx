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

import { h, defineComponent, ref } from 'vue'
import styles from './index.module.scss'
import { useI18n } from 'vue-i18n'
import {
  PersonCircleOutline,
  LogOutOutline,
  FileTrayFullOutline,
  Server,
  SettingsOutline,
} from '@vicons/ionicons5'
import {
  HomeOutlined,
  FolderOutlined,
  SafetyCertificateOutlined,
  UserOutlined,
} from '@vicons/antd'
import { Database, Notes, Users } from '@vicons/tabler'
import { MonitorFilled, AcUnitOutlined } from '@vicons/material'
import { Flow } from '@vicons/carbon'
import { Header } from './components/header'
import { Sider } from './components/sider'
import { NLayout, NLayoutContent, NIcon } from 'naive-ui'

function renderIcon(icon) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const switchLanguageOptions = [
  {
    label: 'English',
    key: 'en',
  },
  {
    label: '中文',
    key: 'zh',
  },
]

const profileOptions = [
  {
    label: '用户信息',
    key: 'profile',
    icon: renderIcon(PersonCircleOutline),
  },
  {
    label: '退出登录',
    key: 'logout',
    icon: renderIcon(LogOutOutline),
  },
]

const menuOptions = [
  {
    label: '首页',
    key: 'home',
    icon: renderIcon(HomeOutlined),
  },
  {
    label: '项目管理',
    key: 'project',
    icon: renderIcon(Notes),
    children: [
      {
        label: '项目',
        key: 'projects-list',
        icon: renderIcon(Notes),
      },
      {
        label: '工作流监控',
        key: 'projects-index',
        icon: renderIcon(Flow),
      },
    ],
  },
  {
    label: '资源中心',
    key: 'resources',
    icon: renderIcon(FolderOutlined),
    children: [
      {
        label: '文件管理',
        key: 'file',
        icon: renderIcon(FileTrayFullOutline),
      },
      {
        label: '创建资源',
        key: 'resource-file-create',
        icon: renderIcon(AcUnitOutlined),
      },
    ],
  },
  {
    label: '数据源中心',
    key: 'datasource',
    icon: renderIcon(Database),
    children: [
      {
        label: '数据源中心',
        key: 'datasource-list',
        icon: renderIcon(Database),
      },
    ],
  },
  {
    label: '监控中心',
    key: 'monitor',
    icon: renderIcon(MonitorFilled),
    children: [
      {
        key: 'servers-master',
        title: '服务管理-Master',
        icon: renderIcon(Server),
      },
      {
        key: 'servers-worker',
        title: '服务管理-Worker',
        icon: renderIcon(SettingsOutline),
      },
    ],
  },
  {
    label: '安全中心',
    key: 'security',
    icon: renderIcon(SafetyCertificateOutlined),
    children: [
      {
        key: 'tenement-manage',
        label: '租户管理',
        icon: renderIcon(UserOutlined),
      },
      {
        key: 'users-manage',
        label: '用户管理',
        icon: renderIcon(Users),
      },
    ],
  },
]

const basic = defineComponent({
  name: 'basic',
  setup() {
    const invertedRef = ref(true)
    const hasSiderRef = ref(false)
    const defaultMenuKeyRef = ref('home')
    const currentMenuRef = ref({})
    const topMenuOptionsRef = ref([])
    const sideMenuOptionsRef = ref([])

    const handleTopMenuClick = (data) => {
      currentMenuRef.value = data
      generateMenus()
    }

    const handleSideMenuClick = (key, data) => {
      console.log(data)
    }

    const generateMenus = () => {
      topMenuOptionsRef.value = []
      sideMenuOptionsRef.value = []
      hasSiderRef.value = false
      menuOptions.forEach((option) => {
        topMenuOptionsRef.value.push({
          label: option.label,
          key: option.key,
          icon: option.icon,
        })
        if (
          currentMenuRef.value.key === option.key ||
          defaultMenuKeyRef.value === option.key
        ) {
          if (option.hasOwnProperty('children') && option.children) {
            sideMenuOptionsRef.value = option.children
            hasSiderRef.value = true
          }
        }
      })
    }
    generateMenus()
    return {
      topMenuOptionsRef,
      sideMenuOptionsRef,
      invertedRef,
      hasSiderRef,
      defaultMenuKeyRef,
      handleTopMenuClick,
      handleSideMenuClick,
    }
  },
  render() {
    return (
      <NLayout class={styles.container}>
        <Header
          languageOptions={switchLanguageOptions}
          profileOptions={profileOptions}
          menuOptions={this.topMenuOptionsRef}
          inverted={this.invertedRef}
          defaultMenuKey={this.defaultMenuKeyRef}
          onMenuClick={this.handleTopMenuClick}
        />
        <NLayout hasSider>
          <Sider
            visible={this.hasSiderRef}
            inverted={this.invertedRef}
            menuOptions={this.sideMenuOptionsRef}
          />
          <NLayoutContent>
            <router-view />
          </NLayoutContent>
        </NLayout>
      </NLayout>
    )
  },
})

export default basic
