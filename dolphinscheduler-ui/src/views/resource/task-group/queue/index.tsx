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

import { ref, defineComponent, toRefs, reactive, onMounted, Ref } from 'vue'
import {
  NButton,
  NIcon,
  NInput,
  NCard,
  NDataTable,
  NPagination,
  NSelect,
  NSpace
} from 'naive-ui'
import Card from '@/components/card'
import { SearchOutlined } from '@vicons/antd'
import { useI18n } from 'vue-i18n'
import styles from './index.module.scss'
import { useTable } from './use-table'
import FormModal from '@/views/resource/task-group/queue/components/form-modal'
import { queryTaskGroupListPaging } from '@/service/modules/task-group'
import { TaskGroupRes } from '@/service/modules/task-group/types'
import { SelectMixedOption } from 'naive-ui/lib/select/src/interface'
import { Router, useRouter } from 'vue-router'

const taskGroupQueue = defineComponent({
  name: 'taskGroupQueue',
  setup() {
    const router: Router = useRouter()
    const { t } = useI18n()
    const { variables, getTableData } = useTable()
    const showModalRef = ref(false)
    const taskGroupOptions: Ref<Array<SelectMixedOption>> = ref([])

    const idRef = ref(Number(router.currentRoute.value.params.id))

    const searchParamRef = reactive({
      groupId: ref<number | null>(),
      processName: '',
      instanceName: '',
      pageSize: 10,
      pageNo: 1
    })

    let updateItemData = reactive({
      queueId: 0,
      priority: 0
    })

    const resetTableData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        groupId: searchParamRef.groupId,
        taskInstanceName: searchParamRef.instanceName,
        processInstanceName: searchParamRef.processName
      })
    }

    const onCancel = () => {
      showModalRef.value = false
    }

    const onConfirm = () => {
      showModalRef.value = false
      updateItemData = {
        queueId: 0,
        priority: 0
      }
      resetTableData()
    }

    const onUpdatePageSize = () => {
      variables.page = 1
      resetTableData()
    }

    const updatePriority = (queueId: number, priority: number) => {
      showModalRef.value = true
      updateItemData.queueId = queueId
      updateItemData.priority = priority
    }

    const onSearch = () => {
      resetTableData()
    }

    onMounted(() => {
      const taskGroupOptionsParams = {
        pageNo: 1,
        pageSize: 2147483647
      }
      if (idRef.value) {
        searchParamRef.groupId = idRef.value
      }
      queryTaskGroupListPaging(taskGroupOptionsParams).then(
        (res: TaskGroupRes) => {
          res.totalList.map((item) => {
            if (!searchParamRef.groupId) {
              searchParamRef.groupId = item.id
            }
            const option: SelectMixedOption = {
              label: item.name,
              value: item.id
            }
            taskGroupOptions.value.push(option)
          })
        }
      )

      resetTableData()
    })

    return {
      ...toRefs(variables),
      t,
      onSearch,
      searchParamRef,
      resetTableData,
      onUpdatePageSize,
      updatePriority,
      onCancel,
      onConfirm,
      showModalRef,
      updateItemData,
      taskGroupOptions
    }
  },
  render() {
    const {
      t,
      resetTableData,
      onUpdatePageSize,
      updatePriority,
      onCancel,
      onConfirm,
      onSearch,
      showModalRef,
      updateItemData,
      taskGroupOptions,
      loadingRef
    } = this

    const { columns } = useTable(updatePriority, resetTableData)

    return (
      <div>
        <NCard>
          <div class={styles.toolbar}>
            <NSpace>
              <NSelect
                size='small'
                options={taskGroupOptions}
                v-model:value={this.searchParamRef.groupId}
                placeholder={t('resource.task_group_queue.task_group_name')}
              />
              <NInput
                size='small'
                v-model={[this.searchParamRef.processName, 'value']}
                placeholder={t(
                  'resource.task_group_queue.workflow_instance_name'
                )}
              ></NInput>
              <NInput
                size='small'
                v-model={[this.searchParamRef.instanceName, 'value']}
                placeholder={t('resource.task_group_queue.task_instance_name')}
              ></NInput>
              <NButton size='small' type='primary' onClick={onSearch}>
                <NIcon>
                  <SearchOutlined />
                </NIcon>
              </NButton>
            </NSpace>
          </div>
        </NCard>
        <Card
          class={styles['table-card']}
          title={t('resource.task_group_queue.queue')}
        >
          <div>
            <NDataTable
              loading={loadingRef}
              columns={columns}
              size={'small'}
              data={this.tableData}
              striped
              scrollX={this.tableWidth}
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
        {showModalRef && (
          <FormModal
            show={showModalRef}
            onCancel={onCancel}
            onConfirm={onConfirm}
            data={updateItemData}
          />
        )}
      </div>
    )
  }
})

export default taskGroupQueue
