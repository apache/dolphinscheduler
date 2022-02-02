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

import { defineComponent, PropType, toRefs, h, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import Modal from '@/components/modal'
import { useForm } from './use-form'
import { useModal } from './use-modal'
import {
  NForm,
  NFormItem,
  NButton,
  NIcon,
  NInput,
  NSpace,
  NRadio,
  NRadioGroup,
  NSelect,
  NDatePicker,
  NInputGroup,
  NList,
  NListItem,
  NThing
} from 'naive-ui'
import { ArrowDownOutlined, ArrowUpOutlined } from '@vicons/antd'
import { timezoneList } from '@/utils/timezone'

const props = {
  row: {
    type: Object,
    default: {}
  },
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  type: {
    type: String as PropType<String>,
    default: 'create'
  }
}

export default defineComponent({
  name: 'workflowDefinitionStart',
  props,
  emits: ['update:show', 'update:row', 'updateList'],
  setup(props, ctx) {
    const parallelismRef = ref(false)
    const { t } = useI18n()
    const { timingState } = useForm()
    const {
      variables,
      handleCreateTiming,
      handleUpdateTiming,
      getWorkerGroups,
      getAlertGroups,
      getEnvironmentList,
      getPreviewSchedule
    } = useModal(timingState, ctx)

    const hideModal = () => {
      ctx.emit('update:show')
    }

    const handleTiming = () => {
      if (props.type === 'create') {
        handleCreateTiming(props.row.code as number)
      } else {
        handleUpdateTiming(props.row.id)
      }
    }

    const generalWarningTypeListOptions = () => [
      {
        value: 'NONE',
        label: t('project.workflow.none_send')
      },
      {
        value: 'SUCCESS',
        label: t('project.workflow.success_send')
      },
      {
        value: 'FAILURE',
        label: t('project.workflow.failure_send')
      },
      {
        value: 'ALL',
        label: t('project.workflow.all_send')
      }
    ]

    const generalPriorityList = () => [
      {
        value: 'HIGHEST',
        label: 'HIGHEST',
        color: '#ff0000',
        icon: ArrowUpOutlined
      },
      {
        value: 'HIGH',
        label: 'HIGH',
        color: '#ff0000',
        icon: ArrowUpOutlined
      },
      {
        value: 'MEDIUM',
        label: 'MEDIUM',
        color: '#EA7D24',
        icon: ArrowUpOutlined
      },
      {
        value: 'LOW',
        label: 'LOW',
        color: '#2A8734',
        icon: ArrowDownOutlined
      },
      {
        value: 'LOWEST',
        label: 'LOWEST',
        color: '#2A8734',
        icon: ArrowDownOutlined
      }
    ]

    const timezoneOptions = () =>
      timezoneList.map((item) => ({ label: item, value: item }))

    const renderLabel = (option: any) => {
      return [
        h(
          NIcon,
          {
            style: {
              verticalAlign: 'middle',
              marginRight: '4px',
              marginBottom: '3px'
            },
            color: option.color
          },
          {
            default: () => h(option.icon)
          }
        ),
        option.label
      ]
    }

    const updateWorkerGroup = () => {
      timingState.timingForm.environmentCode = null
    }

    const handlePreview = () => {
      getPreviewSchedule()
    }

    onMounted(() => {
      getWorkerGroups()
      getAlertGroups()
      getEnvironmentList()
    })

    return {
      t,
      parallelismRef,
      hideModal,
      handleTiming,
      generalWarningTypeListOptions,
      generalPriorityList,
      timezoneOptions,
      renderLabel,
      updateWorkerGroup,
      handlePreview,
      ...toRefs(variables),
      ...toRefs(timingState),
      ...toRefs(props)
    }
  },

  render() {
    const { t } = this
    if (Number(this.timingForm.warningGroupId) === 0) {
      this.timingForm.warningGroupId = ''
    }

    return (
      <Modal
        show={this.show}
        title={t('project.workflow.set_parameters_before_timing')}
        onCancel={this.hideModal}
        onConfirm={this.handleTiming}
      >
        <NForm ref='timingFormRef' label-placement='left' label-width='160'>
          <NFormItem
            label={t('project.workflow.start_and_stop_time')}
            path='startEndTime'
          >
            <NDatePicker
              type='datetimerange'
              clearable
              v-model:value={this.timingForm.startEndTime}
            />
          </NFormItem>
          <NFormItem label={t('project.workflow.timing')} path='crontab'>
            <NInputGroup>
              <NInput
                style={{ width: '80%' }}
                v-model:value={this.timingForm.crontab}
              ></NInput>
              <NButton type='primary' ghost onClick={this.handlePreview}>
                {t('project.workflow.execute_time')}
              </NButton>
            </NInputGroup>
          </NFormItem>
          <NFormItem
            label={t('project.workflow.timezone')}
            path='timezoneId'
            showFeedback={false}
          >
            <NSelect
              v-model:value={this.timingForm.timezoneId}
              options={this.timezoneOptions()}
            />
          </NFormItem>
          <NFormItem label=' ' showFeedback={false}>
            <NList>
              <NListItem>
                <NThing
                  description={t('project.workflow.next_five_execution_times')}
                >
                  {this.schedulePreviewList.map((item: string) => (
                    <NSpace>
                      {item}
                      <br />
                    </NSpace>
                  ))}
                </NThing>
              </NListItem>
            </NList>
          </NFormItem>
          <NFormItem
            label={t('project.workflow.failure_strategy')}
            path='failureStrategy'
          >
            <NRadioGroup v-model:value={this.timingForm.failureStrategy}>
              <NSpace>
                <NRadio value='CONTINUE'>
                  {t('project.workflow.continue')}
                </NRadio>
                <NRadio value='END'>{t('project.workflow.end')}</NRadio>
              </NSpace>
            </NRadioGroup>
          </NFormItem>
          <NFormItem
            label={t('project.workflow.notification_strategy')}
            path='warningType'
          >
            <NSelect
              options={this.generalWarningTypeListOptions()}
              v-model:value={this.timingForm.warningType}
            />
          </NFormItem>
          <NFormItem
            label={t('project.workflow.workflow_priority')}
            path='processInstancePriority'
          >
            <NSelect
              options={this.generalPriorityList()}
              renderLabel={this.renderLabel}
              v-model:value={this.timingForm.processInstancePriority}
            />
          </NFormItem>
          <NFormItem
            label={t('project.workflow.worker_group')}
            path='workerGroup'
          >
            <NSelect
              options={this.workerGroups}
              onUpdateValue={this.updateWorkerGroup}
              v-model:value={this.timingForm.workerGroup}
            />
          </NFormItem>
          <NFormItem
            label={t('project.workflow.environment_name')}
            path='environmentCode'
          >
            <NSelect
              options={this.environmentList.filter((item: any) =>
                item.workerGroups?.includes(this.timingForm.workerGroup)
              )}
              v-model:value={this.timingForm.environmentCode}
            />
          </NFormItem>
          <NFormItem
            label={t('project.workflow.alarm_group')}
            path='warningGroupId'
          >
            <NSelect
              options={this.alertGroups}
              placeholder={t('project.workflow.please_choose')}
              v-model:value={this.timingForm.warningGroupId}
            />
          </NFormItem>
        </NForm>
      </Modal>
    )
  }
})
