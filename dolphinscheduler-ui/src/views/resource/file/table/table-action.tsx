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
  InfoCircleFilled
} from '@vicons/antd'
import _ from 'lodash'
import { useI18n } from 'vue-i18n'
import { ResourceFileTableData } from '../types'
import { fileTypeArr } from '@/common/common'
import { downloadResource, deleteResource } from '@/service/modules/resources'
import { IRenameFile, IRtDisb } from '../types'
import type { Router } from 'vue-router'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  row: {
    type: Object as PropType<ResourceFileTableData>,
    default: {
      id: -1,
      name: '',
      description: ''
    }
  }
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
      const flag = _.includes(fileTypeArr, _.trimStart(a, '.'))
      return !(flag && size < 1000000)
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
      ...props
    }
  },
  render() {
    const { t } = useI18n()
    return (
      <NSpace>
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('resource.file.edit'),
            trigger: () => (
              <NButton
                size='tiny'
                type='info'
                disabled={this.rtDisb(this.row.name, this.row.size)}
                tag='div'
                onClick={() => {
                  this.handleEditFile(this.row)
                }}
                style={{ marginRight: '-5px' }}
                circle
                class='btn-edit'
              >
                <NIcon>
                  <FormOutlined />
                </NIcon>
              </NButton>
            )
          }}
        </NTooltip>
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('resource.file.rename'),
            trigger: () => (
              <NButton
                size='tiny'
                type='info'
                onClick={() =>
                  this.handleRenameFile(
                    this.row.id,
                    this.row.name,
                    this.row.description
                  )
                }
                style={{ marginRight: '-5px' }}
                circle
                class='btn-rename'
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
            default: () => t('resource.file.download'),
            trigger: () => (
              <NButton
                size='tiny'
                type='info'
                disabled={this.row?.directory ? true : false}
                tag='div'
                circle
                style={{ marginRight: '-5px' }}
                onClick={() => downloadResource(this.row.id)}
                class='btn-download'
              >
                <NIcon>
                  <DownloadOutlined />
                </NIcon>
              </NButton>
            )
          }}
        </NTooltip>
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('resource.file.delete'),
            trigger: () => (
              <NButton size='tiny' type='error' circle class='btn-delete'>
                <NPopconfirm
                  positive-text={t('resource.file.confirm')}
                  negative-text={t('resource.file.cancel')}
                  onPositiveClick={() => {
                    this.handleDeleteFile(this.row.id)
                  }}
                >
                  {{
                    default: () => t('resource.file.delete_confirm'),
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
