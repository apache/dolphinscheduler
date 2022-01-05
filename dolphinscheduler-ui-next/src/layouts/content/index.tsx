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

import { defineComponent, ref, watch } from 'vue'
import { NLayout, NLayoutContent, NLayoutHeader } from 'naive-ui'
import NavBar from './components/navbar'
import SideBar from './components/sidebar'
import { useDataList } from './use-dataList'
import { useLanguageStore } from '@/store/language/language'
import { useI18n } from 'vue-i18n'

const Content = defineComponent({
  name: 'Content',
  setup() {
    const languageStore = useLanguageStore()

    watch(useI18n().locale, () => {
      console.log(123)
    })

    return { languageStore }
  },
  render() {
    const { state, getHeaderMenuOptions } = useDataList()
  
    const headerMenuOptions = getHeaderMenuOptions(state.menuOptions)
  
    const sideMenuOptions = ref()
  
    const getSideMenuOptions = (item: any) => {
      this.languageStore.setMenuKey(item.key)
      sideMenuOptions.value =
        state.menuOptions.filter((menu) => menu.key === item.key)[0].children ||
        []
      state.isShowSide = sideMenuOptions.value.length !== 0
    }
    return (
      <NLayout style='height: 100%;'>
        <NLayoutHeader style='height: 65px;'>
          <NavBar
            onHandleMenuClick={getSideMenuOptions}
            headerMenuOptions={headerMenuOptions}
            languageOptions={state.languageOptions}
            profileOptions={state.profileOptions}
          />
        </NLayoutHeader>
        <NLayout has-sider position='absolute' style='top: 65px;'>
          { state.isShowSide && <SideBar sideMenuOptions={sideMenuOptions.value} /> }
          <NLayoutContent native-scrollbar={false} style='padding: 16px 22px;'>
            <router-view />
          </NLayoutContent>
        </NLayout>
      </NLayout>
    )
  }

})

export default Content
