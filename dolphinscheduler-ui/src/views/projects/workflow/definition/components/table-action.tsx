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

import { defineComponent, PropType, toRefs } from 'vue'
import { NSpace, NTooltip, NButton, NIcon, NPopconfirm } from 'naive-ui'
import {
  DeleteOutlined,
  DownloadOutlined,
  FormOutlined,
  InfoCircleFilled,
  PlayCircleOutlined,
  ClockCircleOutlined,
  CopyOutlined,
  FieldTimeOutlined,
  ExportOutlined,
  ApartmentOutlined,
  UploadOutlined
} from '@vicons/antd'
import { useI18n } from 'vue-i18n'
import { IDefinitionData } from '../types'

const props = {
  row: {
    type: Object as PropType<IDefinitionData>
  }
}

export default defineComponent({
  name: 'TableAction',
  props,
  emits: [
    'editWorkflow',
    'updateList',
    'startWorkflow',
    'timingWorkflow',
    'versionWorkflow',
    'deleteWorkflow',
    'releaseWorkflow',
    'copyWorkflow',
    'exportWorkflow',
    'gotoTimingManage',
    'gotoWorkflowTree'
  ],
  setup(props, ctx) {
    const handleEditWorkflow = () => {
      ctx.emit('editWorkflow')
    }

    const handleStartWorkflow = () => {
      ctx.emit('startWorkflow')
    }

    const handleTimingWorkflow = () => {
      ctx.emit('timingWorkflow')
    }

    const handleVersionWorkflow = () => {
      ctx.emit('versionWorkflow')
    }

    const handleDeleteWorkflow = () => {
      ctx.emit('deleteWorkflow')
    }

    const handleReleaseWorkflow = () => {
      ctx.emit('releaseWorkflow')
    }

    const handleCopyWorkflow = () => {
      ctx.emit('copyWorkflow')
    }

    const handleExportWorkflow = () => {
      ctx.emit('exportWorkflow')
    }

    const handleGotoTimingManage = () => {
      ctx.emit('gotoTimingManage')
    }

    const handleGotoWorkflowTree = () => {
      ctx.emit('gotoWorkflowTree')
    }

    return {
      handleEditWorkflow,
      handleStartWorkflow,
      handleTimingWorkflow,
      handleVersionWorkflow,
      handleDeleteWorkflow,
      handleReleaseWorkflow,
      handleCopyWorkflow,
      handleExportWorkflow,
      handleGotoTimingManage,
      handleGotoWorkflowTree,
      ...toRefs(props)
    }
  },
  render() {
    const { t } = useI18n()
    const releaseState = this.row?.releaseState
    const scheduleReleaseState = this.row?.scheduleReleaseState

    return (
      <NSpace>
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('project.workflow.edit'),
            trigger: () => (
              <NButton
                size='small'
                type='info'
                tag='div'
                circle
                onClick={this.handleEditWorkflow}
                disabled={releaseState === 'ONLINE'}
                class='btn-edit'
                /* TODO: Edit workflow */
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
                size='small'
                type='primary'
                tag='div'
                circle
                onClick={this.handleStartWorkflow}
                disabled={releaseState === 'OFFLINE'}
                class='btn-run'
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
                size='small'
                type='info'
                tag='div'
                circle
                onClick={this.handleTimingWorkflow}
                disabled={releaseState !== 'ONLINE' || !!scheduleReleaseState}
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
            default: () =>
              releaseState === 'ONLINE'
                ? t('project.workflow.down_line')
                : t('project.workflow.up_line'),
            trigger: () => (
              <NButton
                size='small'
                type={releaseState === 'ONLINE' ? 'warning' : 'error'}
                tag='div'
                circle
                class='btn-publish'
              >
                <NPopconfirm onPositiveClick={this.handleReleaseWorkflow}>
                  {{
                    default: () =>
                      releaseState === 'ONLINE'
                        ? t('project.workflow.confirm_to_offline')
                        : t('project.workflow.confirm_to_online'),
                    trigger: () => (
                      <NIcon>
                        {releaseState === 'ONLINE' ? (
                          <DownloadOutlined />
                        ) : (
                          <UploadOutlined />
                        )}
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
                size='small'
                type='info'
                tag='div'
                circle
                onClick={this.handleCopyWorkflow}
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
                size='small'
                type='info'
                tag='div'
                circle
                disabled={releaseState === 'OFFLINE'}
                onClick={this.handleGotoTimingManage}
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
                size='small'
                type='error'
                tag='div'
                circle
                disabled={releaseState === 'ONLINE'}
                class='btn-delete'
              >
                <NPopconfirm
                  disabled={releaseState === 'ONLINE'}
                  onPositiveClick={this.handleDeleteWorkflow}
                >
                  {{
                    default: () => t('project.workflow.delete_confirm'),
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
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('project.workflow.tree_view'),
            trigger: () => (
              <NButton
                size='small'
                type='info'
                tag='div'
                circle
                onClick={this.handleGotoWorkflowTree}
              >
                <NIcon>
                  <ApartmentOutlined />
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
                size='small'
                type='info'
                tag='div'
                circle
                onClick={this.handleExportWorkflow}
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
                size='small'
                type='info'
                tag='div'
                circle
                onClick={this.handleVersionWorkflow}
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
