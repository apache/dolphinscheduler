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
  watch
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
import { useResource } from './use-resource'
import FolderModal from './components/folder-modal'
import UploadModal from './components/upload-modal'
import { IUdfResourceParam } from './types'
import { useTable } from './use-table'
import styles from './index.module.scss'

export default defineComponent({
  name: 'resource-manage',
  setup() {
    const idRef = ref(-1)
    const searchRef = ref()
    const folderShowRef = ref(false)
    const uploadShowRef = ref(false)
    const resourceRef = ref()
    const resourceVariables = reactive<IUdfResourceParam>({
      id: -1,
      pageSize: 10,
      pageNo: 1,
      searchVal: undefined,
      type: 'UDF'
    })

    const { getResource } = useResource()

    const handleUpdateList = () => {
      resourceRef.value = getResource(resourceVariables)
    }

    const handleSearch = () => {
      resourceRef.value = getResource({
        ...resourceVariables,
        id: idRef.value,
        searchVal: searchRef.value
      })
    }

    const handleShowModal = (showRef: Ref<Boolean>) => {
      showRef.value = true
    }

    const handleCreateFolder = () => {
      handleShowModal(folderShowRef)
    }

    const handleUploadFile = () => {
      handleShowModal(uploadShowRef)
    }

    onMounted(() => {
      resourceRef.value = getResource(resourceVariables)
    })

    return {
      searchRef,
      resourceRef,
      folderShowRef,
      uploadShowRef,
      handleUpdateList,
      handleSearch,
      handleCreateFolder,
      handleUploadFile
    }
  },
  render() {
    const { t } = useI18n()
    const { columnsRef } = useTable()
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
                    v-model={[this.searchRef, 'value']}
                  />
                </div>
              </div>
            </div>
          </div>
        </Card>
        <Card title={t('resource.udf.udf_resources')}>
          <NDataTable
            remote
            columns={columnsRef}
            data={this.resourceRef?.value.table}
            striped
            size={'small'}
            class={styles.table}
          />
        </Card>
        <FolderModal
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
