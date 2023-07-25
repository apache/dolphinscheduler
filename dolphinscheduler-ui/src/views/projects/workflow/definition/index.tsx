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
  NPagination,
  NSpace,
  NTooltip,
  NPopconfirm,
  NModal
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
import Search from '@/components/input-search'

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
      batchCopyWorkflow,
      gotoTimingManage
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

    const confirmToSetWorkflowTiming = () => {
      gotoTimingManage(variables.row)
    }

    const handleSearch = () => {
      variables.page = 1
      requestData()
    }

    const onClearSearch = () => {
      variables.page = 1
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: ''
      })
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

    const createDefinitionDynamic = () => {
      router.push({
        path: `/projects/${projectCode}/workflow/definitions/create`,
        query: {
          dynamic: 'true'
        }
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
      onClearSearch,
      handleUpdateList,
      createDefinition,
      createDefinitionDynamic,
      handleChangePageSize,
      batchDeleteWorkflow,
      batchExportWorkflow,
      batchCopyWorkflow,
      handleCopyUpdateList,
      confirmToSetWorkflowTiming,
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
              {this.uiSettingStore.getDynamicTask && (
                <NButton
                  type='warning'
                  size='small'
                  onClick={this.createDefinitionDynamic}
                >
                  {t('project.workflow.create_workflow_dynamic')}
                </NButton>
              )}
              <NButton
                strong
                secondary
                size='small'
                onClick={() => (this.showRef = true)}
              >
                {t('project.workflow.import_workflow')}
              </NButton>
            </NSpace>
            <NSpace>
              <Search
                placeholder={t('resource.function.enter_keyword_tips')}
                v-model:value={this.searchVal}
                onSearch={this.handleSearch}
                onClear={this.onClearSearch}
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
                    default: () => t('project.workflow.batch_delete'),
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
                              {t('project.workflow.batch_delete')}
                            </NButton>
                          )
                        }}
                      </NPopconfirm>
                    )
                  }}
                </NTooltip>
                <NTooltip>
                  {{
                    default: () => t('project.workflow.batch_export'),
                    trigger: () => (
                      <NButton
                        tag='div'
                        size='small'
                        type='primary'
                        disabled={this.checkedRowKeys.length <= 0}
                        onClick={this.batchExportWorkflow}
                        class='btn-delete-all'
                      >
                        {t('project.workflow.batch_export')}
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
        <NModal
          v-model:show={this.setTimingDialogShowRef}
          preset={'dialog'}
          title={t('project.workflow.success')}
          content={t('project.workflow.want_to_set_timing')}
          positiveText={t('project.workflow.confirm')}
          negativeText={t('project.workflow.cancel')}
          maskClosable={false}
          onPositiveClick={this.confirmToSetWorkflowTiming}
        />
      </NSpace>
    )
  }
})
