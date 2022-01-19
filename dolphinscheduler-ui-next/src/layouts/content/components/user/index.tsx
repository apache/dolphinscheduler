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

import { defineComponent, PropType } from 'vue'
import { NDropdown, NIcon, NButton } from 'naive-ui'
import { DownOutlined, UserOutlined } from '@vicons/antd'
import { useDropDown } from './use-dropdown'
import { useUserStore } from '@/store/user/user'
import styles from './index.module.scss'
import type { UserInfoRes } from '@/service/modules/users/types'

const User = defineComponent({
  name: 'User',
  props: {
    userDropdownOptions: {
      type: Array as PropType<any>,
      default: []
    }
  },
  setup() {
    const { handleSelect } = useDropDown()
    const userStore = useUserStore()

    return { handleSelect, userStore }
  },
  render() {
    return (
      <NDropdown
        trigger='hover'
        show-arrow
        options={this.userDropdownOptions}
        on-select={this.handleSelect}
      >
        <NButton text>
          <NIcon class={styles.icon}>
            <UserOutlined />
          </NIcon>
          {(this.userStore.getUserInfo as UserInfoRes).userName}
          <NIcon class={styles.icon}>
            <DownOutlined />
          </NIcon>
        </NButton>
      </NDropdown>
    )
  }
})

export default User
