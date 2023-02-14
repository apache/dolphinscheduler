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
  UploadOutlined
} from '@vicons/antd'
import _ from 'lodash'
import { useI18n } from 'vue-i18n'
import {
  ResourceFileTableData,
  IRenameResource,
  IRtDisb,
  IReuploadResource
} from '../types'
import { fileTypeArr } from '@/common/common'
import { downloadResource, deleteResource } from '@/service/modules/resources'
import type { Router } from 'vue-router'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  row: {
    type: Object as PropType<ResourceFileTableData>,
    default: {
      name: '',
      description: '',
      user_name: ''
    }
  }
}

export default defineComponent({
  name: 'TableAction',
  props,
  emits: ['updateList', 'reuploadResource', 'renameResource'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const router: Router = useRouter()

    const rtDisb: IRtDisb = (name, size) => {
      const i = name.lastIndexOf('.')
      const a = name.substring(i, name.length)
      const flag = _.includes(fileTypeArr, _.trimStart(a, '.'))
      return !(flag && size < 1000000)
    }

    const handleEditFile = (item: { fullName: string; user_name: string }) => {
      router.push({
        name: 'resource-file-edit',
        query: { prefix: item.fullName, tenantCode: item.user_name }
      })
    }

    const handleDeleteFile = (fullNameObj: {
      fullName: string
      tenantCode: string
    }) => {
      deleteResource(fullNameObj).then(() => emit('updateList'))
    }

    const handleReuploadFile: IReuploadResource = (
      name: string,
      description: string,
      fullName: string,
      user_name: string
    ) => {
      emit('reuploadResource', name, description, fullName, user_name)
    }

    const handleRenameFile: IRenameResource = (
      name: string,
      description: string,
      fullName: string,
      user_name: string
    ) => {
      emit('renameResource', name, description, fullName, user_name)
    }

    return {
      t,
      rtDisb,
      handleEditFile,
      handleDeleteFile,
      handleReuploadFile,
      handleRenameFile,
      ...props
    }
  },
  render() {
    const { t } = useI18n()
    return (
      <NSpace>
        {this.row.type !== 'UDF' && (
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
                    this.handleEditFile({
                      fullName: this.row.fullName,
                      user_name: this.row.user_name
                    })
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
        )}
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('resource.file.reupload'),
            trigger: () => (
              <NButton
                size='tiny'
                type='info'
                onClick={() =>
                  this.handleReuploadFile(
                    this.row.name,
                    this.row.description,
                    this.row.fullName,
                    this.row.user_name
                  )
                }
                disabled={!!this.row?.directory}
                style={{ marginRight: '-5px' }}
                circle
                class='btn-reupload'
              >
                <NIcon>
                  <UploadOutlined />
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
                onClick={() => {
                  this.handleRenameFile(
                    this.row.name,
                    this.row.description,
                    this.row.fullName,
                    this.row.user_name
                  )
                }}
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
                disabled={!!this.row?.directory}
                tag='div'
                circle
                style={{ marginRight: '-5px' }}
                onClick={() =>
                  downloadResource({ fullName: this.row.fullName })
                }
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
                    this.handleDeleteFile({
                      fullName: this.row.fullName,
                      tenantCode: this.row.user_name
                    })
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
