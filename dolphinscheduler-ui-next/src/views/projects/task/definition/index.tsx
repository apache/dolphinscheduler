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
import { useRoute } from 'vue-router'
import {
  NButton,
  NCard,
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
import { useTask } from './use-task'
import { TASK_TYPES_MAP } from '@/views/projects/task/constants/task-type'
import Card from '@/components/card'
import VersionModal from './components/version-modal'
import MoveModal from './components/move-modal'
import TaskModal from '@/views/projects/task/components/node/detail-modal'
import styles from './index.module.scss'
import type { INodeData } from './types'

const TaskDefinition = defineComponent({
  name: 'task-definition',
  setup() {
    const route = useRoute()
    const projectCode = Number(route.params.projectCode)
    const { t } = useI18n()

    const { task, onToggleShow, onTaskSave, onEditTask, onInitTask } =
      useTask(projectCode)

    const { variables, getTableData, createColumns } = useTable(onEditTask)

    const requestData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchTaskName: variables.searchTaskName,
        searchWorkflowName: variables.searchWorkflowName,
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

    const onRefresh = () => {
      variables.showVersionModalRef = false
      variables.showMoveModalRef = false
      requestData()
    }
    const onCreate = () => {
      onToggleShow(true)
    }
    const onTaskCancel = () => {
      onToggleShow(false)
      onInitTask()
    }
    const onTaskSubmit = async (params: { data: INodeData }) => {
      const result = await onTaskSave(params.data)
      if (result) {
        onTaskCancel()
        onRefresh()
      }
    }
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
      ...toRefs(task),
      onSearch,
      requestData,
      onUpdatePageSize,
      onRefresh,
      onCreate,
      onTaskSubmit,
      onTaskCancel,
      projectCode
    }
  },
  render() {
    const {
      t,
      onSearch,
      requestData,
      onUpdatePageSize,
      onRefresh,
      onCreate,
      loadingRef
    } = this

    return (
      <>
        <NCard>
          <div class={styles['search-card']}>
            <div>
              <NButton size='small' type='primary' onClick={onCreate}>
                {t('project.task.create_task')}
              </NButton>
            </div>
            <NSpace justify='end'>
              <NInput
                size='small'
                clearable
                v-model={[this.searchTaskName, 'value']}
                placeholder={t('project.task.task_name')}
              />
              <NInput
                size='small'
                clearable
                v-model={[this.searchWorkflowName, 'value']}
                placeholder={t('project.task.workflow_name')}
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
          </div>
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
              onUpdatePage={requestData}
              onUpdatePageSize={onUpdatePageSize}
            />
          </div>
        </Card>
        <VersionModal
          show={this.showVersionModalRef}
          row={this.row}
          onConfirm={() => (this.showVersionModalRef = false)}
          onRefresh={onRefresh}
        />
        <MoveModal
          show={this.showMoveModalRef}
          row={this.row}
          onCancel={() => (this.showMoveModalRef = false)}
          onRefresh={onRefresh}
        />
        <TaskModal
          show={this.taskShow}
          data={this.taskData}
          onSubmit={this.onTaskSubmit}
          onCancel={this.onTaskCancel}
          projectCode={this.projectCode}
          from={1}
          readonly={this.taskReadonly}
          saving={this.taskSaving}
        />
      </>
    )
  }
})

export default TaskDefinition
