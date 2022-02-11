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

import { defineComponent, onMounted, toRefs } from 'vue'
import { useI18n } from 'vue-i18n'
import Card from '@/components/card'
import styles from './index.module.scss'
import {
  NButton,
  NDataTable,
  NIcon,
  NInput,
  NPagination,
  NPopconfirm,
  NSpace,
  NTooltip
} from 'naive-ui'
import { SearchOutlined } from '@vicons/antd'
import { useTable } from './use-table'

export default defineComponent({
  name: 'WorkflowInstanceList',
  setup() {
    const { variables, getTableData, batchDeleteInstance } = useTable()

    const requestData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal
      })
    }

    const handleSearch = () => {
      variables.page = 1
      requestData()
    }

    const handleChangePageSize = () => {
      variables.page = 1
      requestData()
    }

    const handleBatchDelete = () => {
      batchDeleteInstance()
    }

    onMounted(() => {
      requestData()
    })

    return {
      requestData,
      handleSearch,
      handleChangePageSize,
      handleBatchDelete,
      ...toRefs(variables)
    }
  },
  render() {
    const { t } = useI18n()

    return (
      <div class={styles.content}>
        <Card class={styles.card}>
          <div class={styles.header}>
            <NSpace></NSpace>
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
        <Card title={t('project.workflow.workflow_definition')}>
          <NDataTable
            rowKey={(row) => row.id}
            columns={this.columns}
            data={this.tableData}
            striped
            size={'small'}
            class={styles.table}
            scrollX={1800}
            v-model:checked-row-keys={this.checkedRowKeys}
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
          <NTooltip>
            {{
              default: () => t('project.workflow.delete'),
              trigger: () => (
                <NButton
                  tag='div'
                  type='primary'
                  disabled={this.checkedRowKeys.length <= 0}
                  style='position: absolute; bottom: 10px; left: 10px;'
                >
                  <NPopconfirm onPositiveClick={this.handleBatchDelete}>
                    {{
                      default: () => t('project.workflow.delete_confirm'),
                      trigger: () => t('project.workflow.delete')
                    }}
                  </NPopconfirm>
                </NButton>
              )
            }}
          </NTooltip>
        </Card>
      </div>
    )
  }
})
