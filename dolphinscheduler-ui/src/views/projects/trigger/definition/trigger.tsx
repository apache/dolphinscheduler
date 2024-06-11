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
import { useRoute } from 'vue-router'
import {
  NButton,
  NDataTable,
  NIcon,
  NInput,
  NPagination,
  NSelect,
  NSpace
} from 'naive-ui'
import { SearchOutlined } from '@vicons/antd'
import { useI18n } from 'vue-i18n'
import { useTable } from './use-table'
import { useTrigger } from './use-trigger'
import { TASK_TYPES_MAP } from '@/store/project/task-type'
import Card from '@/components/card'
import TaskModal from '@/views/projects/trigger/components/node/detail-modal'
import type { INodeData } from './types'
import DependenciesModal from '@/views/projects/components/dependencies/dependencies-modal'

const BatchTriggerDefinition = defineComponent({
  name: 'trigger-definition',
  setup() {
    const route = useRoute()
    const projectCode = Number(route.params.projectCode)
    const { t } = useI18n()

    const {
      trigger,
      onToggleShow,
      onTriggerSave,
      onEditTrigger,
      onInitTrigger
    } = useTrigger(projectCode)

    const { variables, getTableData, createColumns } = useTable(onEditTrigger)

    const requestData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchTaskName: variables.searchTaskName,
        taskType: variables.taskType
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

    const onClearSearchTaskName = () => {
      variables.searchTaskName = null
      onSearch()
    }

    const onClearSearchTaskType = () => {
      variables.taskType = null
      onSearch()
    }

    const onRefresh = () => {
      variables.showVersionModalRef = false
      requestData()
    }
    const onCreate = () => {
      onToggleShow(true)
    }
    const onTaskCancel = () => {
      onToggleShow(false)
      onInitTrigger()
    }
    const onTaskSubmit = async (params: { data: INodeData }) => {
      const result = await onTriggerSave(params.data)
      if (result) {
        onTaskCancel()
        onRefresh()
      }
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
      ...toRefs(trigger),
      onSearch,
      onClearSearchTaskName,
      onClearSearchTaskType,
      requestData,
      onUpdatePageSize,
      onRefresh,
      onCreate,
      onTaskSubmit,
      onTaskCancel,
      projectCode,
      trim
    }
  },
  render() {
    const {
      t,
      onSearch,
      requestData,
      onUpdatePageSize,
      onCreate,
      loadingRef
    } = this

    return (
      <NSpace vertical>
        <Card>
          <NSpace justify='space-between'>
            <NButton size='small' type='primary' onClick={onCreate}>
              {t('project.trigger.create_trigger')}
            </NButton>
            <NSpace>
              <NInput
                allowInput={this.trim}
                size='small'
                clearable
                v-model={[this.searchTaskName, 'value']}
                placeholder={t('project.task.task_name')}
                onClear={this.onClearSearchTaskName}
              />
              <NSelect
                v-model={[this.taskType, 'value']}
                size='small'
                options={Object.keys(TASK_TYPES_MAP).map((item) => {
                  return { value: item, label: item }
                })}
                placeholder={t('project.task.task_type')}
                style={{ width: '180px' }}
                clearable
                onClear={this.onClearSearchTaskType}
              />
              <NButton size='small' type='primary' onClick={onSearch}>
                <NIcon>
                  <SearchOutlined />
                </NIcon>
              </NButton>
            </NSpace>
          </NSpace>
        </Card>
        <Card title={t('project.trigger.trigger_definition')}>
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
                onUpdatePage={requestData}
                onUpdatePageSize={onUpdatePageSize}
              />
            </NSpace>
          </NSpace>
        </Card>
        <TaskModal
          show={this.triggerShow}
          data={this.triggerData}
          onSubmit={this.onTaskSubmit}
          onCancel={this.onTaskCancel}
          projectCode={this.projectCode}
          from={1}
          readonly={this.triggerReadonly}
          saving={this.triggerSaving}
        />
        <DependenciesModal
          v-model:show={this.dependenciesData.showRef}
          v-model:taskLinks={this.dependenciesData.taskLinks}
          required={this.dependenciesData.required}
          content={this.dependenciesData.tip}
          onConfirm={this.dependenciesData.action}
        />
      </NSpace>
    )
  }
})

export default BatchTriggerDefinition
