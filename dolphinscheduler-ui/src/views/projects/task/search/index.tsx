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

import { computed, defineComponent, onBeforeUnmount, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { RouterLink, useRoute } from 'vue-router'
import { NButton, NDataTable, NInput, NPagination, NSpace } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import Card from '@/components/card'
import { searchTaskWorkflows } from '@/service/modules/task-definition'
import type { TaskWorkflowSearchResult } from '@/service/modules/task-definition/types'
import totalCount from '@/utils/tableTotalCount'

export default defineComponent({
  name: 'task-search',
  setup() {
    const { t } = useI18n()
    const route = useRoute()
    const searchVal = ref('')
    const appliedSearchVal = ref('')
    const page = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const rows = ref<TaskWorkflowSearchResult[]>([])
    const loading = ref(false)
    let requestId = 0

    const columns = computed<DataTableColumns<TaskWorkflowSearchResult>>(() => [
      {
        title: t('project.task.task_name'),
        key: 'taskName',
        minWidth: 200,
        ellipsis: { tooltip: true }
      },
      {
        title: t('project.task.task_type'),
        key: 'taskType',
        width: 160
      },
      {
        title: t('project.task.owning_workflow'),
        key: 'workflowDefinitionName',
        minWidth: 200,
        ellipsis: { tooltip: true },
        render: (row) => (
          <RouterLink
            to={{
              name: 'workflow-definition-detail',
              params: {
                projectCode: route.params.projectCode,
                code: row.workflowDefinitionCode
              }
            }}
          >
            {row.workflowDefinitionName}
          </RouterLink>
        )
      }
    ])

    const loadData = async () => {
      const currentRequest = ++requestId
      const projectCode = Number(route.params.projectCode)
      if (!Number.isFinite(projectCode) || projectCode <= 0) return
      loading.value = true
      try {
        const result = await searchTaskWorkflows(
          {
            pageNo: page.value,
            pageSize: pageSize.value,
            searchVal: appliedSearchVal.value
          },
          projectCode
        )
        if (currentRequest !== requestId) return
        rows.value = result.totalList
        total.value = result.total
      } catch {
        if (currentRequest !== requestId) return
        rows.value = []
        total.value = 0
      } finally {
        if (currentRequest === requestId) loading.value = false
      }
    }

    const onSearch = () => {
      appliedSearchVal.value = searchVal.value.trim()
      page.value = 1
      void loadData()
    }

    watch(
      () => route.params.projectCode,
      () => {
        searchVal.value = ''
        appliedSearchVal.value = ''
        page.value = 1
        rows.value = []
        total.value = 0
        void loadData()
      },
      { immediate: true }
    )

    onBeforeUnmount(() => {
      requestId++
    })

    return () => (
      <NSpace vertical>
        <Card>
          <NSpace justify='end'>
            <NInput
              value={searchVal.value}
              onUpdateValue={(value) => (searchVal.value = value)}
              placeholder={t('project.task.task_search_tips')}
              style={{ width: '320px' }}
              size='small'
              clearable
              onClear={() => {
                searchVal.value = ''
                onSearch()
              }}
              onKeyup={(event) => {
                if (event.key === 'Enter') onSearch()
              }}
            />
            <NButton size='small' type='primary' onClick={onSearch}>
              {t('project.dag.search')}
            </NButton>
          </NSpace>
        </Card>
        <Card title={t('project.task.task_search')}>
          <NSpace vertical>
            <NDataTable
              columns={columns.value}
              data={rows.value}
              loading={loading.value}
              rowKey={(row: TaskWorkflowSearchResult) =>
                `${row.taskCode}-${row.workflowDefinitionCode}`
              }
              scrollX={600}
            />
            <NSpace justify='center'>
              <NPagination
                page={page.value}
                pageSize={pageSize.value}
                itemCount={total.value}
                showSizePicker
                pageSizes={[10, 30, 50]}
                showQuickJumper
                prefix={totalCount}
                onUpdatePage={(value) => {
                  page.value = value
                  void loadData()
                }}
                onUpdatePageSize={(value) => {
                  pageSize.value = value
                  page.value = 1
                  void loadData()
                }}
              />
            </NSpace>
          </NSpace>
        </Card>
      </NSpace>
    )
  }
})
