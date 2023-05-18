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

import { defineComponent, onMounted, onUnmounted, toRefs, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  NButton,
  NDataTable,
  NPagination,
  NPopconfirm,
  NTooltip,
  NSpace
} from 'naive-ui'
import { useTable } from './use-table'
import Card from '@/components/card'
import ProcessInstanceCondition from './components/process-instance-condition'
import type { IWorkflowInstanceSearch } from './types'

export default defineComponent({
  name: 'WorkflowInstanceList',
  setup() {
    let setIntervalP: number
    const { variables, createColumns, getTableData, batchDeleteInstance } =
      useTable()

    const requestData = () => {
      getTableData()
    }

    const handleSearch = (params: IWorkflowInstanceSearch) => {
      variables.processDefineCode = params.processDefineCode
      variables.searchVal = params.searchVal
      variables.executorName = params.executorName
      variables.host = params.host
      variables.stateType = params.stateType
      variables.startDate = params.startDate
      variables.endDate = params.endDate
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
      createColumns(variables)
      requestData()

      // Update timing list data
      setIntervalP = setInterval(() => {
        requestData()
      }, 9000)
    })

    watch(useI18n().locale, () => {
      createColumns(variables)
    })

    onUnmounted(() => {
      clearInterval(setIntervalP)
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
    const { loadingRef } = this

    return (
      <NSpace vertical>
        <Card>
          <ProcessInstanceCondition onHandleSearch={this.handleSearch} />
        </Card>
        <Card title={t('project.workflow.workflow_instance')}>
          <NSpace vertical>
            <NDataTable
              loading={loadingRef}
              rowKey={(row) => row.id}
              columns={this.columns}
              data={this.tableData}
              striped
              size={'small'}
              scrollX={this.tableWidth}
              v-model:checked-row-keys={this.checkedRowKeys}
              row-class-name='items-workflow-instances'
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
          <NTooltip>
            {{
              default: () => t('project.workflow.delete'),
              trigger: () => (
                <NButton
                  tag='div'
                  type='primary'
                  disabled={this.checkedRowKeys.length <= 0}
                  style='position: absolute; bottom: 10px; left: 10px;'
                  class='btn-delete-all'
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
      </NSpace>
    )
  }
})
