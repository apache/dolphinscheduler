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
import { NSpace, NTooltip, NButton, NIcon, NPopconfirm } from 'naive-ui'
import { DeleteOutlined, EditOutlined, InfoCircleFilled } from '@vicons/antd'
import { deleteProject } from '@/service/modules/projects'
import type { ProjectList } from '@/service/modules/projects/types'

interface ProjectRow extends ProjectList {
  index: number
}

const props = {
  row: {
    type: Object as PropType<ProjectRow>,
    default: {}
  }
}

const TableAction = defineComponent({
  name: 'TableAction',
  props,
  emits: ['resetTableData', 'updateProjectItem'],
  setup(props, { emit }) {
    const { t } = useI18n()

    const handleEditProject = (
      code: number,
      projectName: string,
      description: string
    ) => {
      emit('updateProjectItem', code, projectName, description)
    }

    const handleDeleteProject = (code: number) => {
      deleteProject(code).then(() => {
        emit('resetTableData')
      })
    }

    return { t, handleEditProject, handleDeleteProject }
  },
  render() {
    const { t, handleEditProject, handleDeleteProject } = this

    return (
      <NSpace>
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('project.list.edit'),
            trigger: () => (
              <NButton
                size='small'
                type='info'
                tag='div'
                onClick={() =>
                  handleEditProject(
                    this.row.code,
                    this.row.name,
                    this.row.description
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
            default: () => t('project.list.delete'),
            trigger: () => (
              <NButton size='small' type='error' circle>
                <NPopconfirm
                  positive-text={t('project.list.confirm')}
                  negative-text={t('project.list.cancel')}
                  onPositiveClick={() => handleDeleteProject(this.row.code)}
                >
                  {{
                    default: () => t('project.list.delete_confirm'),
                    icon: () => (
                      <NIcon>
                        <InfoCircleFilled />
                      </NIcon>
                    ),
                    trigger: () => (
                      <NIcon>
                        <DeleteOutlined />
                      </NIcon>
                    )
                  }}
                </NPopconfirm>
              </NButton>
            )
          }}
        </NTooltip>
      </NSpace>
    )
  }
})

export default TableAction
