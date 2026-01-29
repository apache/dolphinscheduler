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

import { defineComponent, getCurrentInstance, toRefs } from 'vue'
import { NButton, NIcon, NSpace, NDataTable, NPagination } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { SearchOutlined } from '@vicons/antd'
import { useColumns } from './use-columns'
import { useTable } from './use-table'
import UserDetailModal from './components/user-detail-modal'
import AuthorizeModal from './components/authorize-modal'
import PasswordModal from './components/password-modal'
import Card from '@/components/card'
import Search from '@/components/input-search'

const UsersManage = defineComponent({
  name: 'user-manage',
  setup() {
    const { t } = useI18n()
    const { state, changePage, changePageSize, updateList, onOperationClick } =
      useTable()
    const { columnsRef } = useColumns(onOperationClick)

    const onAddUser = () => {
      state.detailModalShow = true
      state.currentRecord = null
    }
    const onDetailModalCancel = () => {
      state.detailModalShow = false
    }
    const onAuthorizeModalCancel = () => {
      state.authorizeModalShow = false
    }
    const onPasswordModalCancel = () => {
      state.passwordModalShow = false
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    return {
      t,
      columnsRef,
      ...toRefs(state),
      changePage,
      changePageSize,
      onAddUser,
      onUpdatedList: updateList,
      onDetailModalCancel,
      onAuthorizeModalCancel,
      onPasswordModalCancel,
      trim
    }
  },
  render() {
    return (
      <NSpace vertical>
        <Card>
          <NSpace justify='space-between'>
            <NButton
              onClick={this.onAddUser}
              type='primary'
              class='btn-create-user'
              size='small'
            >
              {this.t('security.user.create_user')}
            </NButton>
            <NSpace>
              <Search
                v-model:value={this.searchVal}
                onSearch={this.onUpdatedList}
              />
              <NButton type='primary' size='small' onClick={this.onUpdatedList}>
                <NIcon>
                  <SearchOutlined />
                </NIcon>
              </NButton>
            </NSpace>
          </NSpace>
        </Card>
        <Card title={this.t('menu.user_manage')}>
          <NSpace vertical>
            <NDataTable
              row-class-name='items'
              columns={this.columnsRef.columns}
              data={this.list}
              loading={this.loading}
              scrollX={this.columnsRef.tableWidth}
            />
            <NSpace justify='center'>
              <NPagination
                v-model:page={this.page}
                v-model:page-size={this.pageSize}
                item-count={this.itemCount}
                show-size-picker
                page-sizes={[10, 30, 50]}
                show-quick-jumper
                on-update:page={this.changePage}
                on-update:page-size={this.changePageSize}
              />
            </NSpace>
          </NSpace>
        </Card>
        <UserDetailModal
          show={this.detailModalShow}
          currentRecord={this.currentRecord}
          onCancel={this.onDetailModalCancel}
          onUpdate={this.onUpdatedList}
        />
        <AuthorizeModal
          show={this.authorizeModalShow}
          type={this.authorizeType}
          userId={this.currentRecord?.id}
          onCancel={this.onAuthorizeModalCancel}
        />
        <PasswordModal
          show={this.passwordModalShow}
          currentRecord={this.currentRecord}
          onCancel={this.onPasswordModalCancel}
        />
      </NSpace>
    )
  }
})

export default UsersManage
