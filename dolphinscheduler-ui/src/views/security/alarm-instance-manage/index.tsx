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
import { useUserInfo } from './use-userinfo'
import { useColumns } from './use-columns'
import { useTable } from './use-table'
import styles from './index.module.scss'
import type { IRecord } from './types'

const AlarmInstanceManage = defineComponent({
  name: 'alarm-instance-manage',
  setup() {
    const { t } = useI18n()
    const showDetailModal = ref(false)
    const currentRecord = ref()
    const columns = ref()
    const { IS_ADMIN } = useUserInfo()
    const { data, changePage, changePageSize, deleteRecord, updateList } =
      useTable()

    const { getColumns } = useColumns(
      (record: IRecord, type: 'edit' | 'delete') => {
        if (type === 'edit') {
          showDetailModal.value = true
          currentRecord.value = record
        } else {
          deleteRecord(record.id)
        }
      }
    )

    const onCreate = () => {
      currentRecord.value = null
      showDetailModal.value = true
    }

    const onCloseModal = () => {
      showDetailModal.value = false
      currentRecord.value = {}
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
      IS_ADMIN,
      showDetailModal,
      currentRecord: currentRecord,
      columns,
      ...toRefs(data),
      changePage,
      changePageSize,
      onCreate,
      onCloseModal,
      onUpdatedList: updateList
    }
  },
  render() {
    const {
      t,
      IS_ADMIN,
      currentRecord,
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
      onUpdatedList,
      onCloseModal
    } = this

    return (
      <>
        <Card title=''>
          {{
            default: () => (
              <div class={styles['conditions']}>
                {IS_ADMIN && (
                  <NButton onClick={onCreate} type='primary'>
                    {t('security.alarm_instance.create_alarm_instance')}
                  </NButton>
                )}
                <NSpace
                  class={styles['conditions-search']}
                  justify='end'
                  wrap={false}
                >
                  <div class={styles['conditions-search-input']}>
                    <NInput
                      v-model={[this.searchVal, 'value']}
                      placeholder={`${t(
                        'security.alarm_instance.search_input_tips'
                      )}`}
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
          <NDataTable columns={columns} data={list} loading={loading} striped />
          <NPagination
            page={page}
            page-size={pageSize}
            item-count={itemCount}
            show-quick-jumper
            class={styles['pagination']}
            on-update:page={changePage}
            on-update:page-size={changePageSize}
          />
        </Card>
        {IS_ADMIN && (
          <DetailModal
            show={showDetailModal}
            currentRecord={currentRecord}
            onCancel={onCloseModal}
            onUpdate={onUpdatedList}
          />
        )}
      </>
    )
  }
})
export default AlarmInstanceManage
