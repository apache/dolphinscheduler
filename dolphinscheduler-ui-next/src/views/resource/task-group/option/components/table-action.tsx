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

import { defineComponent, PropType } from 'vue'
import { useI18n } from 'vue-i18n'
import type { Router } from 'vue-router'
import { NSpace, NTooltip, NButton, NIcon, NSwitch } from 'naive-ui'
import { EditOutlined, UnorderedListOutlined } from '@vicons/antd'
import type {
  TaskGroupIdReq,
  TaskGroup
} from '@/service/modules/task-group/types'
import { startTaskGroup, closeTaskGroup } from '@/service/modules/task-group'
import { useRouter } from 'vue-router'

interface ItemRow extends TaskGroup {
  projectList: []
}

const props = {
  row: {
    type: Object as PropType<ItemRow>,
    default: {}
  }
}

const TableAction = defineComponent({
  name: 'TableAction',
  props,
  emits: ['resetTableData', 'updateItem'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const router: Router = useRouter()

    const handleEdit = (
      id: number,
      name: string,
      projectCode: number,
      groupSize: number,
      description: string,
      status: number
    ) => {
      emit('updateItem', id, name, projectCode, groupSize, description, status)
    }

    const handleSwitchStatus = (value: number, id: number) => {
      const params: TaskGroupIdReq = { id: id }

      if (value === 1) {
        startTaskGroup(params).then(() => {
          emit('resetTableData')
        })
      } else if (value === 0) {
        closeTaskGroup(params).then(() => {
          emit('resetTableData')
        })
      }
    }

    const handleViewQueue = (id: number) => {
      router.push({ name: 'task-group-queue', params: { id: id } })
    }

    return { t, handleEdit, handleViewQueue, handleSwitchStatus }
  },
  render() {
    const { t, handleEdit, handleViewQueue, handleSwitchStatus } = this

    return (
      <NSpace>
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('resource.task_group_option.switch_status'),
            trigger: () => (
              <NSwitch
                v-model={[this.row.status, 'value']}
                checkedValue={1}
                uncheckedValue={0}
                onUpdate:value={(value) =>
                  handleSwitchStatus(value, this.row.id)
                }
              />
            )
          }}
        </NTooltip>
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('resource.task_group_option.edit'),
            trigger: () => (
              <NButton
                size='small'
                type='info'
                tag='div'
                onClick={() =>
                  handleEdit(
                    this.row.id,
                    this.row.name,
                    this.row.projectCode,
                    this.row.groupSize,
                    this.row.description,
                    this.row.status
                  )
                }
                circle
              >
                <NIcon>
                  <EditOutlined />
                </NIcon>
              </NButton>
            )
          }}
        </NTooltip>
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('resource.task_group_option.view_queue'),
            trigger: () => (
              <NButton
                size='small'
                type='primary'
                tag='div'
                onClick={() => handleViewQueue(this.row.id)}
                circle
              >
                <NIcon>
                  <UnorderedListOutlined />
                </NIcon>
              </NButton>
            )
          }}
        </NTooltip>
      </NSpace>
    )
  }
})

export default TableAction
