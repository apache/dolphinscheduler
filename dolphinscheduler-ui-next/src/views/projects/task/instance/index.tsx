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
import LogModal from './components/log-modal'
import styles from './index.module.scss'

const TaskInstance = defineComponent({
  name: 'task-instance',
  setup() {
    const { t, variables, getTableData, createColumns } = useTable()

    const requestTableData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal,
        processInstanceId: variables.processInstanceId,
        host: variables.host,
        stateType: variables.stateType,
        datePickerRange: variables.datePickerRange,
        executorName: variables.executorName,
        processInstanceName: variables.processInstanceName
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

    const onConfirmModal = () => {
      variables.showModalRef = false
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
      onSearch,
      onConfirmModal
    }
  },
  render() {
    const { t, requestTableData, onUpdatePageSize, onSearch, onConfirmModal } =
      this

    return (
      <>
        <NCard>
          <NSpace justify='end'>
            <NInput
              v-model={[this.searchVal, 'value']}
              size='small'
              placeholder={t('project.task.task_name')}
              clearable
            />
            <NInput
              v-model={[this.processInstanceName, 'value']}
              size='small'
              placeholder={t('project.task.workflow_instance')}
              clearable
            />
            <NInput
              v-model={[this.executorName, 'value']}
              size='small'
              placeholder={t('project.task.executor')}
              clearable
            />
            <NInput
              v-model={[this.host, 'value']}
              size='small'
              placeholder={t('project.task.host')}
              clearable
            />
            <NSelect
              v-model={[this.stateType, 'value']}
              size='small'
              options={[
                {
                  label: t('project.task.submitted_success'),
                  value: 'SUBMITTED_SUCCESS'
                },
                {
                  label: t('project.task.running_execution'),
                  value: 'RUNNING_EXECUTION'
                },
                { label: t('project.task.ready_pause'), value: 'READY_PAUSE' },
                { label: t('project.task.pause'), value: 'PAUSE' },
                { label: t('project.task.ready_stop'), value: 'READY_STOP' },
                { label: t('project.task.stop'), value: 'STOP' },
                { label: t('project.task.failure'), value: 'FAILURE' },
                { label: t('project.task.success'), value: 'SUCCESS' },
                {
                  label: t('project.task.need_fault_tolerance'),
                  value: 'NEED_FAULT_TOLERANCE'
                },
                { label: t('project.task.kill'), value: 'KILL' },
                {
                  label: t('project.task.waiting_thread'),
                  value: 'WAITING_THREAD'
                },
                {
                  label: t('project.task.waiting_depend'),
                  value: 'WAITING_DEPEND'
                },
                {
                  label: t('project.task.delay_execution'),
                  value: 'DELAY_EXECUTION'
                },
                {
                  label: t('project.task.forced_success'),
                  value: 'FORCED_SUCCESS'
                },
                { label: t('project.task.serial_wait'), value: 'SERIAL_WAIT' }
              ]}
              placeholder={t('project.task.state')}
              style={{ width: '180px' }}
              clearable
            />
            <NDatePicker
              v-model={[this.datePickerRange, 'value']}
              type='datetimerange'
              size='small'
              start-placeholder={t('project.task.start_time')}
              end-placeholder={t('project.task.end_time')}
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
          <NDataTable columns={this.columns} data={this.tableData} />
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
        <LogModal
          showModalRef={this.showModalRef}
          row={this.row}
          onConfirmModal={onConfirmModal}
        />
      </>
    )
  }
})

export default TaskInstance
