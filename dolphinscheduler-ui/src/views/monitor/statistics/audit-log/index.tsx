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

import { defineComponent, onMounted, toRefs, watch } from 'vue'
import {
  NSpace,
  NInput,
  NSelect,
  NDatePicker,
  NButton,
  NIcon,
  NDataTable,
  NPagination,
  NCard
} from 'naive-ui'
import { SearchOutlined } from '@vicons/antd'
import { useTable } from './use-table'
import { useI18n } from 'vue-i18n'
import Card from '@/components/card'
import styles from './index.module.scss'

const AuditLog = defineComponent({
  name: 'audit-log',
  setup() {
    const { t, variables, getTableData, createColumns } = useTable()

    const requestTableData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        resourceType: variables.resourceType,
        operationType: variables.operationType,
        userName: variables.userName,
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
      onSearch
    }
  },
  render() {
    const { t, requestTableData, onUpdatePageSize, onSearch, loadingRef } = this

    return (
      <>
        <NCard>
          <NSpace justify='end'>
            <NInput
              v-model={[this.userName, 'value']}
              size='small'
              placeholder={t('monitor.audit_log.user_name')}
              clearable
            />
            <NSelect
              v-model={[this.operationType, 'value']}
              size='small'
              options={[
                { value: 'CREATE', label: t('monitor.audit_log.create') },
                { value: 'UPDATE', label: t('monitor.audit_log.update') },
                { value: 'DELETE', label: t('monitor.audit_log.delete') },
                { value: 'READ', label: t('monitor.audit_log.read') }
              ]}
              placeholder={t('monitor.audit_log.operation_type')}
              style={{ width: '180px' }}
              clearable
            />
            <NSelect
              v-model={[this.resourceType, 'value']}
              size='small'
              options={[
                {
                  value: 'USER_MODULE',
                  label: t('monitor.audit_log.user_audit')
                },
                {
                  value: 'PROJECT_MODULE',
                  label: t('monitor.audit_log.project_audit')
                }
              ]}
              placeholder={t('monitor.audit_log.resource_type')}
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
              {{
                icon: () => (
                  <NIcon>
                    <SearchOutlined />
                  </NIcon>
                )
              }}
            </NButton>
          </NSpace>
        </NCard>
        <Card class={styles['table-card']}>
          <NDataTable
            loading={loadingRef}
            columns={this.columns}
            data={this.tableData}
          />
          <div class={styles.pagination}>
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
          </div>
        </Card>
      </>
    )
  }
})

export default AuditLog
