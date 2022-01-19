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

import { defineComponent, onMounted, ref, toRefs, reactive } from 'vue'
import {
  NCard,
  NButton,
  NInput,
  NIcon,
  NDataTable,
  NPagination
} from 'naive-ui'
import { SearchOutlined } from '@vicons/antd'
import { useI18n } from 'vue-i18n'
import { useTable } from './use-table'
import styles from './index.module.scss'
import Card from '@/components/card'
import ProjectModal from './components/project-modal'

const list = defineComponent({
  name: 'list',
  setup() {
    const showModalRef = ref(false)
    const modelStatusRef = ref(0)
    const { t } = useI18n()
    const { variables, getTableData } = useTable()
    let updateProjectData = reactive({
      code: 0,
      projectName: '',
      description: ''
    })

    const requestData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal
      })
    }

    const onCancel = () => {
      showModalRef.value = false
    }

    const onConfirm = () => {
      showModalRef.value = false
      updateProjectData = {
        code: 0,
        projectName: '',
        description: ''
      }
      resetTableData()
    }

    const onOpen = () => {
      modelStatusRef.value = 0
      showModalRef.value = true
    }

    const resetTableData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal
      })
    }

    const onSearch = () => {
      variables.page = 1
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal
      })
    }

    const onUpdatePageSize = () => {
      variables.page = 1
      resetTableData()
    }

    const updateProjectItem = (
      code: number,
      projectName: string,
      description: string
    ) => {
      modelStatusRef.value = 1
      showModalRef.value = true
      updateProjectData.code = code
      updateProjectData.projectName = projectName
      updateProjectData.description = description
    }

    onMounted(() => {
      requestData()
    })

    return {
      t,
      showModalRef,
      ...toRefs(variables),
      onCancel,
      onConfirm,
      onOpen,
      updateProjectItem,
      resetTableData,
      onUpdatePageSize,
      onSearch,
      updateProjectData,
      modelStatusRef
    }
  },
  render() {
    const {
      t,
      showModalRef,
      onCancel,
      onConfirm,
      onOpen,
      updateProjectItem,
      resetTableData,
      onUpdatePageSize,
      onSearch,
      updateProjectData,
      modelStatusRef
    } = this
    const { columns } = useTable(updateProjectItem, resetTableData)

    return (
      <div>
        <NCard>
          <div class={styles['search-card']}>
            <div>
              <NButton size='small' type='primary' onClick={onOpen}>
                {t('project.list.create_project')}
              </NButton>
            </div>
            <div class={styles.box}>
              <NInput
                size='small'
                v-model:value={this.searchVal}
                placeholder={t('project.list.project_tips')}
                clearable
              />
              <NButton size='small' type='primary' onClick={onSearch}>
                {{
                  icon: () => (
                    <NIcon>
                      <SearchOutlined />
                    </NIcon>
                  )
                }}
              </NButton>
            </div>
          </div>
        </NCard>
        <Card
          title={t('project.list.project_list')}
          class={styles['table-card']}
        >
          <NDataTable
            columns={columns}
            size={'small'}
            data={this.tableData}
            striped
          />
          <div class={styles.pagination}>
            <NPagination
              v-model:page={this.page}
              v-model:page-size={this.pageSize}
              page-count={this.totalPage}
              show-size-picker
              page-sizes={[10, 30, 50]}
              show-quick-jumper
              onUpdatePage={resetTableData}
              onUpdatePageSize={onUpdatePageSize}
            />
          </div>
        </Card>
        {showModalRef && (
          <ProjectModal
            show={showModalRef}
            onCancel={onCancel}
            onConfirm={onConfirm}
            data={updateProjectData}
            status={modelStatusRef}
          />
        )}
      </div>
    )
  }
})

export default list
