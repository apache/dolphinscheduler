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

import {
  defineComponent,
  PropType,
  toRefs,
  h,
  onMounted,
  ref,
  watch,
  computed
} from 'vue'
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
  NThing,
  NPopover
} from 'naive-ui'
import { ArrowDownOutlined, ArrowUpOutlined } from '@vicons/antd'
import { timezoneList } from '@/common/timezone'
import Crontab from '@/components/crontab'

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
    const crontabRef = ref()
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

    const environmentOptions = computed(() =>
      variables.environmentList.filter((item: any) =>
        item.workerGroups?.includes(timingState.timingForm.workerGroup)
      )
    )

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

    const priorityOptions = [
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

    const initEnvironment = () => {
      timingState.timingForm.environmentCode = null
      variables.environmentList.forEach((item) => {
        if (props.row.environmentCode === item.value) {
          timingState.timingForm.environmentCode = item.value
        }
      })
    }

    const initWarningGroup = () => {
      timingState.timingForm.warningGroupId = null
      variables.alertGroups.forEach((item) => {
        if (props.row.warningGroupId === item.value) {
          timingState.timingForm.warningGroupId = item.value
        }
      })
    }

    onMounted(() => {
      getWorkerGroups()
      getAlertGroups()
      getEnvironmentList()
    })

    watch(
      () => props.row,
      () => {
        if (!props.row.crontab) return

        timingState.timingForm.startEndTime = [
          new Date(props.row.startTime),
          new Date(props.row.endTime)
        ]
        timingState.timingForm.crontab = props.row.crontab
        timingState.timingForm.timezoneId = props.row.timezoneId
        timingState.timingForm.failureStrategy = props.row.failureStrategy
        timingState.timingForm.warningType = props.row.warningType
        timingState.timingForm.processInstancePriority =
          props.row.processInstancePriority
        timingState.timingForm.workerGroup = props.row.workerGroup
        initWarningGroup()
        initEnvironment()
      }
    )

    return {
      t,
      crontabRef,
      parallelismRef,
      priorityOptions,
      environmentOptions,
      hideModal,
      handleTiming,
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

    return (
      <Modal
        show={this.show}
        title={t('project.workflow.set_parameters_before_timing')}
        onCancel={this.hideModal}
        onConfirm={this.handleTiming}
        confirmLoading={this.saving}
      >
        <NForm ref='timingFormRef'>
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
              <NPopover
                trigger='click'
                showArrow={false}
                placement='bottom'
                style={{ width: '500px' }}
              >
                {{
                  trigger: () => (
                    <NInput
                      style={{ width: '80%' }}
                      readonly={true}
                      v-model:value={this.timingForm.crontab}
                    ></NInput>
                  ),
                  default: () => (
                    <Crontab v-model:value={this.timingForm.crontab} />
                  )
                }}
              </NPopover>
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
              filterable
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
              options={[
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
              ]}
              v-model:value={this.timingForm.warningType}
            />
          </NFormItem>
          <NFormItem
            label={t('project.workflow.workflow_priority')}
            path='processInstancePriority'
          >
            <NSelect
              options={this.priorityOptions}
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
              options={this.environmentOptions}
              v-model:value={this.timingForm.environmentCode}
              clearable
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
              clearable
            />
          </NFormItem>
        </NForm>
      </Modal>
    )
  }
})
