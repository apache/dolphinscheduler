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
  getCurrentInstance
} from 'vue'
import {
  NIcon,
  NSpace,
  NDataTable,
  NButtonGroup,
  NButton,
  NPagination,
  NInput,
  NBreadcrumb,
  NBreadcrumbItem
} from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { SearchOutlined } from '@vicons/antd'
import { useTable } from './table/use-table'
import { useFileState } from './use-file'
import { BreadcrumbItem, IRenameFile } from './types'
import { useFileStore } from '@/store/file/file'
import {
  queryCurrentResourceById,
  queryResourceById
} from '@/service/modules/resources'
import Card from '@/components/card'
import ResourceFolderModal from './folder'
import ResourceUploadModal from './upload'
import ResourceRenameModal from './rename'
import styles from './index.module.scss'
import type { ResourceFile } from '@/service/modules/resources/types'
import type { Router } from 'vue-router'

export default defineComponent({
  name: 'File',
  setup() {
    const router: Router = useRouter()
    const fileId = ref(Number(router.currentRoute.value.params.id) || -1)

    const resourceListRef = ref()
    const folderShowRef = ref(false)
    const uploadShowRef = ref(false)
    const renameShowRef = ref(false)
    const searchRef = ref()

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
        searchRef.value,
        paginationReactive.page,
        paginationReactive.pageSize
      )
    }

    const handleUpdatePageSize = (pageSize: number) => {
      paginationReactive.page = 1
      paginationReactive.pageSize = pageSize
      resourceListRef.value = getResourceListState(
        fileId.value,
        searchRef.value,
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
        searchRef.value
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

    const handleGoRoot = () => {
      router.push({
        name: 'file-manage'
      })
    }

    const updateList = () => {
      resourceListRef.value = getResourceListState(
        fileId.value,
        searchRef.value
      )
    }
    const fileStore = useFileStore()

    onMounted(() => {
      resourceListRef.value = getResourceListState(fileId.value)
    })

    const breadcrumbItemsRef: Ref<Array<BreadcrumbItem> | undefined> = ref([
      {
        id: 1,
        fullName: 'l1'
      },
      {
        id: 2,
        fullName: 'l2'
      },
      {
        id: 4,
        fullName: 'l3'
      }
    ])

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    onMounted(() => {
      const currFileId = Number(router.currentRoute.value.params.id) || -1
      if (currFileId === -1) {
        fileStore.setCurrentDir('/')
      } else {
        queryCurrentResourceById(currFileId).then((res: ResourceFile) => {
          if (res.fullName) {
            fileStore.setCurrentDir(res.fullName)
          }
        })
      }
    })

    const initBreadcrumb = async (dirs: string[]) => {
      let index = 0
      for (const dir of dirs) {
        const newDir = dirs.slice(0, index + 1).join('/')
        if (newDir) {
          const id = 0
          const resource = await queryResourceById(
            {
              id,
              type: 'FILE',
              fullName: newDir
            },
            id
          )
          breadcrumbItemsRef.value?.push({ id: resource.id, fullName: dir })
        } else {
          breadcrumbItemsRef.value?.push({ id: 0, fullName: 'Root' })
        }
        index = index + 1
      }
    }

    onMounted(() => {
      breadcrumbItemsRef.value = []
      if (fileId.value != -1) {
        queryCurrentResourceById(fileId.value).then((res: ResourceFile) => {
          if (res.fullName) {
            const dirs = res.fullName.split('/')
            if (dirs && dirs.length > 1) {
              initBreadcrumb(dirs)
            }
          }
        })
      }
    })

    return {
      fileId,
      searchRef,
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
      handleGoRoot,
      pagination: paginationReactive,
      renameInfo,
      breadcrumbItemsRef,
      trim
    }
  },
  render() {
    const { t } = useI18n()
    const { columnsRef, tableWidth } = useTable(
      this.handleRenameFile,
      this.updateList
    )
    const {
      handleConditions,
      handleCreateFolder,
      handleCreateFile,
      handleUploadFile
    } = this

    return (
      <NSpace vertical>
        <Card>
          <NSpace justify='space-between'>
            <NButtonGroup size='small'>
              <NButton
                onClick={handleCreateFolder}
                class='btn-create-directory'
              >
                {t('resource.file.create_folder')}
              </NButton>
              <NButton onClick={handleCreateFile} class='btn-create-file'>
                {t('resource.file.create_file')}
              </NButton>
              <NButton onClick={handleUploadFile} class='btn-upload-file'>
                {t('resource.file.upload_files')}
              </NButton>
            </NButtonGroup>
            <NSpace>
              <NInput
                size='small'
                allowInput={this.trim}
                placeholder={t('resource.file.enter_keyword_tips')}
                v-model={[this.searchRef, 'value']}
              />
              <NButton size='small' type='primary' onClick={handleConditions}>
                <NIcon>
                  <SearchOutlined />
                </NIcon>
              </NButton>
            </NSpace>
          </NSpace>
        </Card>
        <Card title={t('resource.file.file_manage')}>
          {{
            'header-extra': () => (
              <NBreadcrumb separator='>' class={styles['breadcrumb']}>
                {this.breadcrumbItemsRef?.map((item: BreadcrumbItem) => {
                  if (item.id === 0) {
                    return (
                      <NBreadcrumbItem>
                        <span onClick={this.handleGoRoot}>{item.fullName}</span>
                      </NBreadcrumbItem>
                    )
                  } else {
                    return (
                      <NBreadcrumbItem href={item.id.toString()}>
                        {item.fullName}
                      </NBreadcrumbItem>
                    )
                  }
                })}
              </NBreadcrumb>
            ),
            default: () => (
              <NSpace vertical>
                <NDataTable
                  remote
                  columns={columnsRef}
                  data={this.resourceListRef?.value.table}
                  striped
                  size={'small'}
                  class={styles['table-box']}
                  row-class-name='items'
                  scrollX={tableWidth}
                />
                <NSpace justify='center'>
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
                </NSpace>
              </NSpace>
            )
          }}
        </Card>
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
      </NSpace>
    )
  }
})
