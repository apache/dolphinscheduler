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
  ref,
  defineComponent,
  toRefs,
  reactive,
  onMounted,
  getCurrentInstance
} from 'vue'
import {
  NButton,
  NIcon,
  NInput,
  NDataTable,
  NPagination,
  NSpace
} from 'naive-ui'
import Card from '@/components/card'
import { SearchOutlined } from '@vicons/antd'
import { useI18n } from 'vue-i18n'
import { useTable } from './use-table'
import FormModal from './components/form-modal'

const taskGroupOption = defineComponent({
  name: 'taskGroupOption',
  setup() {
    const { t } = useI18n()
    const { variables, getTableData } = useTable()
    const showModalRef = ref(false)
    const modelStatusRef = ref(0)

    const searchParamRef = ref()

    const updateItemData = reactive({
      id: 0,
      name: '',
      projectCode: 0,
      groupSize: 0,
      status: 1,
      description: ''
    })

    const requestData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        name: variables.name
      })
    }

    const resetTableData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        name: variables.name
      })
    }

    const onCancel = () => {
      showModalRef.value = false
    }

    const onConfirm = () => {
      showModalRef.value = false
      resetTableData()
    }

    const onUpdatePageSize = () => {
      variables.page = 1
      resetTableData()
    }

    const updateItem = (
      id: number,
      name: string,
      projectCode: number,
      groupSize: number,
      description: string
    ) => {
      modelStatusRef.value = 1
      showModalRef.value = true
      updateItemData.id = id
      updateItemData.name = name
      updateItemData.projectCode = projectCode
      updateItemData.groupSize = groupSize
      updateItemData.description = description
    }

    const onSearch = () => {
      resetTableData()
    }

    const onCreate = () => {
      modelStatusRef.value = 0
      showModalRef.value = true
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    onMounted(() => {
      requestData()
    })

    return {
      ...toRefs(variables),
      t,
      onCreate,
      onSearch,
      searchParamRef,
      resetTableData,
      onUpdatePageSize,
      updateItem,
      showModalRef,
      modelStatusRef,
      onCancel,
      onConfirm,
      updateItemData,
      trim
    }
  },
  render() {
    const {
      t,
      showModalRef,
      modelStatusRef,
      onCancel,
      onConfirm,
      updateItemData,
      resetTableData,
      onUpdatePageSize,
      updateItem,
      onSearch,
      loadingRef
    } = this

    const { columns } = useTable(updateItem, resetTableData)

    return (
      <NSpace vertical>
        <Card>
          <NSpace justify='space-between'>
            <NButton
              size='small'
              type={'primary'}
              onClick={() => this.onCreate()}
            >
              {t('resource.task_group_option.create')}
            </NButton>
            <NSpace>
              <NInput
                allowInput={this.trim}
                size='small'
                v-model={[this.name, 'value']}
                placeholder={t(
                  'resource.task_group_option.please_enter_keywords'
                )}
              ></NInput>
              <NButton size='small' type='primary' onClick={onSearch}>
                <NIcon>
                  <SearchOutlined />
                </NIcon>
              </NButton>
            </NSpace>
          </NSpace>
        </Card>
        <Card title={t('resource.task_group_option.option')}>
          <NSpace vertical>
            <NDataTable
              loading={loadingRef}
              columns={columns}
              size={'small'}
              data={this.tableData}
              striped
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
                onUpdatePage={resetTableData}
                onUpdatePageSize={onUpdatePageSize}
              />
            </NSpace>
          </NSpace>
        </Card>
        {showModalRef && (
          <FormModal
            show={showModalRef}
            onCancel={onCancel}
            onConfirm={onConfirm}
            data={updateItemData}
            status={modelStatusRef}
          />
        )}
      </NSpace>
    )
  }
})

export default taskGroupOption
