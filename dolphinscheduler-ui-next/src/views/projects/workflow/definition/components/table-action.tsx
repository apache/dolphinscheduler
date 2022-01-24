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
  PlayCircleOutlined,
  ClockCircleOutlined,
  CopyOutlined,
  FieldTimeOutlined,
  ImportOutlined,
  ExportOutlined
} from '@vicons/antd'
import { useI18n } from 'vue-i18n'
import { IDefinitionData } from '../types'
import type { Router } from 'vue-router'

const props = {
  row: {
    type: Object as PropType<IDefinitionData>
  }
}

export default defineComponent({
  name: 'TableAction',
  props,
  emits: ['updateList', 'renameResource'],
  setup(props, ctx) {
    const { t } = useI18n()
    const router: Router = useRouter()

    // TODO
    const handleEditFile = () => {}

    return {
      t,
      ...props
    }
  },
  render() {
    const { t } = useI18n()
    return (
      <NSpace>
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('project.workflow.edit'),
            trigger: () => (
              <NButton
                size='tiny'
                type='info'
                tag='div'
                style={{ marginRight: '-5px' }}
                circle
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
            default: () => t('project.workflow.start'),
            trigger: () => (
              <NButton
                size='tiny'
                type='primary'
                style={{ marginRight: '-5px' }}
                circle
              >
                <NIcon>
                  <PlayCircleOutlined />
                </NIcon>
              </NButton>
            )
          }}
        </NTooltip>
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('project.workflow.timing'),
            trigger: () => (
              <NButton
                size='tiny'
                type='info'
                tag='div'
                circle
                style={{ marginRight: '-5px' }}
              >
                <NIcon>
                  <ClockCircleOutlined />
                </NIcon>
              </NButton>
            )
          }}
        </NTooltip>
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('project.workflow.upline'),
            trigger: () => (
              <NButton
                size='tiny'
                type='error'
                circle
                style={{ marginRight: '-5px' }}
              >
                <NPopconfirm
                  positive-text={t('project.workflow.confirm')}
                  negative-text={t('project.workflow.cancel')}
                >
                  {{
                    default: () => t('project.workflow.copy_workflow'),
                    icon: () => (
                      <NIcon>
                        <DownloadOutlined />
                      </NIcon>
                    ),
                    trigger: () => (
                      <NIcon>
                        <DownloadOutlined />
                      </NIcon>
                    )
                  }}
                </NPopconfirm>
              </NButton>
            )
          }}
        </NTooltip>
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('project.workflow.copy_workflow'),
            trigger: () => (
              <NButton
                size='tiny'
                type='info'
                tag='div'
                style={{ marginRight: '-5px' }}
                circle
              >
                <NIcon>
                  <CopyOutlined />
                </NIcon>
              </NButton>
            )
          }}
        </NTooltip>
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('project.workflow.cron_manage'),
            trigger: () => (
              <NButton
                size='tiny'
                type='info'
                tag='div'
                style={{ marginRight: '-5px' }}
                circle
              >
                <NIcon>
                  <FieldTimeOutlined />
                </NIcon>
              </NButton>
            )
          }}
        </NTooltip>
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('project.workflow.delete'),
            trigger: () => (
              <NButton
                size='tiny'
                type='error'
                tag='div'
                style={{ marginRight: '-5px' }}
                circle
              >
                <NIcon>
                  <DeleteOutlined />
                </NIcon>
              </NButton>
            )
          }}
        </NTooltip>
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('project.workflow.tree_view'),
            trigger: () => (
              <NButton
                size='tiny'
                type='info'
                tag='div'
                style={{ marginRight: '-5px' }}
                circle
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
            default: () => t('project.workflow.export'),
            trigger: () => (
              <NButton
                size='tiny'
                type='info'
                tag='div'
                style={{ marginRight: '-5px' }}
                circle
              >
                <NIcon>
                  <ExportOutlined />
                </NIcon>
              </NButton>
            )
          }}
        </NTooltip>
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('project.workflow.version_info'),
            trigger: () => (
              <NButton
                size='tiny'
                type='info'
                tag='div'
                style={{ marginRight: '-5px' }}
                circle
              >
                <NIcon>
                  <InfoCircleFilled />
                </NIcon>
              </NButton>
            )
          }}
        </NTooltip>
      </NSpace>
    )
  }
})
