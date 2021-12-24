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

import { defineComponent, computed } from 'vue'
import { NConfigProvider, darkTheme, GlobalThemeOverrides } from 'naive-ui'
import { useThemeStore } from '@/store/theme/theme'
import themeList from '@/themes'

const App = defineComponent({
  name: 'App',
  setup() {
    const themeStore = useThemeStore()
    const currentTheme = computed(() =>
      themeStore.darkTheme ? darkTheme : undefined
    )
    return {
      currentTheme,
    }
  },
  render() {
    const themeOverrides: GlobalThemeOverrides =
      themeList[this.currentTheme ? 'dark' : 'light']

    return (
      <NConfigProvider
        theme={this.currentTheme}
        themeOverrides={themeOverrides}
        style={{ width: '100%', height: '100vh', overflow: 'hidden' }}
      >
        <router-view />
      </NConfigProvider>
    )
  },
})

export default App
