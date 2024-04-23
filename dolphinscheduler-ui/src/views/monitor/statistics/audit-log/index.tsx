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
  NPagination,
  NCascader
} from 'naive-ui'
import { SearchOutlined } from '@vicons/antd'
import { useTable } from './use-table'
import { useI18n } from 'vue-i18n'
import Card from '@/components/card'

const AuditLog = defineComponent({
  name: 'audit-log',
  setup() {
    const {
      t,
      variables,
      getTableData,
      createColumns,
      getModelTypeData,
      getOperationTypeData
    } = useTable()

    const requestTableData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        modelType: variables.modelType,
        operationType: variables.operationType,
        userName: variables.userName,
        modelName: variables.modelName,
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
      getModelTypeData()
      getOperationTypeData()
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
              v-model={[this.userName, 'value']}
              size='small'
              placeholder={t('monitor.audit_log.user_name')}
              clearable
            />
            <NInput
              allowInput={this.trim}
              v-model={[this.modelName, 'value']}
              size='small'
              placeholder={t('monitor.audit_log.model_name')}
              clearable
            />
            <NCascader
              v-model={[this.modelType, 'value']}
              multiple
              cascade={false}
              size='small'
              options={this.ModelTypeData}
              placeholder={t('monitor.audit_log.model_type')}
              style={{ width: '180px' }}
              clearable
              filterable
              value-field='name'
              label-field='name'
              children-field='child'
              show-path={false}
              maxTagCount={1}
            />
            <NSelect
              v-model={[this.operationType, 'value']}
              size='small'
              options={this.OperationTypeData}
              placeholder={t('monitor.audit_log.operation_type')}
              style={{ width: '180px' }}
              clearable
              filterable
              value-field='name'
              label-field='name'
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
        <Card title={t('menu.audit_log')}>
          <NSpace vertical>
            <NDataTable
              loading={loadingRef}
              columns={this.columns}
              scrollX={this.tableWidth}
              data={this.tableData}
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

export default AuditLog
