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
import { NButton, NIcon, NDataTable, NPagination, NSpace } from 'naive-ui'
import DetailModal from './detail'
import Card from '@/components/card'
import { SearchOutlined } from '@vicons/antd'
import { useI18n } from 'vue-i18n'
import { useUserInfo } from './use-userinfo'
import { useColumns } from './use-columns'
import { useTable } from './use-table'
import type { IRecord } from './types'
import Search from '@/components/input-search'

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
      IS_ADMIN,
      showDetailModal,
      currentRecord: currentRecord,
      columns,
      ...toRefs(data),
      changePage,
      changePageSize,
      onCreate,
      onCloseModal,
      onUpdatedList: updateList,
      trim
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
      <NSpace vertical>
        <Card>
          {{
            default: () => (
              <NSpace justify='space-between'>
                {IS_ADMIN && (
                  <NButton onClick={onCreate} type='primary' size='small'>
                    {t('security.alarm_instance.create_alarm_instance')}
                  </NButton>
                )}
                <NSpace justify='end' wrap={false}>
                  <Search
                    v-model:value={this.searchVal}
                    placeholder={`${t(
                      'security.alarm_instance.search_input_tips'
                    )}`}
                    onSearch={onUpdatedList}
                  />
                  <NButton type='primary' size='small' onClick={onUpdatedList}>
                    <NIcon>
                      <SearchOutlined />
                    </NIcon>
                  </NButton>
                </NSpace>
              </NSpace>
            )
          }}
        </Card>
        <Card title={t('menu.alarm_instance_manage')}>
          <NSpace vertical>
            <NDataTable
              columns={columns}
              data={list}
              loading={loading}
              striped
            />
            <NSpace justify='center'>
              <NPagination
                page={page}
                page-size={pageSize}
                item-count={itemCount}
                show-quick-jumper
                on-update:page={changePage}
                on-update:page-size={changePageSize}
              />
            </NSpace>
          </NSpace>
        </Card>
        {IS_ADMIN && (
          <DetailModal
            show={showDetailModal}
            currentRecord={currentRecord}
            onCancel={onCloseModal}
            onUpdate={onUpdatedList}
          />
        )}
      </NSpace>
    )
  }
})
export default AlarmInstanceManage
