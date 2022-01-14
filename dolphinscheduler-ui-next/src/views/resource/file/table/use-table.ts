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
import { bytesToSize } from '@/utils/common'
import { useFileStore } from '@/store/file/file'
import TableAction from './table-action'
import { IRenameFile } from '../types'
import type { Router } from 'vue-router'
import type { TableColumns } from 'naive-ui/es/data-table/src/interface'
import styles from './index.module.scss'

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
      title: t('resource.file.id'),
      key: 'id',
      width: 50,
      render: (_row, index) => index + 1
    },
    {
      title: t('resource.file.name'),
      key: 'name',
      width: 120,
      render: (row) =>
        h(
          'a',
          {
            href: 'javascript:',
            class: styles.links,
            onClick: () => goSubFolder(router, row)
          },
          {
            default: () => {
              return row.name
            }
          }
        )
    },
    { title: t('resource.file.user_name'), width: 100, key: 'user_name' },
    {
      title: t('resource.file.whether_directory'),
      key: 'whether_directory',
      width: 100,
      render: (row) =>
        row.directory ? t('resource.file.yes') : t('resource.file.no')
    },
    { title: t('resource.file.file_name'), key: 'file_name' },
    { title: t('resource.file.description'), width: 150, key: 'description' },
    {
      title: t('resource.file.size'),
      key: 'size',
      render: (row) => bytesToSize(row.size)
    },
    { title: t('resource.file.update_time'), width: 150, key: 'update_time' },
    {
      title: t('resource.file.operation'),
      key: 'operation',
      width: 150,
      render: (row) =>
        h(TableAction, {
          row,
          onRenameResource: (id, name, description) =>
            renameResource(id, name, description),
          onUpdateList: () => updateList()
        })
    }
  ]

  return {
    columnsRef
  }
}
