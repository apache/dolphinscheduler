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
  queryCurrentResourceByFullName,
  queryCurrentResourceByFileName
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
    const fullName = ref(String(router.currentRoute.value.query.prefix || ""))
    const tenantCode = ref(String(router.currentRoute.value.query.tenantCode || ""))
    const resourceListRef = ref()
    const folderShowRef = ref(false)
    const uploadShowRef = ref(false)
    const renameShowRef = ref(false)
    const searchRef = ref()

    const renameInfo = reactive({
      name: '',
      description: '',
      fullName: '',
      user_name: ''
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
        fullName.value,
        tenantCode.value,
        searchRef.value,
        paginationReactive.page,
        paginationReactive.pageSize
      )
    }

    const handleUpdatePageSize = (pageSize: number) => {
      paginationReactive.page = 1
      paginationReactive.pageSize = pageSize
      resourceListRef.value = getResourceListState(
        fullName.value,
        tenantCode.value,
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
        fullName.value,
        tenantCode.value,
        searchRef.value
      )
    }

    const handleCreateFolder = () => {
      handleShowModal(folderShowRef)
    }

    const handleCreateFile = () => {
      const name = fullName.value
        ? 'resource-subfile-create'
        : 'resource-file-create'
      router.push({
        name,
        params: { id: fullName.value }
      })
    }

    const handleUploadFile = () => {
      handleShowModal(uploadShowRef)
    }

    const handleRenameFile: IRenameFile = (name: string, description: string, fullName: string, user_name: string) => {
      renameInfo.fullName = fullName
      renameInfo.name = name
      renameInfo.description = description
      renameInfo.user_name = user_name
      handleShowModal(renameShowRef)
    }

    const handleGoRoot = () => {
      router.push({
        name: 'file-manage'
      })
    }

    const updateList = () => {
      resourceListRef.value = getResourceListState(
        fullName.value,
        tenantCode.value,
        searchRef.value
      )
    }
    const fileStore = useFileStore()

    onMounted(() => {
      resourceListRef.value = getResourceListState(fullName.value, tenantCode.value,searchRef.value)
    })

    const breadcrumbItemsRef: Ref<Array<BreadcrumbItem> | undefined> = ref([
      {
        id: 1,
        fullName: 'l1',
        userName: 'u1'
      },
      {
        id: 2,
        fullName: 'l2',
        userName: 'u2'
      },
      {
        id: 4,
        fullName: 'l3',
        userName: 'u3'
      }
    ])

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    onMounted(() => {
      const currfullName = String(router.currentRoute.value.query.prefix || "")
      if (currfullName === "") {
        fileStore.setCurrentDir('/')
      } else {
        fileStore.setCurrentDir(currfullName)
      }
    })

    const initBreadcrumb = async (dirs: string[]) => {
      for (let index = 0; index < dirs.length; index ++) {
        const newDir = dirs.slice(0, index + 1).join('/')
        const resource = await queryCurrentResourceByFileName(
          {
            type: 'FILE',
            fileName: newDir+"/",
            tenantCode: tenantCode.value
          }
        )
        breadcrumbItemsRef.value?.push({ id: resource.fullName, fullName: resource.alias, userName: resource.userName })
        }
    }

    onMounted(() => {
      breadcrumbItemsRef.value = []
      if (fullName.value != "") {
        breadcrumbItemsRef.value?.push({ id: 0, fullName: 'Root', userName: '' })
        queryCurrentResourceByFullName(
          {
            type: 'FILE',
            fullName: fullName.value,
            tenantCode: tenantCode.value,
          }
        ).then((res: ResourceFile) => {
          if (res.fileName) {
            const dirs = res.fileName.split('/')
            if (dirs && dirs.length > 1) {
              dirs.pop()
              initBreadcrumb(dirs)
            }
          }
        })
      }
    })

    return {
      fullName,
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
                      <NBreadcrumbItem href={"0?prefix=" + item.id.toString() + "&tenantCode=" + item.userName}>
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
          name={this.renameInfo.name}
          fullName={this.renameInfo.fullName}
          description={this.renameInfo.description}
          userName={this.renameInfo.user_name}
          onUpdateList={this.updateList}
        />
      </NSpace>
    )
  }
})
