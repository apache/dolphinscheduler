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
  NBreadcrumb,
  NBreadcrumbItem
} from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { SearchOutlined } from '@vicons/antd'
import { useTable } from './table/use-table'
import { useFileState } from './use-file'
import { IRenameFile } from './types'
import { useFileStore } from '@/store/file/file'
import {
  queryCurrentResourceByFullName
} from '@/service/modules/resources'
import Card from '@/components/card'
import ResourceFolderModal from './folder'
import ResourceUploadModal from './upload'
import ResourceRenameModal from './rename'
import styles from './index.module.scss'
import type { ResourceFile } from '@/service/modules/resources/types'
import type { Router } from 'vue-router'
import Search from "@/components/input-search";
import {getBaseDir} from "@/views/resource";

const baseFILEDir = getBaseDir('FILE')

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

    const breadList = ref([] as String[])

    const handleGoBread = (index: number) => {
      let breadName = ''
      breadList.value.forEach((item, i) => {
        if (i <= index) {
          breadName = breadName === "" ? item.toString() : breadName + '/' + item.toString();
        }
      })
      goBread(breadName)
    }

    const goBread = (fullName: string) => {
      if (!fullName.startsWith(baseFILEDir.value)) {
        handleGoResourceManage()
        return
      }
      queryCurrentResourceByFullName(
          {
            type: 'FILE',
            fullName: fullName + "/",
            tenantCode: tenantCode.value
          }
      ).then((res: any) => {
        fileStore.setCurrentDir(res.fullName)
        router.push({
          name: 'file-manage', query: {prefix: res.fullName, tenantCode: res.userName}
        })
      })
    }

    const handleGoResourceManage = () => {
      router.push({ name: 'file-manage' })
    }

    const getTableData = (fullName: string) => {
      if (fullName != "") {
        queryCurrentResourceByFullName(
            {
              type: 'FILE',
              fullName: fullName as string,
              tenantCode: tenantCode.value,
            }
        ).then((res: ResourceFile) => {
          if (res.fullName) {
            const dirs = res.fullName.split('/')
            if (dirs && dirs.length > 1) {
              dirs.pop()
              breadList.value = dirs
            }
          }
        })
      } else {
        breadList.value = [] as String[]
      }
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    onMounted(() => {
      const currfullName = String(router.currentRoute.value.query.prefix || "")
      if (currfullName === "") {
        fileStore.setCurrentDir('/')
      } else {
        fileStore.setCurrentDir(currfullName)
      }
    })

    onMounted(() => {
      getTableData(fullName.value)
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
      handleGoBread,
      handleGoResourceManage,
      pagination: paginationReactive,
      renameInfo,
      breadList,
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
              <Search
                placeholder = {t('resource.file.enter_keyword_tips')}
                v-model:value={this.searchRef}
                onSearch={handleConditions}
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
            header: () => (
              <NBreadcrumb separator='>' class={styles['breadcrumb']}>
                <NBreadcrumbItem>
                  <NButton text onClick={() => this.handleGoResourceManage()}>
                    {t('resource.file.file_manage')}
                  </NButton>
                </NBreadcrumbItem>
                {this.breadList.map((item, index) => (
                  <NBreadcrumbItem>
                    <NButton
                        text
                        disabled={index === this.breadList.length - 1}
                        onClick={() => this.handleGoBread(index)}>
                      {item}
                    </NButton>
                  </NBreadcrumbItem>
                ))}
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
