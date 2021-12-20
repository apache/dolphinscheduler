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
import { useRoute } from 'vue-router'
import { UserAlt } from '@vicons/fa'
import { IosArrowDown } from '@vicons/ionicons4';
import { PersonCircleOutline, LogOutOutline } from '@vicons/ionicons5';
import { HomeOutlined, FolderOutlined, SafetyCertificateOutlined } from '@vicons/antd';
import { Database, Notes } from "@vicons/tabler";
import { MonitorFilled } from "@vicons/material";
import { Logo } from './components/logo';
import { NLayout, NLayoutContent, NLayoutSider, NLayoutHeader, NMenu, NDropdown, NButton, NIcon, NAvatar } from 'naive-ui'

function renderIcon (icon) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const switchLanguageDropDownOptions = [
  {
    label: 'English',
    key: 'en'
  },
  {
    label: '中文',
    key: 'zh'
  },
]

const dropDownOptions = [
  {
    label: '用户信息',
    key: 'profile',
    icon: renderIcon(PersonCircleOutline)
  },
  {
    label: '退出登录',
    key: 'logout',
    icon: renderIcon(LogOutOutline)
  }
]

const menuOptions = [
  {
    label: '首页',
    key: 'home',
    icon: renderIcon(HomeOutlined)
  },
  {
    label: '项目管理',
    key: 'project',
    icon: renderIcon(Notes)
  },
  {
    label: '资源中心',
    key: 'resources',
    icon: renderIcon(FolderOutlined)
  },
  {
    label: '数据源中心',
    key: 'datasource',
    icon: renderIcon(Database)
  },
  {
    label: '监控中心',
    key: 'monitor',
    icon: renderIcon(MonitorFilled)
  },
  {
    label: '安全中心',
    key: 'security',
    icon: renderIcon(SafetyCertificateOutlined)
  }
]

const basic = defineComponent({
  name: 'basic',
  setup() {
    const inverted = ref(true)
    return { inverted }
  },
  render() {
    return (
        <NLayout class={styles.container}>
          <NLayoutHeader class={styles['header-model']} inverted={this.inverted} bordered>
            <Logo/>
            <div class={styles.nav}>
              <NMenu mode='horizontal' class={styles.menu} inverted={this.inverted} options={menuOptions}/>
              <div class={styles.profile}>
                <NDropdown inverted={this.inverted} options={ switchLanguageDropDownOptions }>
                  <span>
                   中文<NIcon class={styles.icon}><IosArrowDown/></NIcon>
                  </span>
                </NDropdown>
                <NDropdown inverted={this.inverted} options={ dropDownOptions }>
                  <span>
                    <NIcon class={styles.icon}><UserAlt/></NIcon>
                    admin
                    <NIcon class={styles.icon}><IosArrowDown/></NIcon>
                  </span>
                </NDropdown>
              </div>
            </div>
          </NLayoutHeader>
          <NLayout hasSider>
            <NLayoutSider
                width={240}
                collapseMode={'width'}
                collapsedWidth={64}
                inverted={this.inverted}
                nativeScrollbar={false}
                show-trigger
                bordered>
              <NMenu
                  inverted={this.inverted}
                  collapsedWidth={64}
                  collapsedIconSize={22}
                  options={ menuOptions }
            />
            </NLayoutSider>
            <NLayoutContent>
              <router-view />
            </NLayoutContent>
          </NLayout>
        </NLayout>
    )
  },
})

export default basic
