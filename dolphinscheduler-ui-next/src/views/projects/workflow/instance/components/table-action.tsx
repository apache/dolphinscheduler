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
  FormOutlined,
  InfoCircleFilled,
  SyncOutlined,
  CloseOutlined,
  CloseCircleOutlined,
  PauseCircleOutlined,
  ControlOutlined,
  PlayCircleOutlined
} from '@vicons/antd'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import type { Router } from 'vue-router'
import { IWorkflowInstance } from '@/service/modules/process-instances/types'

const props = {
  row: {
    type: Object as PropType<IWorkflowInstance>,
    required: true
  }
}

export default defineComponent({
  name: 'TableAction',
  props,
  emits: [
    'updateList',
    'reRun',
    'reStore',
    'stop',
    'suspend',
    'deleteInstance'
  ],
  setup(props, ctx) {
    const router: Router = useRouter()

    const handleEdit = () => {
      router.push({
        name: 'workflow-instance-detail',
        params: { id: props.row!.id },
        query: { code: props.row!.processDefinitionCode }
      })
    }

    const handleGantt = () => {
      router.push({
        name: 'workflow-instance-gantt',
        params: { id: props.row!.id },
        query: { code: props.row!.processDefinitionCode }
      })
    }

    const handleReRun = () => {
      ctx.emit('reRun')
    }

    const handleReStore = () => {
      ctx.emit('reStore')
    }

    const handleStop = () => {
      ctx.emit('stop')
    }

    const handleSuspend = () => {
      ctx.emit('suspend')
    }

    const handleDeleteInstance = () => {
      ctx.emit('deleteInstance')
    }

    return {
      handleEdit,
      handleReRun,
      handleReStore,
      handleStop,
      handleSuspend,
      handleDeleteInstance,
      handleGantt,
      ...toRefs(props)
    }
  },
  render() {
    const { t } = useI18n()
    const state = this.row?.state

    return (
      <NSpace>
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('project.workflow.edit'),
            trigger: () => (
              <NButton
                tag='div'
                size='small'
                type='info'
                circle
                class='btn-edit'
                disabled={
                  (state !== 'SUCCESS' &&
                    state !== 'PAUSE' &&
                    state !== 'FAILURE' &&
                    state !== 'STOP') ||
                  this.row?.disabled
                }
                onClick={this.handleEdit}
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
            default: () => t('project.workflow.rerun'),
            trigger: () => {
              return (
                <NButton
                  tag='div'
                  size='small'
                  type='info'
                  circle
                  onClick={this.handleReRun}
                  class='btn-rerun'
                  disabled={
                    (state !== 'SUCCESS' &&
                      state !== 'PAUSE' &&
                      state !== 'FAILURE' &&
                      state !== 'STOP') ||
                    this.row?.disabled
                  }
                >
                  {this.row?.buttonType === 'run' ? (
                    <span>{this.row?.count}</span>
                  ) : (
                    <NIcon>
                      <SyncOutlined />
                    </NIcon>
                  )}
                </NButton>
              )
            }
          }}
        </NTooltip>
        <NTooltip trigger={'hover'}>
          {{
            default: () => t('project.workflow.failed_to_retry'),
            trigger: () => (
              <NButton
                tag='div'
                size='small'
                type='primary'
                circle
                onClick={this.handleReStore}
                disabled={state !== 'FAILURE' || this.row?.disabled}
              >
                {this.row?.buttonType === 'store' ? (
                  <span>{this.row?.count}</span>
                ) : (
                  <NIcon>
                    <CloseCircleOutlined />
                  </NIcon>
                )}
              </NButton>
            )
          }}
        </NTooltip>
        <NTooltip trigger={'hover'}>
          {{
            default: () =>
              state === 'STOP'
                ? t('project.workflow.recovery_suspend')
                : t('project.workflow.stop'),
            trigger: () => (
              <NButton
                tag='div'
                size='small'
                type='error'
                circle
                onClick={this.handleStop}
                disabled={
                  (state !== 'RUNNING_EXECUTION' && state !== 'STOP') ||
                  this.row?.disabled
                }
              >
                <NIcon>
                  {state === 'STOP' ? (
                    <PlayCircleOutlined />
                  ) : (
                    <CloseOutlined />
                  )}
                </NIcon>
              </NButton>
            )
          }}
        </NTooltip>
        <NTooltip trigger={'hover'}>
          {{
            default: () =>
              state === 'PAUSE'
                ? t('project.workflow.recovery_suspend')
                : t('project.workflow.pause'),
            trigger: () => (
              <NButton
                tag='div'
                size='small'
                type='warning'
                circle
                disabled={
                  (state !== 'RUNNING_EXECUTION' && state !== 'PAUSE') ||
                  this.row?.disabled
                }
                onClick={this.handleSuspend}
              >
                <NIcon>
                  {state === 'PAUSE' ? (
                    <PlayCircleOutlined />
                  ) : (
                    <PauseCircleOutlined />
                  )}
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
                tag='div'
                size='small'
                type='error'
                circle
                disabled={
                  (state !== 'SUCCESS' &&
                    state !== 'FAILURE' &&
                    state !== 'STOP' &&
                    state !== 'PAUSE') ||
                  this.row?.disabled
                }
              >
                <NPopconfirm onPositiveClick={this.handleDeleteInstance}>
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
            default: () => t('project.workflow.gantt'),
            trigger: () => (
              <NButton
                tag='div'
                size='small'
                type='info'
                circle
                disabled={this.row?.disabled}
                onClick={this.handleGantt}
              >
                <NIcon>
                  <ControlOutlined />
                </NIcon>
              </NButton>
            )
          }}
        </NTooltip>
      </NSpace>
    )
  }
})
