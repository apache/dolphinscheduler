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
  NDataTable,
  NIcon,
  NInput,
  NPagination,
  NSpace,
  NButton,
  NSelect
} from 'naive-ui'
import { defineComponent, onMounted, toRefs, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useTable } from '@/views/projects/parameter/use-table'
import Card from '@/components/card'
import ParameterModal from '@/views/projects/parameter/components/parameter-modal'
import { SearchOutlined } from '@vicons/antd'
import { DATA_TYPES_MAP } from "@/views/projects/parameter/data_type"

export default defineComponent({
  name: 'ProjectParameterList',
  setup() {
    const { variables, createColumns, getTableData } = useTable()

    const requestData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal,
        projectParameterDataType: variables.projectParameterDataType,
        projectCode: variables.projectCode
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

    const onCancelModal = () => {
      variables.showRef = false
    }

    const onConfirmModal = () => {
      variables.showRef = false
      requestData()
    }

    const onCreateParameter = () => {
      variables.showRef = true
      variables.statusRef = 0
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
      onCreateParameter,
      onCancelModal,
      onConfirmModal,
      ...toRefs(variables)
    }
  },
  render() {
    const { t } = useI18n()
    const {
      loadingRef,
      handleSearch,
      onCreateParameter,
      onConfirmModal,
      onCancelModal
    } = this

    return (
      <NSpace vertical>
        <Card>
          <NSpace justify='space-between'>
            <NButton size='small' type='primary' onClick={onCreateParameter}>
              {t('project.parameter.create_parameter')}
            </NButton>
            <NSpace>
              <NInput
                size='small'
                clearable
                v-model={[this.searchVal, 'value']}
                placeholder={t('project.parameter.name')}
              />
              <NSelect
                  v-model={[this.projectParameterDataType, 'value']}
                  size='small'
                  options={Object.keys(DATA_TYPES_MAP).map((item) => {
                    return { value: item, label: item }
                  })}
                  placeholder={t('project.parameter.data_type_tips')}
                  style={{ width: '180px' }}
                  clearable
              />
              <NButton size='small' type='primary' onClick={handleSearch}>
                <NIcon>
                  <SearchOutlined />
                </NIcon>
              </NButton>
            </NSpace>
          </NSpace>
        </Card>
        <Card title={t('project.parameter.parameter_manage')}>
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
        <ParameterModal
          showModalRef={this.showRef}
          statusRef={this.statusRef}
          row={this.row}
          onCancelModal={onCancelModal}
          onConfirmModal={onConfirmModal}
        />
      </NSpace>
    )
  }
})
