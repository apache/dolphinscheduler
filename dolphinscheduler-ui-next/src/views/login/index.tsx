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

import { defineComponent } from 'vue'
import styles from './index.module.scss'
import { useI18n } from 'vue-i18n'
import { NButton } from 'naive-ui'
import { useThemeStore } from '@/store/theme/theme'

const Login = defineComponent({
  name: 'login',
  setup() {
    const { t, locale } = useI18n()
    const themeStore = useThemeStore()

    const setTheme = (): void => {
      themeStore.setDarkTheme()
    }

    return { t, locale, setTheme }
  },
  render() {
    return (
      <div class={styles.container}>
        <NButton type='error' onClick={this.setTheme}>
          {this.t('login.test')} + 切换主题
        </NButton>
        <select v-model={this.locale}>
          <option value='en_US'>en_US</option>
          <option value='zh_CN'>zh_CN</option>
        </select>
      </div>
    )
  },
})

export default Login
