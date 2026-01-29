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

import { defineComponent, PropType, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { SettingOutlined } from '@vicons/antd'
import { useI18n } from 'vue-i18n'
import { NMenu, NButton, NIcon } from 'naive-ui'
import styles from './index.module.scss'
import Logo from '../logo'
import Locales from '../locales'
import Timezone from '../timezone'
import User from '../user'
import Theme from '../theme'

const Navbar = defineComponent({
  name: 'Navbar',
  props: {
    headerMenuOptions: {
      type: Array as PropType<any>,
      default: []
    },
    localesOptions: {
      type: Array as PropType<any>,
      default: []
    },
    timezoneOptions: {
      type: Array as PropType<any>,
      default: []
    },
    userDropdownOptions: {
      type: Array as PropType<any>,
      default: []
    }
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const { t } = useI18n()
    const menuKey = ref(route.meta.activeMenu as string)

    const handleMenuClick = (key: string) => {
      router.push({ path: `/${key}` })
    }

    const handleUISettingClick = () => {
      router.push({ path: '/ui-setting' })
    }

    watch(
      () => route.path,
      () => {
        menuKey.value = route.meta.activeMenu as string
      }
    )

    return { handleMenuClick, handleUISettingClick, menuKey, t }
  },
  render() {
    return (
      <div class={styles.container}>
        <Logo />
        <div class={styles.nav}>
          <NMenu
            value={this.menuKey}
            mode='horizontal'
            options={this.headerMenuOptions}
            onUpdateValue={this.handleMenuClick}
          />
        </div>
        <div class={styles.settings}>
          <NButton quaternary onClick={this.handleUISettingClick}>
            {{
              icon: () => (
                <NIcon size='16'>
                  <SettingOutlined />
                </NIcon>
              ),
              default: this.t('menu.ui_setting')
            }}
          </NButton>
          <Theme />
          <Locales localesOptions={this.localesOptions} />
          <Timezone timezoneOptions={this.timezoneOptions} />
          <User userDropdownOptions={this.userDropdownOptions} />
        </div>
      </div>
    )
  }
})

export default Navbar
