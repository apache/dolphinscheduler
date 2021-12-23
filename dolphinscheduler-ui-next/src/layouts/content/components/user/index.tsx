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

import { defineComponent, toRefs } from 'vue'
import { NDropdown, NIcon, NButton } from 'naive-ui'
import styles from './index.module.scss'
import { DownOutlined, UserOutlined } from '@vicons/antd'
import { useDataList } from './use-dataList'
import { useDropDown } from './use-dropdown'

const user = defineComponent({
  name: 'user',
  setup() {
    const { state } = useDataList()
    const { handleSelect } = useDropDown()
    return { ...toRefs(state), handleSelect }
  },
  render() {
    return (
      <NDropdown
        trigger='hover'
        show-arrow
        options={this.profileOptions}
        on-select={this.handleSelect}
      >
        <NButton text>
          <NIcon class={styles.icon}>
            <UserOutlined />
          </NIcon>
          admin
          <NIcon class={styles.icon}>
            <DownOutlined />
          </NIcon>
        </NButton>
      </NDropdown>
    )
  },
})

export default user
