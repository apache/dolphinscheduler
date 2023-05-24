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

import { ArrowLeftOutlined } from '@vicons/antd'
import { NButton, NDataTable, NIcon, NPagination, NSpace } from 'naive-ui'
import { defineComponent, onMounted, toRefs, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useTable } from './use-table'
import Card from '@/components/card'
import TimingModal from '../components/timing-modal'
import type { Router } from 'vue-router'

export default defineComponent({
  name: 'WorkflowDefinitionTiming',
  setup() {
    const { variables, createColumns, getTableData } = useTable()

    const requestData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal,
        projectCode: variables.projectCode,
        processDefinitionCode: variables.processDefinitionCode
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

    onMounted(() => {
      createColumns(variables)
      requestData()
    })

    watch(useI18n().locale, () => {
      createColumns(variables)
    })

    return {
      requestData,
      handleSearch,
      handleUpdateList,
      handleChangePageSize,
      ...toRefs(variables)
    }
  },
  render() {
    const { t } = useI18n()
    const router: Router = useRouter()
    const { loadingRef } = this

    return (
      <NSpace vertical>
        <Card>
          <NButton type='primary' size='small' onClick={() => router.go(-1)}>
            <NIcon>
              <ArrowLeftOutlined />
            </NIcon>
          </NButton>
        </Card>
        <Card title={t('project.workflow.cron_manage')}>
          <NSpace vertical>
            <NDataTable
              loading={loadingRef}
              columns={this.columns}
              data={this.tableData}
              striped
              size={'small'}
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
                onUpdatePage={this.requestData}
                onUpdatePageSize={this.handleChangePageSize}
              />
            </NSpace>
          </NSpace>
        </Card>
        <TimingModal
          type={'update'}
          v-model:row={this.row}
          v-model:show={this.showRef}
          onUpdateList={this.handleUpdateList}
        />
      </NSpace>
    )
  }
})
