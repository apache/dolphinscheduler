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

import {
  defineComponent,
  Ref,
  toRefs,
  onMounted,
  toRef,
  watch,
  getCurrentInstance
} from 'vue'
import {
  NIcon,
  NSpace,
  NDataTable,
  NButton,
  NPagination,
  NInput,
  NBreadcrumb,
  NBreadcrumbItem
} from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { useTable } from './use-table'
import { SearchOutlined } from '@vicons/antd'
import Card from '@/components/card'
import FolderModal from './components/folder-modal'
import UploadModal from './components/upload-modal'
import styles from './index.module.scss'

export default defineComponent({
  name: 'resource-manage',
  setup() {
    const { variables, createColumns, getTableData, goUdfManage, goBread } =
      useTable()

    const requestData = () => {
      getTableData({
        id: variables.id,
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal
      })
    }

    const handleUpdateList = () => {
      requestData()
    }

    const handleChangePageSize = () => {
      variables.page = 1
      requestData()
    }

    const handleSearch = () => {
      variables.page = 1
      requestData()
    }

    const handleShowModal = (showRef: Ref<Boolean>) => {
      showRef.value = true
    }

    const handleCreateFolder = () => {
      variables.row = {}
      handleShowModal(toRef(variables, 'folderShowRef'))
    }

    const handleUploadFile = () => {
      handleShowModal(toRef(variables, 'uploadShowRef'))
    }

    const handleBread = (index: number) => {
      let breadName = ''
      variables.breadList.forEach((item, i) => {
        if (i <= index) {
          breadName = breadName + '/' + item
        }
      })
      goBread(breadName)
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    watch(useI18n().locale, () => {
      createColumns(variables)
    })

    onMounted(() => {
      createColumns(variables)
      requestData()
    })

    return {
      goUdfManage,
      handleBread,
      requestData,
      handleSearch,
      handleUpdateList,
      handleCreateFolder,
      handleUploadFile,
      handleChangePageSize,
      ...toRefs(variables),
      trim
    }
  },
  render() {
    const { t } = useI18n()
    const { loadingRef } = this

    return (
      <NSpace vertical>
        <Card>
          <NSpace justify='space-between'>
            <NSpace>
              <NButton
                type='primary'
                size='small'
                onClick={this.handleCreateFolder}
                class='btn-create-directory'
              >
                {t('resource.udf.create_folder')}
              </NButton>
              <NButton
                strong
                size='small'
                secondary
                onClick={this.handleUploadFile}
                class='btn-upload-udf'
              >
                {t('resource.udf.upload_udf_resources')}
              </NButton>
            </NSpace>
            <NSpace>
              <NInput
                allowInput={this.trim}
                size='small'
                placeholder={t('resource.udf.enter_keyword_tips')}
                v-model={[this.searchVal, 'value']}
              />
              <NButton type='primary' size='small' onClick={this.handleSearch}>
                <NIcon>
                  <SearchOutlined />
                </NIcon>
              </NButton>
            </NSpace>
          </NSpace>
        </Card>
        <Card title={t('resource.udf.udf_resources')}>
          {{
            header: () => (
              <NBreadcrumb separator='>'>
                <NBreadcrumbItem>
                  <NButton text onClick={() => this.goUdfManage()}>
                    {t('resource.udf.udf_resources')}
                  </NButton>
                </NBreadcrumbItem>
                {this.breadList.map((item, index) => (
                  <NBreadcrumbItem>
                    <NButton
                      text
                      disabled={index === this.breadList.length - 1}
                      onClick={() => this.handleBread(index)}
                    >
                      {item}
                    </NButton>
                  </NBreadcrumbItem>
                ))}
              </NBreadcrumb>
            ),
            default: () => (
              <NSpace vertical>
                <NDataTable
                  loading={loadingRef}
                  columns={this.columns}
                  data={this.tableData}
                  striped
                  size={'small'}
                  class={styles.table}
                  row-class-name='items'
                  scrollX={this.tableWidth}
                />
                <NSpace justify='center'>
                  <NPagination
                    v-model:page={this.page}
                    v-model:page-size={this.pageSize}
                    page-count={this.totalPage}
                    show-size-picker
                    page-sizes={[10, 30, 50]}
                    show-quick-jumper
                    onUpdatePage={this.requestData}
                    onUpdatePageSize={this.handleChangePageSize}
                  />
                </NSpace>
              </NSpace>
            )
          }}
        </Card>
        <FolderModal
          v-model:row={this.row}
          v-model:show={this.folderShowRef}
          onUpdateList={this.handleUpdateList}
        />
        <UploadModal
          v-model:show={this.uploadShowRef}
          onUpdateList={this.handleUpdateList}
        />
      </NSpace>
    )
  }
})
