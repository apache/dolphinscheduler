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
  getCurrentInstance,
  PropType,
  toRefs,
  watch
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
import { useFileStore } from '@/store/file/file'
import Card from '@/components/card'
import ResourceFolderModal from './folder'
import ResourceUploadModal from './upload'
import ResourceRenameModal from './rename'
import styles from './index.module.scss'
import type { Router } from 'vue-router'
import Search from '@/components/input-search'
import { ResourceType } from '@/views/resource/components/resource/types'
import { useUserStore } from '@/store/user/user'

const props = {
  resourceType: {
    type: String as PropType<ResourceType>,
    default: undefined
  }
}

export default defineComponent({
  name: 'ResourceList',
  props,
  setup(props) {
    const router: Router = useRouter()
    const fileStore = useFileStore()
    const breadListRef = ref<Array<string>>()

    const {
      variables,
      tableWidth,
      requestData,
      updateList,
      createColumns,
      handleCreateFile
    } = useTable()

    const userStore = useUserStore()

    variables.resourceType = props.resourceType

    const handleUpdatePage = (page: number) => {
      variables.pagination.page = page
      requestData()
    }

    const handleUpdatePageSize = (pageSize: number) => {
      variables.pagination.page = 1
      variables.pagination.pageSize = pageSize
      requestData()
    }

    const handleConditions = () => {
      requestData()
    }

    const handleCreateFolder = () => {
      variables.folderShowRef = true
    }

    const handleUploadFile = () => {
      variables.isReupload = false
      variables.uploadShowRef = true
    }

    const handleRenameFile = () => {
      variables.renameShowRef = true
    }

    onMounted(() => {
      createColumns(variables)
      fileStore.setCurrentDir(variables.fullName)
      breadListRef.value = fileStore.getCurrentDir
        .replace(/\/+$/g, '')
        .split('/')
        .slice(2) as Array<string>
      requestData()
    })

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    const handleBread = (index: number) => {
      const breadName =
        variables.fullName
          .split('/')
          .slice(0, index + 3)
          .join('/') + '/'
      goBread(breadName)
    }

    const goBread = (fullName: string) => {
      const { resourceType, tenantCode } = variables
      const baseDir =
        resourceType === 'UDF'
          ? userStore.getBaseUdfDir
          : userStore.getBaseResDir
      if (fullName === '' || !fullName.startsWith(baseDir)) {
        router.push({
          name: resourceType === 'UDF' ? 'resource-manage' : 'file-manage'
        })
      } else {
        router.push({
          name:
            resourceType === 'UDF'
              ? 'resource-sub-manage'
              : 'resource-file-subdirectory',
          query: { prefix: fullName, tenantCode: tenantCode }
        })
      }
    }

    watch(useI18n().locale, () => {
      createColumns(variables)
    })

    return {
      breadListRef,
      tableWidth,
      updateList,
      handleConditions,
      handleCreateFolder,
      handleCreateFile,
      handleUploadFile,
      handleRenameFile,
      handleUpdatePage,
      handleUpdatePageSize,
      handleBread,
      trim,
      ...toRefs(variables)
    }
  },
  render() {
    const { t } = useI18n()

    const {
      handleConditions,
      handleCreateFolder,
      handleCreateFile,
      handleUploadFile,
      tableWidth
    } = this
    const manageTitle =
      this.resourceType === 'UDF'
        ? t('resource.udf.udf_resources')
        : t('resource.file.file_manage')

    return (
      <NSpace vertical>
        <Card>
          <NSpace justify='space-between'>
            <NButtonGroup size='small'>
              <NButton
                type='primary'
                onClick={handleCreateFolder}
                class='btn-create-directory'
              >
                {t('resource.file.create_folder')}
              </NButton>
              {this.resourceType !== 'UDF' && (
                <NButton onClick={handleCreateFile} class='btn-create-file'>
                  {t('resource.file.create_file')}
                </NButton>
              )}
              <NButton onClick={handleUploadFile} class='btn-upload-resource'>
                {this.resourceType === 'UDF'
                  ? t('resource.udf.upload_udf_resources')
                  : t('resource.file.upload_files')}
              </NButton>
            </NButtonGroup>
            <NSpace>
              <Search
                placeholder={t('resource.file.enter_keyword_tips')}
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
        <Card title={manageTitle}>
          {{
            header: () => (
              <NBreadcrumb separator='>'>
                {this.breadListRef?.map((item, index) => (
                  <NBreadcrumbItem>
                    <NButton
                      text
                      disabled={
                        index > 0 && index === this.breadListRef!.length - 1
                      }
                      onClick={() => this.handleBread(index)}
                    >
                      {index === 0 ? manageTitle : item}
                    </NButton>
                  </NBreadcrumbItem>
                ))}
              </NBreadcrumb>
            ),
            default: () => (
              <NSpace vertical>
                <NDataTable
                  remote
                  columns={this.columns}
                  data={this.resourceList?.table}
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
          resourceType={this.resourceType}
          onUpdateList={this.updateList}
        />
        <ResourceUploadModal
          v-model:show={this.uploadShowRef}
          isReupload={this.isReupload}
          resourceType={this.resourceType}
          name={this.reuploadInfo.name}
          fullName={this.reuploadInfo.fullName}
          description={this.reuploadInfo.description}
          userName={this.reuploadInfo.user_name}
          onUpdateList={this.updateList}
        />
        <ResourceRenameModal
          v-model:show={this.renameShowRef}
          resourceType={this.resourceType}
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
