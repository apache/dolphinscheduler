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

import { defineComponent, ref } from 'vue'
import styles from './basicLayout.module.scss'
import { useI18n } from 'vue-i18n'
import { Logo } from './components/Logo';
import 'remixicon/fonts/remixicon.css'
import { NLayout, NLayoutContent, NLayoutSider, NLayoutHeader, NMenu, NDropdown, NButton, NIcon } from 'naive-ui'

// function renderIcon (icon) {
//   return () => h(NIcon, null, { default: () => h(icon) })
// }

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
    key: 'profile'
  },
  {
    label: '退出登录',
    key: 'logout',
  }
]

const menuOptions = [
  {
    label: '且听风吟',
    key: 'hear-the-wind-sing'
  },
  {
    label: '1973年的弹珠玩具',
    key: 'pinball-1973',
    disabled: true,
    children: [
      {
        label: '鼠',
        key: 'rat'
      }
    ]
  },
  {
    label: '寻羊冒险记',
    key: 'a-wild-sheep-chase',
    disabled: true
  },
  {
    label: '舞，舞，舞',
    key: 'dance-dance-dance',
    children: [
      {
        type: 'group',
        label: '人物',
        key: 'people',
        children: [
          {
            label: '叙事者',
            key: 'narrator'
          },
          {
            label: '羊男',
            key: 'sheep-man'
          }
        ]
      },
      {
        label: '饮品',
        key: 'beverage',
        children: [
          {
            label: '威士忌',
            key: 'whisky'
          }
        ]
      },
      {
        label: '食物',
        key: 'food',
        children: [
          {
            label: '三明治',
            key: 'sandwich'
          }
        ]
      },
      {
        label: '过去增多，未来减少',
        key: 'the-past-increases-the-future-recedes'
      }
    ]
  }
]

const Layout = defineComponent({
  name: 'BasicLayout',
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
                  <NButton>语言</NButton>
                </NDropdown>
                <NDropdown inverted={this.inverted} options={ dropDownOptions }>
                  <NButton>用户资料</NButton>
                </NDropdown>
              </div>
            </div>
          </NLayoutHeader>
          <NLayout hasSider>
            <NLayoutSider width={240} inverted={this.inverted} nativeScrollbar={false} show-trigger>
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

export default Layout
