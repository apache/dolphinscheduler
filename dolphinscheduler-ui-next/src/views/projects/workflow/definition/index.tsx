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

import Card from '@/components/card'
import { SearchOutlined } from '@vicons/antd'
import {
  NButton,
  NDataTable,
  NIcon,
  NInput,
  NPagination,
  NSpace,
  NTooltip,
  NPopconfirm
} from 'naive-ui'
import { defineComponent, onMounted, toRefs, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useTable } from './use-table'
import ImportModal from './components/import-modal'
import StartModal from './components/start-modal'
import TimingModal from './components/timing-modal'
import VersionModal from './components/version-modal'
import { useRouter, useRoute } from 'vue-router'
import type { Router } from 'vue-router'
import styles from './index.module.scss'

export default defineComponent({
  name: 'WorkflowDefinitionList',
  setup() {
    const router: Router = useRouter()
    const route = useRoute()
    const projectCode = Number(route.params.projectCode)

    const {
      variables,
      createColumns,
      getTableData,
      batchDeleteWorkflow,
      batchExportWorkflow,
      batchCopyWorkflow
    } = useTable()

    const requestData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal
      })
    }

    const handleUpdateList = () => {
      requestData()
    }

    const handleSearch = () => {
      variables.page = 1
      requestData()
    }

    const handleChangePageSize = () => {
      variables.page = 1
      requestData()
    }

    const createDefinition = () => {
      router.push({
        path: `/projects/${projectCode}/workflow/definitions/create`
      })
    }

    watch(useI18n().locale, () => {
      createColumns(variables)
    })

    onMounted(() => {
      createColumns(variables)
      requestData()
    })

    return {
      requestData,
      handleSearch,
      handleUpdateList,
      createDefinition,
      handleChangePageSize,
      batchDeleteWorkflow,
      batchExportWorkflow,
      batchCopyWorkflow,
      ...toRefs(variables)
    }
  },
  render() {
    const { t } = useI18n()

    return (
      <div class={styles.content}>
        <Card class={styles.card}>
          <div class={styles.header}>
            <NSpace>
              <NButton
                type='primary'
                onClick={this.createDefinition}
                class='btn-create-process'
              >
                {t('project.workflow.create_workflow')}
              </NButton>
              <NButton strong secondary onClick={() => (this.showRef = true)}>
                {t('project.workflow.import_workflow')}
              </NButton>
            </NSpace>
            <div class={styles.right}>
              <div class={styles.search}>
                <div class={styles.list}>
                  <NButton type='primary' onClick={this.handleSearch}>
                    <NIcon>
                      <SearchOutlined />
                    </NIcon>
                  </NButton>
                </div>
                <div class={styles.list}>
                  <NInput
                    placeholder={t('resource.function.enter_keyword_tips')}
                    v-model={[this.searchVal, 'value']}
                  />
                </div>
              </div>
            </div>
          </div>
        </Card>
        <Card title={t('project.workflow.workflow_definition')}>
          <NDataTable
            rowKey={(row) => row.code}
            columns={this.columns}
            data={this.tableData}
            striped
            size={'small'}
            class={styles.table}
            v-model:checked-row-keys={this.checkedRowKeys}
            row-class-name='items'
          />
          <div class={styles.pagination}>
            <NPagination
              v-model:page={this.page}
              v-model:page-size={this.pageSize}
              page-count={this.totalPage}
              show-size-picker
              page-sizes={[10, 30, 50]}
              show-quick-jumper
              onUpdatePage={this.requestData}
              onUpdatePageSize={this.handleChangePageSize}
            />
          </div>
          <NTooltip>
            {{
              default: () => t('project.workflow.delete'),
              trigger: () => (
                <NButton
                  tag='div'
                  type='primary'
                  disabled={this.checkedRowKeys.length <= 0}
                  style='position: absolute; bottom: 10px; left: 10px;'
                  class='btn-delete-all'
                >
                  <NPopconfirm onPositiveClick={this.batchDeleteWorkflow}>
                    {{
                      default: () => t('project.workflow.delete_confirm'),
                      trigger: () => t('project.workflow.delete')
                    }}
                  </NPopconfirm>
                </NButton>
              )
            }}
          </NTooltip>
          <NTooltip>
            {{
              default: () => t('project.workflow.export'),
              trigger: () => (
                <NButton
                  tag='div'
                  type='primary'
                  disabled={this.checkedRowKeys.length <= 0}
                  onClick={this.batchExportWorkflow}
                  style='position: absolute; bottom: 10px; left: 70px;'
                  class='btn-delete-all'
                >
                  {t('project.workflow.export')}
                </NButton>
              )
            }}
          </NTooltip>
          <NTooltip>
            {{
              default: () => t('project.workflow.batch_copy'),
              trigger: () => (
                <NButton
                  tag='div'
                  type='primary'
                  disabled={this.checkedRowKeys.length <= 0}
                  style='position: absolute; bottom: 10px; left: 130px;'
                  class='btn-delete-all'
                >
                  <NPopconfirm onPositiveClick={this.batchCopyWorkflow}>
                    {{
                      default: () => t('project.workflow.delete_confirm'),
                      trigger: () => t('project.workflow.batch_copy')
                    }}
                  </NPopconfirm>
                </NButton>
              )
            }}
          </NTooltip>
        </Card>
        <ImportModal
          v-model:show={this.showRef}
          onUpdateList={this.handleUpdateList}
        />
        <StartModal
          v-model:row={this.row}
          v-model:show={this.startShowRef}
          onUpdateList={this.handleUpdateList}
        />
        <TimingModal
          v-model:row={this.row}
          v-model:show={this.timingShowRef}
          onUpdateList={this.handleUpdateList}
        />
        <VersionModal
          v-model:row={this.row}
          v-model:show={this.versionShowRef}
          onUpdateList={this.handleUpdateList}
        />
      </div>
    )
  }
})
