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
  getCurrentInstance,
  onMounted,
  toRefs,
  watch
} from 'vue'
import { NButton, NDataTable, NIcon, NPagination, NSpace } from 'naive-ui'
import { SearchOutlined } from '@vicons/antd'
import { useI18n } from 'vue-i18n'
import { useTable } from './use-table'
import EnvironmentModal from './components/environment-modal'
import Card from '@/components/card'
import Search from '@/components/input-search'

const environmentManage = defineComponent({
  name: 'environment-manage',
  setup() {
    const { t } = useI18n()
    const { variables, getTableData, createColumns } = useTable()

    const requestData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal
      })
    }

    const onUpdatePageSize = () => {
      variables.page = 1
      requestData()
    }

    const onSearch = () => {
      variables.page = 1
      requestData()
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
      onCancelModal,
      onConfirmModal,
      onUpdatePageSize,
      handleModalChange,
      onSearch,
      trim
    }
  },
  render() {
    const {
      t,
      requestData,
      onUpdatePageSize,
      onCancelModal,
      onConfirmModal,
      handleModalChange,
      onSearch,
      loadingRef
    } = this

    return (
      <NSpace vertical>
        <Card>
          <NSpace justify='space-between'>
            <NButton
              size='small'
              type='primary'
              onClick={handleModalChange}
              class='btn-create-environment'
            >
              {t('security.environment.create_environment')}
            </NButton>
            <NSpace>
              <Search
                v-model:value={this.searchVal}
                placeholder={t('security.environment.search_tips')}
                onSearch={onSearch}
              />
              <NButton size='small' type='primary' onClick={onSearch}>
                <NIcon>
                  <SearchOutlined />
                </NIcon>
              </NButton>
            </NSpace>
          </NSpace>
        </Card>
        <Card title={t('menu.environment_manage')}>
          <NSpace vertical>
            <NDataTable
              loading={loadingRef}
              row-class-name='items'
              columns={this.columns}
              data={this.tableData}
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
                onUpdatePage={requestData}
                onUpdatePageSize={onUpdatePageSize}
              />
            </NSpace>
          </NSpace>
        </Card>
        <EnvironmentModal
          showModalRef={this.showModalRef}
          statusRef={this.statusRef}
          row={this.row}
          onCancelModal={onCancelModal}
          onConfirmModal={onConfirmModal}
        />
      </NSpace>
    )
  }
})

export default environmentManage
