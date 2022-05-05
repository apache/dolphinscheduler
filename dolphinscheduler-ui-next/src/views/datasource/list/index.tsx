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

import { defineComponent, onMounted, ref, toRefs, watch } from 'vue'
import {
  NButton,
  NInput,
  NIcon,
  NDataTable,
  NPagination,
  NSpace
} from 'naive-ui'
import Card from '@/components/card'
import DetailModal from './detail'
import { SearchOutlined } from '@vicons/antd'
import { useI18n } from 'vue-i18n'
import { useColumns } from './use-columns'
import { useTable } from './use-table'
import styles from './index.module.scss'
import type { TableColumns } from './types'
import { DefaultTableWidth } from '@/common/column-width-config'

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
      onUpdatedList: updateList
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
      <>
        <Card title=''>
          {{
            default: () => (
              <div class={styles['conditions']}>
                <NButton
                  onClick={onCreate}
                  type='primary'
                  class='btn-create-data-source'
                >
                  {t('datasource.create_datasource')}
                </NButton>
                <NSpace
                  class={styles['conditions-search']}
                  justify='end'
                  wrap={false}
                >
                  <div class={styles['conditions-search-input']}>
                    <NInput
                      v-model={[this.searchVal, 'value']}
                      placeholder={`${t('datasource.search_input_tips')}`}
                    />
                  </div>
                  <NButton type='primary' onClick={onUpdatedList}>
                    <NIcon>
                      <SearchOutlined />
                    </NIcon>
                  </NButton>
                </NSpace>
              </div>
            )
          }}
        </Card>
        <Card title='' class={styles['mt-8']}>
          <NDataTable
            row-class-name='data-source-items'
            columns={columns.columns}
            data={list}
            loading={loading}
            striped
            scrollX={columns.tableWidth}
          />
          <NPagination
            page={page}
            page-size={pageSize}
            item-count={itemCount}
            show-quick-jumper
            show-size-picker
            page-sizes={[10, 30, 50]}
            class={styles['pagination']}
            on-update:page={changePage}
            on-update:page-size={changePageSize}
          />
        </Card>
        <DetailModal
          show={showDetailModal}
          id={id}
          onCancel={() => void (this.showDetailModal = false)}
          onUpdate={onUpdatedList}
        />
      </>
    )
  }
})
export default list
