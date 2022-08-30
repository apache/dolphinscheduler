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
  defineComponent,
  getCurrentInstance,
  onMounted,
  ref,
  toRefs,
  watch
} from 'vue'
import {
  NButton,
  NInput,
  NIcon,
  NDataTable,
  NPagination,
  NSpace
} from 'naive-ui'
import { SearchOutlined } from '@vicons/antd'
import { useI18n } from 'vue-i18n'
import { useColumns } from './use-columns'
import { useTable } from './use-table'
import { DefaultTableWidth } from '@/common/column-width-config'
import Card from '@/components/card'
import DetailModal from './detail'
import type { TableColumns } from './types'

const list = defineComponent({
  name: 'list',
  setup() {
    const { t } = useI18n()
    const showDetailModal = ref(false)
    const selectId = ref()
    const columns = ref({
      columns: [] as TableColumns,
      tableWidth: DefaultTableWidth
    })
    const { data, changePage, changePageSize, deleteRecord, updateList } =
      useTable()

    const { getColumns } = useColumns((id: number, type: 'edit' | 'delete') => {
      if (type === 'edit') {
        showDetailModal.value = true
        selectId.value = id
      } else {
        deleteRecord(id)
      }
    })

    const onCreate = () => {
      selectId.value = null
      showDetailModal.value = true
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    onMounted(() => {
      changePage(1)
      columns.value = getColumns()
    })

    watch(useI18n().locale, () => {
      columns.value = getColumns()
    })

    return {
      t,
      showDetailModal,
      id: selectId,
      columns,
      ...toRefs(data),
      changePage,
      changePageSize,
      onCreate,
      onUpdatedList: updateList,
      trim
    }
  },
  render() {
    const {
      t,
      id,
      showDetailModal,
      columns,
      list,
      page,
      pageSize,
      itemCount,
      loading,
      changePage,
      changePageSize,
      onCreate,
      onUpdatedList
    } = this

    return (
      <NSpace vertical>
        <Card>
          <NSpace justify='space-between'>
            <NButton
              onClick={onCreate}
              type='primary'
              size='small'
              class='btn-create-data-source'
            >
              {t('datasource.create_datasource')}
            </NButton>
            <NSpace justify='end' wrap={false}>
              <NInput
                allowInput={this.trim}
                v-model={[this.searchVal, 'value']}
                size='small'
                placeholder={`${t('datasource.search_input_tips')}`}
              />
              <NButton type='primary' size='small' onClick={onUpdatedList}>
                <NIcon>
                  <SearchOutlined />
                </NIcon>
              </NButton>
            </NSpace>
          </NSpace>
        </Card>
        <Card title={t('menu.datasource')}>
          <NSpace vertical>
            <NDataTable
              row-class-name='data-source-items'
              columns={columns.columns}
              data={list}
              loading={loading}
              striped
              scrollX={columns.tableWidth}
            />
            <NSpace justify='center'>
              <NPagination
                page={page}
                page-size={pageSize}
                item-count={itemCount}
                show-quick-jumper
                show-size-picker
                page-sizes={[10, 30, 50]}
                on-update:page={changePage}
                on-update:page-size={changePageSize}
              />
            </NSpace>
          </NSpace>
        </Card>
        <DetailModal
          show={showDetailModal}
          id={id}
          onCancel={() => void (this.showDetailModal = false)}
          onUpdate={onUpdatedList}
        />
      </NSpace>
    )
  }
})
export default list
