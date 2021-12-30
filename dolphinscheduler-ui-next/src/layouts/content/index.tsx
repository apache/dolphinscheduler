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

import { ref } from 'vue'
import { NLayout, NLayoutContent, NLayoutHeader } from 'naive-ui'
import NavBar from './components/navbar'
import SideBar from './components/sidebar'
import { useDataList } from './use-dataList'
import { useLanguageStore } from '@/store/language/language'

const Content = () => {

  const { state, getHeaderMenuOptions } = useDataList()

  const headerMenuOptions = getHeaderMenuOptions(state.menuOptions)

  const sideMenuOptions = ref()
  const languageStore = useLanguageStore()

  const getSideMenuOptions = (item: any) => {
    // console.log('item', item)
    languageStore.setMenuKey(item.key)
    sideMenuOptions.value =
      state.menuOptions.filter((menu) => menu.key === item.key)[0].children ||
      []
    state.isShowSide = sideMenuOptions.value.length !== 0
    // console.log('sideMenuOptions.value', sideMenuOptions.value)
    // console.log('state.isShowSide', state.isShowSide)
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
        <SideBar
          sideMenuOptions={sideMenuOptions.value}
          isShowSide={state.isShowSide}
        />
        <NLayoutContent native-scrollbar={false} style='padding: 16px 22px;'>
          <router-view />
        </NLayoutContent>
      </NLayout>
    </NLayout>
  )
}

export default Content
