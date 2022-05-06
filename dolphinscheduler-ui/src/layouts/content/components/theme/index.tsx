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
import { NButton } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { useThemeStore } from '@/store/theme/theme'
import styles from './index.module.scss'

const Theme = defineComponent({
  name: 'Theme',
  setup() {
    const { t } = useI18n()
    const themeStore = useThemeStore()

    return { t, themeStore }
  },
  render() {
    return (
      <NButton
        class={styles.theme}
        quaternary
        onClick={() => (this.themeStore.darkTheme = !this.themeStore.darkTheme)}
      >
        {this.t(this.themeStore.darkTheme ? 'theme.light' : 'theme.dark')}
      </NButton>
    )
  }
})

export default Theme
