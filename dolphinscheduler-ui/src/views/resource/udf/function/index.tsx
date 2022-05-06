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

import { defineComponent, Ref, toRefs, onMounted, toRef, watch } from 'vue'
import {
  NIcon,
  NSpace,
  NDataTable,
  NButton,
  NPagination,
  NInput
} from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { SearchOutlined } from '@vicons/antd'
import Card from '@/components/card'
import FolderModal from './components/function-modal'
import { useTable } from './use-table'
import styles from './index.module.scss'

export default defineComponent({
  name: 'function-manage',
  setup() {
    const { variables, createColumns, getTableData } = useTable()

    const requestData = () => {
      getTableData({
        id: variables.id,
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal
      })
    }

    const handleUpdateList = () => {
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

    const handleShowModal = (showRef: Ref<Boolean>) => {
      showRef.value = true
    }

    const handleCreateFolder = () => {
      variables.row = {}
      handleShowModal(toRef(variables, 'showRef'))
    }

    watch(useI18n().locale, () => {
      createColumns(variables)
    })

    onMounted(() => {
      createColumns(variables)
      requestData()
    })

    return {
      requestData,
      handleSearch,
      handleUpdateList,
      handleCreateFolder,
      handleChangePageSize,
      ...toRefs(variables)
    }
  },
  render() {
    const { t } = useI18n()
    const { loadingRef } = this

    return (
      <div class={styles.content}>
        <Card class={styles.card}>
          <div class={styles.header}>
            <NSpace>
              <NButton
                type='primary'
                onClick={this.handleCreateFolder}
                class='btn-create-udf-function'
              >
                {t('resource.function.create_udf_function')}
              </NButton>
            </NSpace>
            <div class={styles.right}>
              <div class={styles.search}>
                <div class={styles.list}>
                  <NButton type='primary' onClick={this.handleSearch}>
                    <NIcon>
                      <SearchOutlined />
                    </NIcon>
                  </NButton>
                </div>
                <div class={styles.list}>
                  <NInput
                    placeholder={t('resource.function.enter_keyword_tips')}
                    v-model={[this.searchVal, 'value']}
                  />
                </div>
              </div>
            </div>
          </div>
        </Card>
        <Card title={t('resource.function.udf_function')}>
          <NDataTable
            loading={loadingRef}
            columns={this.columns}
            data={this.tableData}
            striped
            size={'small'}
            class={styles.table}
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
        <FolderModal
          v-model:row={this.row}
          v-model:show={this.showRef}
          onUpdateList={this.handleUpdateList}
        />
      </div>
    )
  }
})
