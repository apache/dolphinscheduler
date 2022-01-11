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
    { title: t('resource.id'), key: 'id', render: (row, index) => index + 1 },
    {
      title: t('resource.name'),
      key: 'name',
      render: (row) =>
        h(
          'a',
          {
            href: 'javascript:',
            class: styles.links,
            onClick: () => goSubFolder(router, row),
          },
          {
            default: () => {
              return row.name
            },
          },
        ),
    },
    { title: t('resource.user_name'), key: 'user_name' },
    {
      title: t('resource.whether_directory'),
      key: 'whether_directory',
      render: (row) => (row.directory ? t('resource.yes') : t('resource.no')),
    },
    { title: t('resource.file_name'), key: 'file_name' },
    { title: t('resource.description'), key: 'description' },
    {
      title: t('resource.size'),
      key: 'size',
      render: (row) => bytesToSize(row.size),
    },
    { title: t('resource.update_time'), key: 'update_time' },
    {
      title: t('resource.operation'),
      key: 'operation',
      render: (row) =>
        h(TableAction, {
          row,
          onRenameResource: (id, name, description) =>
            renameResource(id, name, description),
          onUpdateList: () => updateList(),
        }),
    },
  ]

  return {
    columnsRef,
  }
}
