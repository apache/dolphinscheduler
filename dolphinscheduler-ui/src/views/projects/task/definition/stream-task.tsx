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

import { useI18n } from 'vue-i18n'
import { SearchOutlined } from '@vicons/antd'
import { defineComponent, getCurrentInstance, watch, onMounted } from 'vue'
import {
  NInput,
  NButton,
  NIcon,
  NSpace,
  NDataTable,
  NPagination
} from 'naive-ui'
import { useRoute } from 'vue-router'
import { useTable } from './use-stream-table'
import { useTask } from './use-task'
import StartModal from './components/start-modal'
import Card from '@/components/card'
import TaskModal from '@/views/projects/task/components/node/detail-modal'
import type { INodeData } from './types'

const StreamTaskDefinition = defineComponent({
  name: 'stream-task-definition',
  setup() {
    const { t } = useI18n()
    const route = useRoute()
    const projectCode = Number(route.params.projectCode)

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim
    const { task, onToggleShow, onEditTask, onInitTask, onUpdateTask } =
      useTask(projectCode)
    const { variables, getTableData, createColumns } = useTable(onEditTask)

    const onSearch = () => {
      variables.page = 1
      getTableData()
    }

    const onRefresh = () => {
      getTableData()
    }

    const onUpdatePageSize = () => {
      variables.page = 1
      getTableData()
    }

    const onTaskCancel = () => {
      onToggleShow(false)
      onInitTask()
    }

    const onTaskSubmit = async (params: { data: INodeData }) => {
      const result = await onUpdateTask(params.data)
      if (result) {
        onTaskCancel()
        onRefresh()
      }
    }

    onMounted(() => {
      createColumns(variables)
      getTableData()
    })

    watch(useI18n().locale, () => {
      createColumns(variables)
    })

    return () => (
      <NSpace vertical>
        <Card>
          <NSpace justify='end'>
            <NInput
              allowInput={trim}
              size='small'
              clearable
              v-model={[variables.searchTaskName, 'value']}
              placeholder={t('project.task.task_name')}
            />
            <NInput
              allowInput={trim}
              size='small'
              clearable
              v-model={[variables.searchWorkflowName, 'value']}
              placeholder={t('project.task.workflow_name')}
            />
            <NButton size='small' type='primary' onClick={onSearch}>
              <NIcon>
                <SearchOutlined />
              </NIcon>
            </NButton>
          </NSpace>
        </Card>
        <Card>
          <NSpace vertical>
            <NDataTable
              loading={variables.loading}
              columns={variables.columns}
              data={variables.tableData}
              scrollX={variables.tableWidth}
            />
            <NSpace justify='center'>
              <NPagination
                v-model:page={variables.page}
                v-model:page-size={variables.pageSize}
                page-count={variables.totalPage}
                show-size-picker
                page-sizes={[10, 30, 50]}
                show-quick-jumper
                onUpdatePage={getTableData}
                onUpdatePageSize={onUpdatePageSize}
              />
            </NSpace>
          </NSpace>
        </Card>
        <TaskModal
          show={task.taskShow}
          data={task.taskData}
          onSubmit={onTaskSubmit}
          onCancel={onTaskCancel}
          projectCode={projectCode}
          from={1}
          readonly={task.taskReadonly}
          saving={task.taskSaving}
        />
        <StartModal
          v-model:row={variables.row}
          v-model:show={variables.startShow}
          onUpdateList={getTableData}
        />
      </NSpace>
    )
  }
})

export default StreamTaskDefinition
