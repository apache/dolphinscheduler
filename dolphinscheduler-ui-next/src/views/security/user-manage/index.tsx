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

import { defineComponent, provide } from 'vue'
import {
  NCard,
  NButton,
  NInputGroup,
  NInput,
  NIcon,
  NSpace,
  NGrid,
  NGridItem,
  NDataTable,
  NPagination,
  NSkeleton
} from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { SearchOutlined } from '@vicons/antd'
import { useTable } from './use-table'
import UserModal from './components/user-modal'
import {
  useSharedUserModalState,
  UserModalSharedStateKey,
  Mode
} from './components/use-modal'

const UsersManage = defineComponent({
  name: 'user-manage',
  setup() {
    const { t } = useI18n()
    const { show, mode, user } = useSharedUserModalState()
    const tableState = useTable({
      onEdit: (u) => {
        show.value = true
        mode.value = 'edit'
        user.value = u
      },
      onDelete: (u) => {
        show.value = true
        mode.value = 'delete'
        user.value = u
      }
    })

    const onSuccess = (mode: Mode) => {
      if (mode === 'add') {
        tableState.resetPage()
      }
      tableState.getUserList()
    }

    const onAddUser = () => {
      show.value = true
      mode.value = 'add'
      user.value = undefined
    }

    provide(UserModalSharedStateKey, { show, mode, user, onSuccess })

    return {
      t,
      onAddUser,
      ...tableState
    }
  },
  render() {
    const { t, onSearchValOk, onSearchValClear, userListLoading } = this
    return (
      <>
        <NGrid cols={1} yGap={16}>
          <NGridItem>
            <NCard>
              <NSpace justify='space-between'>
                <NButton onClick={this.onAddUser} type='primary'>
                  {t('security.user.create_user')}
                </NButton>
                <NInputGroup>
                  <NInput
                    v-model:value={this.searchInputVal}
                    clearable
                    onClear={onSearchValClear}
                    onKeyup={(e) => {
                      if (e.key === 'Enter') {
                        onSearchValOk()
                      }
                    }}
                  />
                  <NButton type='primary' onClick={onSearchValOk}>
                    <NIcon>
                      <SearchOutlined />
                    </NIcon>
                  </NButton>
                </NInputGroup>
              </NSpace>
            </NCard>
          </NGridItem>
          <NGridItem>
            <NCard>
              {userListLoading ? (
                <NSkeleton text repeat={6}></NSkeleton>
              ) : (
                <NSpace v-show={!userListLoading} vertical size={20}>
                  <NDataTable
                    columns={this.columns}
                    data={this.userList}
                    scrollX={this.scrollX}
                    bordered={false}
                  />
                  <NSpace justify='center'>
                    <NPagination
                      v-model:page={this.page}
                      v-model:page-size={this.pageSize}
                      pageCount={this.pageCount}
                      pageSizes={this.pageSizes}
                      showSizePicker
                    />
                  </NSpace>
                </NSpace>
              )}
            </NCard>
          </NGridItem>
        </NGrid>
        <UserModal />
      </>
    )
  }
})

export default UsersManage
