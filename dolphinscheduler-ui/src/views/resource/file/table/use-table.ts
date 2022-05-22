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

import { h } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { bytesToSize } from '@/common/common'
import { useFileStore } from '@/store/file/file'
import TableAction from './table-action'
import { IRenameFile } from '../types'
import ButtonLink from '@/components/button-link'
import { NEllipsis } from 'naive-ui'
import {
  COLUMN_WIDTH_CONFIG,
  calculateTableWidth,
  DefaultTableWidth
} from '@/common/column-width-config'
import type { Router } from 'vue-router'
import type { TableColumns } from 'naive-ui/es/data-table/src/interface'

const goSubFolder = (router: Router, item: any) => {
  const fileStore = useFileStore()
  fileStore.setFileInfo(`${item.alias}|${item.size}`)

  if (item.directory) {
    fileStore.setCurrentDir(`${item.fullName}`)
    router.push({ name: 'resource-file-subdirectory', params: { id: item.id } })
  } else {
    router.push({ name: 'resource-file-list', params: { id: item.id } })
  }
}

export function useTable(renameResource: IRenameFile, updateList: () => void) {
  const { t } = useI18n()
  const router: Router = useRouter()

  const columnsRef: TableColumns<any> = [
    {
      title: '#',
      key: 'id',
      ...COLUMN_WIDTH_CONFIG['index'],
      render: (_row, index) => index + 1
    },
    {
      title: t('resource.file.name'),
      key: 'name',
      ...COLUMN_WIDTH_CONFIG['linkName'],
      render: (row) =>
        h(
          ButtonLink,
          {
            onClick: () => void goSubFolder(router, row)
          },
          {
            default: () =>
              h(NEllipsis, COLUMN_WIDTH_CONFIG['linkEllipsis'], () => row.name)
          }
        )
    },
    {
      title: t('resource.file.user_name'),
      ...COLUMN_WIDTH_CONFIG['userName'],
      key: 'user_name'
    },
    {
      title: t('resource.file.whether_directory'),
      key: 'whether_directory',
      ...COLUMN_WIDTH_CONFIG['yesOrNo'],
      render: (row) =>
        row.directory ? t('resource.file.yes') : t('resource.file.no')
    },
    {
      title: t('resource.file.file_name'),
      ...COLUMN_WIDTH_CONFIG['name'],
      key: 'file_name'
    },
    {
      title: t('resource.file.description'),
      ...COLUMN_WIDTH_CONFIG['note'],
      key: 'description'
    },
    {
      title: t('resource.file.size'),
      key: 'size',
      ...COLUMN_WIDTH_CONFIG['size'],
      render: (row) => bytesToSize(row.size)
    },
    {
      title: t('resource.file.update_time'),
      ...COLUMN_WIDTH_CONFIG['time'],
      key: 'update_time'
    },
    {
      title: t('resource.file.operation'),
      key: 'operation',
      render: (row) =>
        h(TableAction, {
          row,
          onRenameResource: (id, name, description) =>
            renameResource(id, name, description),
          onUpdateList: () => updateList()
        }),
      ...COLUMN_WIDTH_CONFIG['operation'](4)
    }
  ]

  return {
    columnsRef,
    tableWidth: calculateTableWidth(columnsRef) || DefaultTableWidth
  }
}
