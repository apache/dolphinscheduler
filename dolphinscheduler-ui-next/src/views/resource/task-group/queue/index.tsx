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

import { ref, defineComponent, toRefs, reactive, onMounted } from 'vue'
import {
  NButton,
  NIcon,
  NInput,
  NCard,
  NDataTable,
  NPagination,
} from 'naive-ui'
import Card from '@/components/card'
import { SearchOutlined } from '@vicons/antd'
import { useI18n } from 'vue-i18n'
import styles from './index.module.scss'
import { useTable } from './use-table'

const taskGroupQueue = defineComponent({
  name: 'taskGroupQueue',
  setup() {
    const { t } = useI18n()
    const { variables, getTableData } = useTable()
    const modelStatusRef = ref(0)

    const searchParamRef = reactive({
      groupId: undefined,
      taskGroupName: '',
      processName: '',
      instanceName: '',
      pageSize: 10,
      pageNo: 1
    })

    let updateItemData = reactive({
      queueId: 0,
      priority: 0,
    })

    const requestData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        groupId: variables.groupId
      })
    }

    const resetTableData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        groupId: variables.groupId
      })
    }

    const onUpdatePageSize = () => {
      variables.page = 1
      resetTableData()
    }

    const updatePriority = (
      queueId: number,
      priority: number
    ) => {
      console.log('updatePriority')
      modelStatusRef.value = 1
      updateItemData.queueId = queueId
      updateItemData.priority = priority
    }

    const onSearch = () => {
      resetTableData()
    }

    onMounted(() => {
      requestData()
    })

    return {
      ...toRefs(variables),
      t,
      onSearch,
      searchParamRef,
      resetTableData,
      onUpdatePageSize,
      updatePriority,
      modelStatusRef,
      updateItemData
    }
  },
  render() {
    const {
      t,
      resetTableData,
      onUpdatePageSize,
      updatePriority,
      onSearch
    } = this

    const { columns } = useTable(updatePriority, resetTableData)

    return (
      <div>
        <NCard>
          <div class={styles.toolbar}>
            <div class={styles.right}>
              <NInput
                  size='small'
                  v-model={[this.searchParamRef.taskGroupName, 'value']}
                  placeholder={t(
                      'resource.task_group_queue.task_group_name'
                  )}
              ></NInput>
              <NInput
                  size='small'
                  v-model={[this.searchParamRef.processName, 'value']}
                  placeholder={t(
                      'resource.task_group_queue.process_name'
                  )}
              ></NInput>
              <NInput
                size='small'
                v-model={[this.searchParamRef.instanceName, 'value']}
                placeholder={t(
                  'resource.task_group_queue.process_instance_name'
                )}
              ></NInput>
              <NButton size='small' type='primary' onClick={onSearch}>
                <NIcon>
                  <SearchOutlined />
                </NIcon>
              </NButton>
            </div>
          </div>
        </NCard>
        <Card
          class={styles['table-card']}
          title={t('resource.task_group_option.option')}
        >
          <div>
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
          </div>
        </Card>
      </div>
    )
  }
})

export default taskGroupQueue
