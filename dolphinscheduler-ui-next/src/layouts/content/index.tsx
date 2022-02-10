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

import { defineComponent, onMounted, watch, toRefs, ref } from 'vue'
import { NLayout, NLayoutContent, NLayoutHeader, useMessage } from 'naive-ui'
import NavBar from './components/navbar'
import SideBar from './components/sidebar'
import { useDataList } from './use-dataList'
import { useMenuStore } from '@/store/menu/menu'
import { useLocalesStore } from '@/store/locales/locales'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'

const Content = defineComponent({
  name: 'Content',
  setup() {
    window.$message = useMessage()

    const route = useRoute()
    const menuStore = useMenuStore()
    const { locale } = useI18n()
    const localesStore = useLocalesStore()
    const {
      state,
      changeMenuOption,
      changeHeaderMenuOptions,
      changeUserDropdown
    } = useDataList()
    const sideKeyRef = ref()

    locale.value = localesStore.getLocales

    onMounted(() => {
      changeMenuOption(state)
      changeHeaderMenuOptions(state)
      getSideMenu(state)
      changeUserDropdown(state)
    })

    watch(useI18n().locale, () => {
      changeMenuOption(state)
      changeHeaderMenuOptions(state)
      getSideMenu(state)
      changeUserDropdown(state)
    })

    const getSideMenu = (state: any) => {
      const key = menuStore.getMenuKey
      state.sideMenuOptions =
        state.menuOptions.filter((menu: { key: string }) => menu.key === key)[0]
          ?.children || state.menuOptions
      state.isShowSide = menuStore.getShowSideStatus
    }

    const getSideMenuOptions = (item: any) => {
      menuStore.setMenuKey(item.key)
      getSideMenu(state)
    }

    watch(
      () => route.path,
      () => {
        if (route.path !== '/login') {
          state.isShowSide = menuStore.getShowSideStatus
          route.matched[1].path.includes(':projectCode')
          if (route.matched[1].path === '/projects/:projectCode') {
            changeMenuOption(state)
            getSideMenu(state)
          }
          sideKeyRef.value = route.matched[1].path.includes(':projectCode')
            ? route.matched[1].path.replace(
                ':projectCode',
                menuStore.getProjectCode
              )
            : route.matched[1].path
        }
      },
      { immediate: true }
    )

    return {
      ...toRefs(state),
      menuStore,
      changeMenuOption,
      getSideMenuOptions,
      sideKeyRef
    }
  },
  render() {
    return (
      <NLayout style='height: 100%'>
        <NLayoutHeader style='height: 65px'>
          <NavBar
            onHandleMenuClick={this.getSideMenuOptions}
            headerMenuOptions={this.headerMenuOptions}
            localesOptions={this.localesOptions}
            userDropdownOptions={this.userDropdownOptions}
          />
        </NLayoutHeader>
        <NLayout has-sider position='absolute' style='top: 65px'>
          {this.isShowSide && (
            <SideBar
              sideMenuOptions={this.sideMenuOptions}
              sideKey={this.sideKeyRef}
            />
          )}
          <NLayoutContent native-scrollbar={false} style='padding: 16px 22px'>
            <router-view key={this.$route.fullPath} />
          </NLayoutContent>
        </NLayout>
      </NLayout>
    )
  }
})

export default Content
