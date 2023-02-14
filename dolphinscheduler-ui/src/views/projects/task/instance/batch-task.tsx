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
import { useAsyncState } from '@vueuse/core'
import { queryLog } from '@/service/modules/log'
import { stateType } from '@/common/common'
import { useUISettingStore } from '@/store/ui-setting/ui-setting'
import Card from '@/components/card'
import LogModal from '@/components/log-modal'

const BatchTaskInstance = defineComponent({
  name: 'task-instance',
  setup() {
    const uiSettingStore = useUISettingStore()
    const logTimer = uiSettingStore.getLogTimer
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

    const onClearSearchTaskName = () => {
      variables.searchVal = ''
      onSearch()
    }

    const onClearSearchProcessInstanceName = () => {
      variables.processInstanceName = null
      onSearch()
    }

    const onClearSearchExecutorName = () => {
      variables.executorName = null
      onSearch()
    }

    const onClearSearchHost = () => {
      variables.host = null
      onSearch()
    }

    const onClearSearchStateType = () => {
      variables.stateType = null
      onSearch()
    }

    const onClearSearchTime = () => {
      variables.datePickerRange = null
      onSearch()
    }

    const onConfirmModal = () => {
      variables.showModalRef = false
    }

    let getLogsID: number

    const getLogs = (row: any, logTimer: number) => {
      const { state } = useAsyncState(
        queryLog({
          taskInstanceId: Number(row.id),
          limit: variables.limit,
          skipLineNum: variables.skipLineNum
        }).then((res: any) => {
          variables.logRef += res.message || ''
          if (res && res.message !== '') {
            variables.limit += 1000
            variables.skipLineNum += res.lineNum
            getLogs(row, logTimer)
          } else {
            variables.logLoadingRef = false
            if (logTimer !== 0) {
              if (typeof getLogsID === 'number') {
                clearTimeout(getLogsID)
              }
              getLogsID = setTimeout(() => {
                variables.logRef = ''
                variables.limit = 1000
                variables.skipLineNum = 0
                variables.logLoadingRef = true
                getLogs(row, logTimer)
              }, logTimer * 1000)
            }
          }
        }),
        {}
      )

      return state
    }

    const refreshLogs = (row: any) => {
      variables.logRef = ''
      variables.limit = 1000
      variables.skipLineNum = 0
      getLogs(row, logTimer)
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
      onClearSearchTaskName,
      onClearSearchProcessInstanceName,
      onClearSearchExecutorName,
      onClearSearchHost,
      onClearSearchStateType,
      onClearSearchTime,
      onConfirmModal,
      refreshLogs,
      trim
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
      <NSpace vertical>
        <Card>
          <NSpace justify='end' wrap={false}>
            <NInput
              allowInput={this.trim}
              v-model={[this.searchVal, 'value']}
              size='small'
              placeholder={t('project.task.task_name')}
              clearable
              onClear={this.onClearSearchTaskName}
            />
            <NInput
              allowInput={this.trim}
              v-model={[this.processInstanceName, 'value']}
              size='small'
              placeholder={t('project.task.workflow_instance')}
              clearable
              onClear={this.onClearSearchProcessInstanceName}
            />
            <NInput
              allowInput={this.trim}
              v-model={[this.executorName, 'value']}
              size='small'
              placeholder={t('project.task.executor')}
              clearable
              onClear={this.onClearSearchExecutorName}
            />
            <NInput
              allowInput={this.trim}
              v-model={[this.host, 'value']}
              size='small'
              placeholder={t('project.task.host')}
              clearable
              onClear={this.onClearSearchHost}
            />
            <NSelect
              v-model={[this.stateType, 'value']}
              size='small'
              options={stateType(t).slice(1)}
              placeholder={t('project.task.state')}
              style={{ width: '180px' }}
              clearable
              onClear={this.onClearSearchStateType}
            />
            <NDatePicker
              v-model={[this.datePickerRange, 'value']}
              type='datetimerange'
              size='small'
              start-placeholder={t('project.task.start_time')}
              end-placeholder={t('project.task.end_time')}
              clearable
              onClear={this.onClearSearchTime}
            />
            <NButton size='small' type='primary' onClick={onSearch}>
              <NIcon>
                <SearchOutlined />
              </NIcon>
            </NButton>
          </NSpace>
        </Card>
        <Card title={t('project.task.batch_task')}>
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
        <LogModal
          showModalRef={this.showModalRef}
          logRef={this.logRef}
          row={this.row}
          logLoadingRef={this.logLoadingRef}
          onConfirmModal={onConfirmModal}
          onRefreshLogs={refreshLogs}
        />
      </NSpace>
    )
  }
})

export default BatchTaskInstance
