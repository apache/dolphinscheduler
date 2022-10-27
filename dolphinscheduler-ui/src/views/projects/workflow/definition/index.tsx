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
import {
  defineComponent,
  getCurrentInstance,
  onMounted,
  toRefs,
  watch
} from 'vue'
import { useI18n } from 'vue-i18n'
import { useTable } from './use-table'
import { useRouter, useRoute } from 'vue-router'
import { useUISettingStore } from '@/store/ui-setting/ui-setting'
import Card from '@/components/card'
import ImportModal from './components/import-modal'
import StartModal from './components/start-modal'
import TimingModal from './components/timing-modal'
import VersionModal from './components/version-modal'
import CopyModal from './components/copy-modal'
import type { Router } from 'vue-router'

export default defineComponent({
  name: 'WorkflowDefinitionList',
  setup() {
    const router: Router = useRouter()
    const route = useRoute()
    const projectCode = Number(route.params.projectCode)
    const uiSettingStore = useUISettingStore()

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

    const handleCopyUpdateList = () => {
      variables.checkedRowKeys = []
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
    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

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
      handleCopyUpdateList,
      ...toRefs(variables),
      uiSettingStore,
      trim
    }
  },
  render() {
    const { t } = useI18n()
    const { loadingRef } = this

    return (
      <NSpace vertical>
        <Card>
          <NSpace justify='space-between'>
            <NSpace>
              <NButton
                type='primary'
                size='small'
                onClick={this.createDefinition}
                class='btn-create-process'
              >
                {t('project.workflow.create_workflow')}
              </NButton>
              <NButton
                strong
                secondary
                size='small'
                onClick={() => (this.showRef = true)}
              >
                {t('project.workflow.import_workflow')}
              </NButton>
              {
                this.uiSettingStore.getDynamicTask && <NButton
                  size='small'
                  type='warning'
                >
                  {t('project.workflow.create_workflow_dynamic')}
                </NButton>
              }
            </NSpace>
            <NSpace>
              <NInput
                allowInput={this.trim}
                size='small'
                placeholder={t('resource.function.enter_keyword_tips')}
                v-model={[this.searchVal, 'value']}
              />
              <NButton type='primary' size='small' onClick={this.handleSearch}>
                <NIcon>
                  <SearchOutlined />
                </NIcon>
              </NButton>
            </NSpace>
          </NSpace>
        </Card>
        <Card title={t('project.workflow.workflow_definition')}>
          <NSpace vertical>
            <NDataTable
              loading={loadingRef}
              rowKey={(row) => row.code}
              columns={this.columns}
              data={this.tableData}
              striped
              v-model:checked-row-keys={this.checkedRowKeys}
              row-class-name='items'
              scrollX={this.tableWidth}
            />
            <NSpace justify='space-between'>
              <NSpace>
                <NTooltip>
                  {{
                    default: () => t('project.workflow.delete'),
                    trigger: () => (
                      <NPopconfirm onPositiveClick={this.batchDeleteWorkflow}>
                        {{
                          default: () => t('project.workflow.delete_confirm'),
                          trigger: () => (
                            <NButton
                              tag='div'
                              size='small'
                              type='primary'
                              disabled={this.checkedRowKeys.length <= 0}
                              class='btn-delete-all'
                            >
                              {t('project.workflow.delete')}
                            </NButton>
                          )
                        }}
                      </NPopconfirm>
                    )
                  }}
                </NTooltip>
                <NTooltip>
                  {{
                    default: () => t('project.workflow.export'),
                    trigger: () => (
                      <NButton
                        tag='div'
                        size='small'
                        type='primary'
                        disabled={this.checkedRowKeys.length <= 0}
                        onClick={this.batchExportWorkflow}
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
                        size='small'
                        type='primary'
                        disabled={this.checkedRowKeys.length <= 0}
                        onClick={() => (this.copyShowRef = true)}
                        class='btn-delete-all'
                      >
                        {t('project.workflow.batch_copy')}
                      </NButton>
                    )
                  }}
                </NTooltip>
              </NSpace>
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
            </NSpace>
          </NSpace>
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
        <CopyModal
          v-model:codes={this.checkedRowKeys}
          v-model:show={this.copyShowRef}
          onUpdateList={this.handleCopyUpdateList}
        />
      </NSpace>
    )
  }
})
