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

import { defineComponent, toRefs, onMounted, watch } from 'vue'
import {
  NButton,
  NInput,
  NIcon,
  NDataTable,
  NPagination,
  NCard,
  NSpace
} from 'naive-ui'
import styles from './index.module.scss'
import { useTable } from './use-table'
import { SearchOutlined } from '@vicons/antd'
import TenantModal from './components/tenant-modal'
import { useI18n } from 'vue-i18n'
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
      handleChangePageSize
    }
  },
  render() {
    const { t, loadingRef } = this
    return (
      <div class={styles.container}>
        <NCard>
          <div class={styles.header}>
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
          </div>
        </NCard>
        <Card
          title={t('security.tenant.tenant_manage')}
          class={styles['table-card']}
        >
          <NDataTable
            loading={loadingRef}
            columns={this.columns}
            data={this.tableData}
            row-class-name='items'
            scrollX={this.tableWidth}
          />
          <div class={styles.pagination}>
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
          </div>
        </Card>
        <TenantModal
          showModalRef={this.showModalRef}
          statusRef={this.statusRef}
          row={this.row}
          onCancelModal={this.onCancelModal}
          onConfirmModal={this.onConfirmModal}
        />
      </div>
    )
  }
})

export default tenementManage
