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

import { useAsyncState } from '@vueuse/core'
import { reactive, h, ref } from 'vue'
import { NButton, NIcon, NTooltip } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { queryQueueListPaging } from '@/service/modules/queues'
import { EditOutlined } from '@vicons/antd'
import type { QueueRes } from '@/service/modules/queues/types'

export function useTable() {
  const { t } = useI18n()

  const handleEdit = (row: any) => {
    variables.showModalRef = true
    variables.statusRef = 1
    variables.row = row
  }

  const createColumns = (variables: any) => {
    variables.columns = [
      {
        title: '#',
        key: 'index',
        render: (row: any, index: number) => index + 1
      },
      {
        title: t('security.yarn_queue.queue_name'),
        key: 'queueName',
        className: 'queue-name'
      },
      {
        title: t('security.yarn_queue.queue_value'),
        key: 'queue'
      },
      {
        title: t('security.yarn_queue.create_time'),
        key: 'createTime'
      },
      {
        title: t('security.yarn_queue.update_time'),
        key: 'updateTime'
      },
      {
        title: t('security.yarn_queue.operation'),
        key: 'operation',
        render(row: any) {
          return h(
            NTooltip,
            {},
            {
              trigger: () =>
                h(
                  NButton,
                  {
                    circle: true,
                    type: 'info',
                    size: 'small',
                    class: 'edit',
                    onClick: () => {
                      handleEdit(row)
                    }
                  },
                  {
                    icon: () =>
                      h(NIcon, null, { default: () => h(EditOutlined) })
                  }
                ),
              default: () => t('security.yarn_queue.edit')
            }
          )
        }
      }
    ]
  }

  const variables = reactive({
    columns: [],
    tableData: [],
    page: ref(1),
    pageSize: ref(10),
    searchVal: ref(null),
    totalPage: ref(1),
    showModalRef: ref(false),
    statusRef: ref(0),
    row: {},
    loadingRef: ref(false)
  })

  const getTableData = (params: any) => {
    if (variables.loadingRef) return
    variables.loadingRef = true
    const { state } = useAsyncState(
      queryQueueListPaging({ ...params }).then((res: QueueRes) => {
        variables.tableData = res.totalList.map((item, unused) => {
          return {
            ...item
          }
        }) as any
        variables.totalPage = res.totalPage
        variables.loadingRef = false
      }),
      {}
    )

    return state
  }

  return {
    variables,
    getTableData,
    createColumns
  }
}
