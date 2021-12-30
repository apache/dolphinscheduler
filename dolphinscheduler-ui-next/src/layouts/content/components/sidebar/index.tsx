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

import { defineComponent, PropType, ref, watch, onMounted } from 'vue'
import styles from './index.module.scss'
import { NLayoutSider, NMenu } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { useLanguageStore } from '@/store/language/language'

interface Props {
  sideMenuOptions: Array<any>,
  isShowSide: boolean
}

const Sidebar = (props: Props) => {
  // console.log('props', JSON.stringify(props))
  const collapsedRef = ref(false)
  const defaultExpandedKeys = [
    'workflow',
    'udf-manage',
    'service-manage',
    'statistical-manage',
  ]

  watch(useI18n().locale, () => {
    const languageStore = useLanguageStore()
    refreshOptionsRef.value = props.sideMenuOptions
    // console.log(123, JSON.stringify(props))
  })

  const refreshOptionsRef = ref()

  return (
    <NLayoutSider style={{display: props.isShowSide ? 'block' : 'none'}}
      bordered
      nativeScrollbar={false}
      show-trigger='bar'
      collapse-mode='width'
      collapsed={collapsedRef.value}
      onCollapse={() => (collapsedRef.value = true)}
      onExpand={() => (collapsedRef.value = false)}
    >
      <NMenu
        options={props.sideMenuOptions || refreshOptionsRef.value}
        defaultExpandedKeys={defaultExpandedKeys}
      />
    </NLayoutSider>
  )
}

export default Sidebar
