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
  inject
} from 'vue'
import {
  NIcon,
  NSpace,
  NDataTable,
  NButtonGroup,
  NButton,
  NPagination,
  NInput
} from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { SearchOutlined } from '@vicons/antd'
import Card from '@/components/card'
import { useTable } from './table/use-table'
import { useFileState } from './use-file'
import ResourceFolderModal from './folder'
import ResourceUploadModal from './upload'
import ResourceRenameModal from './rename'
import { IRenameFile } from './types'
import type { Router } from 'vue-router'
import styles from './index.module.scss'

export default defineComponent({
  name: 'File',
  inject: ['reload'],
  setup() {
    const router: Router = useRouter()
    const fileId = ref(Number(router.currentRoute.value.params.id) || -1)

    const reload: any = inject('reload')
    const resourceListRef = ref()
    const folderShowRef = ref(false)
    const uploadShowRef = ref(false)
    const renameShowRef = ref(false)
    const serachRef = ref()

    const renameInfo = reactive({
      id: -1,
      name: '',
      description: ''
    })

    const paginationReactive = reactive({
      page: 1,
      pageSize: 10,
      itemCount: 0,
      pageSizes: [10, 30, 50]
    })

    const handleUpdatePage = (page: number) => {
      paginationReactive.page = page
      resourceListRef.value = getResourceListState(
        fileId.value,
        serachRef.value,
        paginationReactive.page,
        paginationReactive.pageSize
      )
    }

    const handleUpdatePageSize = (pageSize: number) => {
      paginationReactive.page = 1
      paginationReactive.pageSize = pageSize
      resourceListRef.value = getResourceListState(
        fileId.value,
        serachRef.value,
        paginationReactive.page,
        paginationReactive.pageSize
      )
    }

    const handleShowModal = (showRef: Ref<Boolean>) => {
      showRef.value = true
    }

    const setPagination = (count: number) => {
      paginationReactive.itemCount = count
    }

    const { getResourceListState } = useFileState(setPagination)

    const handleConditions = () => {
      resourceListRef.value = getResourceListState(
        fileId.value,
        serachRef.value
      )
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
        params: { id: fileId.value }
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
        serachRef.value
      )
    }

    onMounted(() => {
      resourceListRef.value = getResourceListState(fileId.value)
    })

    watch(
      () => router.currentRoute.value.params.id,
      // @ts-ignore
      () => reload()
    )

    return {
      fileId,
      serachRef,
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
      handleUpdatePage,
      handleUpdatePageSize,
      pagination: paginationReactive,
      renameInfo
    }
  },
  render() {
    const { t } = useI18n()
    const { columnsRef } = useTable(this.handleRenameFile, this.updateList)
    const {
      handleConditions,
      handleCreateFolder,
      handleCreateFile,
      handleUploadFile
    } = this
    return (
      <div>
        <Card style={{ marginBottom: '8px' }}>
          <div class={styles['conditions-model']}>
            <NSpace>
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
            </NSpace>
            <div class={styles.right}>
              <div class={styles['form-box']}>
                <div class={styles.list}>
                  <NButton onClick={handleConditions}>
                    <NIcon>
                      <SearchOutlined />
                    </NIcon>
                  </NButton>
                </div>
                <div class={styles.list}>
                  <NInput
                    placeholder={t('resource.file.enter_keyword_tips')}
                    v-model={[this.serachRef, 'value']}
                  />
                </div>
              </div>
            </div>
          </div>
        </Card>
        <Card title={t('resource.file.file_manage')}>
          <NDataTable
            remote
            columns={columnsRef}
            data={this.resourceListRef?.value.table}
            striped
            size={'small'}
            class={styles['table-box']}
          />
          <div class={styles.pagination}>
            <NPagination
              v-model:page={this.pagination.page}
              v-model:pageSize={this.pagination.pageSize}
              pageSizes={this.pagination.pageSizes}
              item-count={this.pagination.itemCount}
              onUpdatePage={this.handleUpdatePage}
              onUpdatePageSize={this.handleUpdatePageSize}
              show-quick-jumper
              show-size-picker
            />
          </div>
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
      </div>
    )
  }
})
