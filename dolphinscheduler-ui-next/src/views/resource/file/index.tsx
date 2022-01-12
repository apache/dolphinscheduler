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

import { useRouter } from 'vue-router'
import {
  defineComponent,
  onMounted,
  ref,
  reactive,
  Ref,
  watch,
  inject,
} from 'vue'
import { NDataTable, NButtonGroup, NButton } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import Card from '@/components/card'
import Conditions from '@/components/conditions'
import { useTable } from './table/use-table'
import { useFileState } from './use-file'
import ResourceFolderModal from './folder'
import ResourceUploadModal from './upload'
import ResourceRenameModal from './rename'
import { IRenameFile } from './types'
import type { Router } from 'vue-router'

export default defineComponent({
  name: 'File',
  inject: ['reload'],
  setup() {
    const router: Router = useRouter()
    const fileId = ref(Number(router.currentRoute.value.params.id) || -1)

    const reload = inject('reload')
    const resourceListRef = ref()
    const folderShowRef = ref(false)
    const uploadShowRef = ref(false)
    const renameShowRef = ref(false)
    const serachRef = ref()

    const renameInfo = reactive({
      id: -1,
      name: '',
      description: '',
    })

    const paginationReactive = reactive({
      page: 1,
      pageSize: 10,
      itemCount: 0,
      showSizePicker: true,
      pageSizes: [10, 30, 50],
      onChange: (page: number) => {
        paginationReactive.page = page
        resourceListRef.value = getResourceListState(
          fileId.value,
          serachRef.value,
          paginationReactive.page,
          paginationReactive.pageSize,
        )
      },
      onPageSizeChange: (pageSize: number) => {
        paginationReactive.page = 1
        paginationReactive.pageSize = pageSize
        resourceListRef.value = getResourceListState(
          fileId.value,
          serachRef.value,
          paginationReactive.page,
          paginationReactive.pageSize,
        )
      },
    })

    const handleShowModal = (showRef: Ref<Boolean>) => {
      showRef.value = true
    }

    const setPagination = (count: number) => {
      paginationReactive.itemCount = count
    }

    const { getResourceListState } = useFileState(setPagination)

    const handleConditions = (val: string) => {
      serachRef.value = val
      resourceListRef.value = getResourceListState(fileId.value, val)
    }

    const handleCreateFolder = () => {
      handleShowModal(folderShowRef)
    }

    const handleCreateFile = () => {
      const name = fileId.value
        ? 'resource-subfile-create'
        : 'resource-file-create'
      router.push({
        name,
        params: { id: fileId.value },
      })
    }

    const handleUploadFile = () => {
      handleShowModal(uploadShowRef)
    }

    const handleRenameFile: IRenameFile = (id, name, description) => {
      renameInfo.id = id
      renameInfo.name = name
      renameInfo.description = description
      handleShowModal(renameShowRef)
    }

    const updateList = () => {
      resourceListRef.value = getResourceListState(
        fileId.value,
        serachRef.value,
      )
    }

    onMounted(() => {
      resourceListRef.value = getResourceListState(fileId.value)
    })

    watch(
      () => router.currentRoute.value.params.id,
      () => reload(),
    )

    return {
      fileId,
      folderShowRef,
      uploadShowRef,
      renameShowRef,
      handleShowModal,
      resourceListRef,
      updateList,
      handleConditions,
      handleCreateFolder,
      handleCreateFile,
      handleUploadFile,
      handleRenameFile,
      pagination: paginationReactive,
      renameInfo,
    }
  },
  render() {
    const { t } = useI18n()
    const { columnsRef } = useTable(this.handleRenameFile, this.updateList)
    const {
      handleConditions,
      handleCreateFolder,
      handleCreateFile,
      handleUploadFile,
      pagination,
    } = this
    return (
      <Card title={t('resource.file.file_manage')}>
        <Conditions onConditions={handleConditions}>
          <NButtonGroup>
            <NButton onClick={handleCreateFolder}>
              {t('resource.file.create_folder')}
            </NButton>
            <NButton onClick={handleCreateFile}>
              {t('resource.file.create_file')}
            </NButton>
            <NButton onClick={handleUploadFile}>
              {t('resource.file.upload_files')}
            </NButton>
          </NButtonGroup>
        </Conditions>
        <NDataTable
          remote
          columns={columnsRef}
          data={this.resourceListRef?.value.table}
          striped
          size={'small'}
          pagination={pagination}
        />
        <ResourceFolderModal
          v-model:show={this.folderShowRef}
          onUpdateList={this.updateList}
        />
        <ResourceUploadModal
          v-model:show={this.uploadShowRef}
          onUpdateList={this.updateList}
        />
        <ResourceRenameModal
          v-model:show={this.renameShowRef}
          id={this.renameInfo.id}
          name={this.renameInfo.name}
          description={this.renameInfo.description}
          onUpdateList={this.updateList}
        />
      </Card>
    )
  },
})
