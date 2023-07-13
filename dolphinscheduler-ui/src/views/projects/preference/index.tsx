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
  NButton
} from 'naive-ui'
import { defineComponent, onMounted, toRefs, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useTable } from '@/views/projects/parameter/use-table'
import Card from '@/components/card'
import ParameterModal from '@/views/projects/parameter/components/parameter-modal'
import { SearchOutlined } from '@vicons/antd'

export default defineComponent({
  name: 'ProjectPreferenceList',
  setup() {
    const { variables, createColumns, getTableData } = useTable()

    const requestData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal,
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
        <Card title={t('project.preference.preference_manage')}>
          <NSpace vertical>
            <NDataTable
              loading={loadingRef}
              columns={this.columns}
              data={this.tableData}
              striped
              size={'small'}
              scrollX={this.tableWidth}
            />
          </NSpace>
        </Card>
      </NSpace>
    )
  }
})
