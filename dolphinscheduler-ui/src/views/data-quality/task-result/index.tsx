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
import {
  NSpace,
  NInput,
  NSelect,
  NDatePicker,
  NButton,
  NIcon,
  NDataTable,
  NPagination
} from 'naive-ui'
import { SearchOutlined } from '@vicons/antd'
import { useTable } from './use-table'
import { useI18n } from 'vue-i18n'
import Card from '@/components/card'

const TaskResult = defineComponent({
  name: 'task-result',
  setup() {
    const { t, variables, getTableData, createColumns } = useTable()

    const requestTableData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        ruleType: variables.ruleType,
        state: variables.state,
        searchVal: variables.searchVal,
        datePickerRange: variables.datePickerRange
      })
    }

    const onUpdatePageSize = () => {
      variables.page = 1
      requestTableData()
    }

    const onSearch = () => {
      variables.page = 1
      requestTableData()
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    onMounted(() => {
      createColumns(variables)
      requestTableData()
    })

    watch(useI18n().locale, () => {
      createColumns(variables)
    })

    return {
      t,
      ...toRefs(variables),
      requestTableData,
      onUpdatePageSize,
      onSearch,
      trim
    }
  },
  render() {
    const { t, requestTableData, onUpdatePageSize, onSearch, loadingRef } = this

    return (
      <NSpace vertical>
        <Card>
          <NSpace justify='end'>
            <NInput
              allowInput={this.trim}
              v-model={[this.searchVal, 'value']}
              size='small'
              placeholder={t('data_quality.task_result.task_name')}
              clearable
            />
            <NSelect
              v-model={[this.ruleType, 'value']}
              size='small'
              options={[
                {
                  value: 0,
                  label: t('data_quality.task_result.single_table')
                },
                {
                  value: 1,
                  label: t('data_quality.task_result.single_table_custom_sql')
                },
                {
                  value: 2,
                  label: t('data_quality.task_result.multi_table_accuracy')
                },
                {
                  value: 3,
                  label: t('data_quality.task_result.multi_table_comparison')
                }
              ]}
              placeholder={t('data_quality.task_result.rule_type')}
              style={{ width: '180px' }}
              clearable
            />
            <NSelect
              v-model={[this.state, 'value']}
              size='small'
              options={[
                {
                  value: 0,
                  label: t('data_quality.task_result.undone')
                },
                {
                  value: 1,
                  label: t('data_quality.task_result.success')
                },
                {
                  value: 2,
                  label: t('data_quality.task_result.failure')
                }
              ]}
              placeholder={t('data_quality.task_result.state')}
              style={{ width: '180px' }}
              clearable
            />
            <NDatePicker
              v-model={[this.datePickerRange, 'value']}
              type='datetimerange'
              size='small'
              start-placeholder={t('monitor.audit_log.start_time')}
              end-placeholder={t('monitor.audit_log.end_time')}
              clearable
            />
            <NButton size='small' type='primary' onClick={onSearch}>
              <NIcon>
                <SearchOutlined />
              </NIcon>
            </NButton>
          </NSpace>
        </Card>
        <Card title={t('menu.task_result')}>
          <NSpace vertical>
            <NDataTable
              loading={loadingRef}
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
                onUpdatePage={requestTableData}
                onUpdatePageSize={onUpdatePageSize}
              />
            </NSpace>
          </NSpace>
        </Card>
      </NSpace>
    )
  }
})

export default TaskResult
