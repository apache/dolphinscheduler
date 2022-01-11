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
import { defineComponent, PropType } from 'vue'
import { NSpace, NTooltip, NButton, NIcon, NPopconfirm } from 'naive-ui'
import {
  DeleteOutlined,
  DownloadOutlined,
  FormOutlined,
  EditOutlined,
  InfoCircleFilled,
} from '@vicons/antd'
import _ from 'lodash'
import { useI18n } from 'vue-i18n'
import { ResourceFileTableData } from '../types'
import { fileTypeArr } from '@/utils/common'
import { downloadResource, deleteResource } from '@/service/modules/resources'
import { IRenameFile, IRtDisb } from '../types'
import type { Router } from 'vue-router'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false,
  },
  row: {
    type: Object as PropType<ResourceFileTableData>,
    default: {
      id: -1,
      name: '',
      description: '',
    },
  },
}

export default defineComponent({
  name: 'TableAction',
  props,
  emits: ['updateList', 'renameResource'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const router: Router = useRouter()

    const rtDisb: IRtDisb = (name, size) => {
      const i = name.lastIndexOf('.')
      const a = name.substring(i, name.length)
      let flag = _.includes(fileTypeArr, _.trimStart(a, '.'))
      if (flag && size < 1000000) {
        flag = true
      } else {
        flag = false
      }
      return !flag
    }

    const handleEditFile = (item: { id: number }) => {
      router.push({ name: 'resource-file-edit', params: { id: item.id } })
    }

    const handleDeleteFile = (id: number) => {
      deleteResource(id).then(() => emit('updateList'))
    }

    const handleRenameFile: IRenameFile = (id, name, description) => {
      emit('renameResource', id, name, description)
    }

    return {
      t,
      rtDisb,
      handleEditFile,
      handleDeleteFile,
      handleRenameFile,
      ...props,
    }
  },
  render() {
    const { t } = useI18n()
    return (
      <NSpace>
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('resource.edit'),
            trigger: () => (
              <NButton
                size='small'
                type='info'
                disabled={this.rtDisb(this.row.name, this.row.size)}
                tag='div'
                onClick={() => {
                  this.handleEditFile(this.row)
                }}
                circle
              >
                <NIcon>
                  <FormOutlined />
                </NIcon>
              </NButton>
            ),
          }}
        </NTooltip>
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('resource.rename'),
            trigger: () => (
              <NButton
                size='small'
                type='info'
                onClick={() =>
                  this.handleRenameFile(
                    this.row.id,
                    this.row.name,
                    this.row.description,
                  )
                }
                circle
              >
                <NIcon>
                  <EditOutlined />
                </NIcon>
              </NButton>
            ),
          }}
        </NTooltip>
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('resource.download'),
            trigger: () => (
              <NButton
                size='small'
                type='info'
                disabled={this.row?.directory ? true : false}
                tag='div'
                circle
                onClick={() => downloadResource(this.row.id)}
              >
                <NIcon>
                  <DownloadOutlined />
                </NIcon>
              </NButton>
            ),
          }}
        </NTooltip>
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('resource.delete'),
            trigger: () => (
              <NButton size='small' type='error' circle>
                <NPopconfirm
                  positive-text={t('resource.confirm')}
                  negative-text={t('resource.cancel')}
                  onPositiveClick={() => {
                    this.handleDeleteFile(this.row.id)
                  }}
                >
                  {{
                    default: () => t('resource.delete_confirm'),
                    icon: () => (
                      <NIcon>
                        <InfoCircleFilled />
                      </NIcon>
                    ),
                    trigger: () => (
                      <NIcon>
                        <DeleteOutlined />
                      </NIcon>
                    ),
                  }}
                </NPopconfirm>
              </NButton>
            ),
          }}
        </NTooltip>
      </NSpace>
    )
  },
})
