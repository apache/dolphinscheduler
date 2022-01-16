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
  ref,
  defineComponent,
  reactive,
  Ref,
  toRefs,
  ShallowRef,
  onMounted,
  unref,
  watch,
  toRef
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
import FolderModal from './components/folder-modal'
import UploadModal from './components/upload-modal'
import { useTable } from './use-table'
import styles from './index.module.scss'

export default defineComponent({
  name: 'resource-manage',
  setup() {
    const { variables, getTableData } = useTable()

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

    onMounted(() => {
      requestData()
    })

    return {
      ...toRefs(variables),
      requestData,
      handleSearch,
      handleUpdateList,
      handleCreateFolder,
      handleUploadFile,
      handleChangePageSize
    }
  },
  render() {
    const { t } = useI18n()

    return (
      <div class={styles.content}>
        <Card class={styles.card}>
          <div class={styles.header}>
            <NSpace>
              <NButtonGroup>
                <NButton onClick={this.handleCreateFolder}>
                  {t('resource.udf.create_folder')}
                </NButton>
                <NButton onClick={this.handleUploadFile}>
                  {t('resource.udf.upload_udf_resources')}
                </NButton>
              </NButtonGroup>
            </NSpace>
            <div class={styles.right}>
              <div class={styles.search}>
                <div class={styles.list}>
                  <NButton onClick={this.handleSearch}>
                    <NIcon>
                      <SearchOutlined />
                    </NIcon>
                  </NButton>
                </div>
                <div class={styles.list}>
                  <NInput
                    placeholder={t('resource.udf.enter_keyword_tips')}
                    v-model={[this.searchVal, 'value']}
                  />
                </div>
              </div>
            </div>
          </div>
        </Card>
        <Card title={t('resource.udf.udf_resources')}>
          <NDataTable
            remote
            columns={this.columns}
            data={this.tableData}
            striped
            size={'small'}
            class={styles.table}
          />
          <div class={styles.pagination}>
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
          </div>
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
      </div>
    )
  }
})
