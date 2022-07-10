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

import { h, ref, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  DataTableCreateRowKey,
  NSpace,
  NRadioGroup,
  NRadioButton,
  NTooltip,
  NButton,
  NIcon,
  NTag,
  NDropdown,
  NPopconfirm,
  NRadio
} from 'naive-ui'
import { EditOutlined, DeleteOutlined, UserOutlined } from '@vicons/antd'
import {
  COLUMN_WIDTH_CONFIG,
  calculateTableWidth,
  DefaultTableWidth
} from '@/common/column-width-config'
import type { TableColumns, InternalRowData } from '../types'

export function useColumns(onCallback: Function) {
  const { t } = useI18n()

  const columnsRef = ref({
    columns: [] as TableColumns,
    tableWidth: DefaultTableWidth
  })

  const handleClick = (row: any) => {
    console.log('Clicked')
  }

  const createColumns = () => {
    const columns = [
      {
        type: 'selection',
        key: 'selection',
        ...COLUMN_WIDTH_CONFIG['checkbox']
      },
      {
        title: '#',
        key: 'id',
        // render: (unused: any, index: number) => index + 1,
        ...COLUMN_WIDTH_CONFIG['index']
      },
      {
        title: t('project.list.project_name'),
        key: 'name',
        className: 'project-name',
        ...COLUMN_WIDTH_CONFIG['size'],
      },
      {
        // title: t('project.list.perm'),
        title: '权限等级',
        key: 'perm',
        ...COLUMN_WIDTH_CONFIG['index'],
      },
      // 下面是尝试实现的单选版本
      // {
      //   title: t('project.list.operation'),
      //   key: 'actions',
      //   ...COLUMN_WIDTH_CONFIG['linkName'],
      //   render(row: any) {
      //     return h(NRadioGroup, null, {
      //       default: () => [
      //         h(
      //           NRadio,
      //           {
      //             label: '无权限',
      //             onUpdateChecked: () => {
      //               handleClick(row)
      //             }
      //           },


      //         ),
      //         h(
      //           NRadio,
      //           {
      //             label: '读权限'
      //           },


      //         ),
      //         h(
      //           NRadio,
      //           {
      //             label: '读写权限'
      //           },


      //         )
      //       ]
      //     })
      //   }
      // },

      // 下面是原有的按钮实现
      // {
      //   title: t('project.list.operation'),
      //   key: 'actions',
      //   ...COLUMN_WIDTH_CONFIG['operation'](2),
      //   render(row: any) {
      //     return h(NSpace, null, {
      //       default: () => [
      //         h(
      //           NTooltip,
      //           {},
      //           {
      //             trigger: () =>
      //               h(
      //                 NButton,
      //                 {
      //                   circle: true,
      //                   type: 'info',
      //                   size: 'small',
      //                   class: 'edit',
      //                   onClick: () => {
      //                     // handleEdit(row)
      //                   }
      //                 },
      //                 {
      //                   icon: () =>
      //                     h(NIcon, null, { default: () => h(EditOutlined) })
      //                 }
      //               ),
      //             default: () => t('project.list.edit')
      //           }
      //         ),
      //         h(
      //           NPopconfirm,
      //           {
      //             onPositiveClick: () => {
      //               // handleDelete(row)
      //             }
      //           },
      //           {
      //             trigger: () =>
      //               h(
      //                 NTooltip,
      //                 {},
      //                 {
      //                   trigger: () =>
      //                     h(
      //                       NButton,
      //                       {
      //                         circle: true,
      //                         type: 'error',
      //                         size: 'small',
      //                         class: 'delete'
      //                       },
      //                       {
      //                         icon: () =>
      //                           h(NIcon, null, {
      //                             default: () => h(DeleteOutlined)
      //                           })
      //                       }
      //                     ),
      //                   default: () => t('project.list.delete')
      //                 }
      //               ),
      //             default: () => t('project.list.delete_confirm')
      //           }
      //         )
      //       ]
      //     })
      //   }
      // }
    ]
    columnsRef.value = {
      columns,
      tableWidth: calculateTableWidth(columns)
    }
  }

  onMounted(() => {
    createColumns()
  })

  watch(useI18n().locale, () => {
    createColumns()
  })

  return {
    columnsRef,
    createColumns
  }
}
