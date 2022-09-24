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

import {
  defineComponent,
  toRefs,
  onMounted,
  watch,
  getCurrentInstance
} from 'vue'
import {
  NButton,
  NInput,
  NIcon,
  NDataTable,
  NPagination,
  NSpace
} from 'naive-ui'
import { useTable } from './use-table'
import { SearchOutlined } from '@vicons/antd'
import { useI18n } from 'vue-i18n'
import TenantModal from './components/tenant-modal'
import Card from '@/components/card'

const tenementManage = defineComponent({
  name: 'tenement-manage',
  setup() {
    const { variables, getTableData, createColumns } = useTable()
    const { t } = useI18n()

    const requestData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal
      })
    }

    const handleModalChange = () => {
      variables.showModalRef = true
      variables.statusRef = 0
    }

    const onCancelModal = () => {
      variables.showModalRef = false
    }

    const onConfirmModal = () => {
      variables.showModalRef = false
      requestData()
    }

    const handleChangePageSize = () => {
      variables.page = 1
      requestData()
    }

    const handleSearch = () => {
      variables.page = 1
      requestData()
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    onMounted(() => {
      createColumns(variables)
      requestData()
    })

    watch(useI18n().locale, () => {
      createColumns(variables)
    })

    return {
      t,
      ...toRefs(variables),
      requestData,
      handleModalChange,
      onCancelModal,
      onConfirmModal,
      handleSearch,
      handleChangePageSize,
      trim
    }
  },
  render() {
    const { t, loadingRef } = this
    return (
      <NSpace vertical>
        <Card>
          <NSpace justify='space-between'>
            <NButton
              size='small'
              onClick={this.handleModalChange}
              type='primary'
              class='btn-create-tenant'
            >
              {t('security.tenant.create_tenant')}
            </NButton>
            <NSpace>
              <NInput
                allowInput={this.trim}
                size='small'
                v-model={[this.searchVal, 'value']}
                placeholder={t('security.tenant.search_tips')}
                clearable
              />
              <NButton size='small' type='primary' onClick={this.handleSearch}>
                <NIcon>
                  <SearchOutlined />
                </NIcon>
              </NButton>
            </NSpace>
          </NSpace>
        </Card>
        <Card title={t('menu.tenant_manage')}>
          <NSpace vertical>
            <NDataTable
              loading={loadingRef}
              columns={this.columns}
              data={this.tableData}
              row-class-name='items'
              scrollX={this.tableWidth}
            />
            <NSpace justify='center'>
              <NPagination
                v-model:page={this.page}
                v-model:page-size={this.pageSize}
                page-count={this.totalPage}
                show-size-picker
                page-sizes={[10, 30, 50]}
                show-quick-jumper
                onUpdatePage={this.requestData}
                onUpdatePageSize={this.handleChangePageSize}
              />
            </NSpace>
          </NSpace>
        </Card>
        <TenantModal
          showModalRef={this.showModalRef}
          statusRef={this.statusRef}
          row={this.row}
          onCancelModal={this.onCancelModal}
          onConfirmModal={this.onConfirmModal}
        />
      </NSpace>
    )
  }
})

export default tenementManage
