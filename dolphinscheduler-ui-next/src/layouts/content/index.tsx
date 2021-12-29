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

import { defineComponent, ref, toRefs } from 'vue'
import { NLayout, NLayoutContent, NLayoutHeader } from 'naive-ui'
import NavBar from './components/navbar'
import SideBar from './components/sidebar'
import { useDataList } from './use-dataList'

const Content = defineComponent({
  name: 'Content',
  setup() {
    const { state, getHeaderMenuOptions } = useDataList()

    const headerMenuOptions = getHeaderMenuOptions(state.menuOptions)

    const sideMenuOptions = ref()

    const getSideMenuOptions = (item: any) => {
      sideMenuOptions.value =
        state.menuOptions.filter((menu) => menu.key === item.key)[0].children ||
        []
      state.isShowSide = sideMenuOptions.value.length !== 0
    }

    return {
      ...toRefs(state),
      headerMenuOptions,
      getSideMenuOptions,
      sideMenuOptions,
    }
  },
  render() {
    return (
      <NLayout style='height: 100%;'>
        <NLayoutHeader style='height: 65px;'>
          <NavBar
            onHandleMenuClick={this.getSideMenuOptions}
            headerMenuOptions={this.headerMenuOptions}
            languageOptions={this.languageOptions}
            profileOptions={this.profileOptions}
          />
        </NLayoutHeader>
        <NLayout has-sider position='absolute' style='top: 65px;'>
          <SideBar
            sideMenuOptions={this.sideMenuOptions}
            isShowSide={this.isShowSide}
          />
          <NLayoutContent native-scrollbar={false} style='padding: 16px 22px;'>
            <router-view />
          </NLayoutContent>
        </NLayout>
      </NLayout>
    )
  },
})

export default Content
