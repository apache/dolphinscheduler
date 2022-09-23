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

import { defineComponent, getCurrentInstance, onMounted, toRefs, watch } from 'vue'
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
import LogModal from '@/components/log-modal'
import { useAsyncState } from '@vueuse/core'
import { queryLog } from '@/service/modules/log'
import { stateType } from '@/common/common'
import styles from './index.module.scss'
import { useLogTimerStore } from '@/store/logTimer/logTimer'

const TaskInstance = defineComponent({
  name: 'task-instance',
  setup() {
    const logTimerStore = useLogTimerStore()
    const logTimer = logTimerStore.getLogTimer;

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

    const getLogs = (row: any, logTimer: number) => {
      const { state } = useAsyncState(
        queryLog({
          taskInstanceId: Number(row.id),
          limit: variables.limit,
          skipLineNum: variables.skipLineNum
        }).then((res: any) => {
          variables.logRef += res.message || '';
          if (res && res.message !== '') {
            variables.limit += 1000
            variables.skipLineNum += 1000
            getLogs(row, logTimer);
          } else {
            // Automatically refresh the log based on the interval user defined in UI setting
            variables.logLoadingRef = false
            setTimeout(() => {
              variables.logRef = ''
              variables.limit = 1000
              variables.skipLineNum = 0
              variables.logLoadingRef = true
              getLogs(row, logTimer);
            }, logTimer* 1000);
          }
        }).catch((error: string) => {
          console.log(`error occurs ${error}`);
        }),
        {}
      )

      return state
    }

    const refreshLogs = (row: any) => {
      variables.logRef = ''
      variables.limit = 1000
      variables.skipLineNum = 0
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    onMounted(() => {
      createColumns(variables)
      requestTableData()
    })

    watch(useI18n().locale, () => {
      createColumns(variables)
    })

    watch(
      () => variables.showModalRef,
      () => {
        if (variables.showModalRef) {
          // get logs is called 
          getLogs(variables.row, logTimer)
        } else {
          variables.row = {}
          variables.logRef = ''
          variables.logLoadingRef = true
          variables.skipLineNum = 0
          variables.limit = 1000
        }
      }
    )

    return {
      t,
      ...toRefs(variables),
      requestTableData,
      onUpdatePageSize,
      onSearch,
      onConfirmModal,
      refreshLogs,
      trim,
      logTimer
    }
  },
  render() {
    const {
      t,
      requestTableData,
      onUpdatePageSize,
      onSearch,
      onConfirmModal,
      loadingRef,
      refreshLogs
    } = this

    return (
      <>
        <NCard>
          <NSpace justify='end' wrap={false}>
            <NInput
                  allowInput={this.trim}
              v-model={[this.searchVal, 'value']}
              size='small'
              placeholder={t('project.task.task_name')}
              clearable
            />
            <NInput
                  allowInput={this.trim}
              v-model={[this.processInstanceName, 'value']}
              size='small'
              placeholder={t('project.task.workflow_instance')}
              clearable
            />
            <NInput
                  allowInput={this.trim}
              v-model={[this.executorName, 'value']}
              size='small'
              placeholder={t('project.task.executor')}
              clearable
            />
            <NInput
                  allowInput={this.trim}
              v-model={[this.host, 'value']}
              size='small'
              placeholder={t('project.task.host')}
              clearable
            />
            <NSelect
              v-model={[this.stateType, 'value']}
              size='small'
              options={stateType(t).slice(1)}
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
          <NDataTable
            loading={loadingRef}
            columns={this.columns}
            data={this.tableData}
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
              onUpdatePage={requestTableData}
              onUpdatePageSize={onUpdatePageSize}
            />
          </div>
        </Card>
        <LogModal
          showModalRef={this.showModalRef}
          logRef={this.logRef}
          row={this.row}
          logLoadingRef={this.logLoadingRef}
          onConfirmModal={onConfirmModal}
          onRefreshLogs={refreshLogs}
        />
      </>
    )
  }
})

export default TaskInstance
