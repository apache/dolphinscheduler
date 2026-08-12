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
  computed,
  getCurrentInstance
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
  NInputNumber,
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
import { Router, useRouter } from 'vue-router'
import { ArrowDownOutlined, ArrowUpOutlined } from '@vicons/antd'
import { timezoneList } from '@/common/timezone'
import Crontab from '@/components/crontab'
import { queryProjectPreferenceByProjectCode } from '@/service/modules/projects-preference'

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
  },
  state: {
    type: String as PropType<String>,
    default: 'OFFLINE'
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
    const router: Router = useRouter()

    const { timingState } = useForm()
    const {
      variables,
      handleCreateTiming,
      handleUpdateTiming,
      getWorkerGroups,
      getTenantList,
      getAlertGroups,
      getEnvironmentList,
      getPreviewSchedule
    } = useModal(timingState, ctx)

    const projectCode = Number(router.currentRoute.value.params.projectCode)

    const environmentOptions = computed(() =>
      variables.environmentList.filter((item: any) =>
        item.workerGroups?.includes(timingState.timingForm.workerGroup)
      )
    )

    const projectPreferences = ref({} as any)
    const intervalHours = ref(1)
    const intervalMinutes = ref(0)
    const intervalSeconds = ref(0)
    const intervalRepeat = ref(-1)
    const cronExpression = ref(timingState.timingForm.crontab)

    const initProjectPreferences = (projectCode: number) => {
      queryProjectPreferenceByProjectCode(projectCode).then((result: any) => {
        if (result?.preferences && result.state === 1) {
          projectPreferences.value = JSON.parse(result.preferences)
        }
      })
    }

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
      if (
        timingState.timingForm.triggerType === 'INTERVAL' &&
        intervalHours.value === 0 &&
        intervalMinutes.value === 0 &&
        intervalSeconds.value === 0
      ) {
        window.$message.error(t('project.workflow.interval_must_be_positive'))
        return
      }
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

    const containValueInOptions = (
      options: Array<any>,
      findingValue: string
    ): boolean => {
      for (const { value } of options) {
        if (findingValue === value) {
          return true
        }
      }
      return false
    }

    const restructureTimingForm = (timingForm: any) => {
      if (projectPreferences.value?.taskPriority) {
        timingForm.workflowInstancePriority =
          projectPreferences.value.taskPriority
      }
      if (projectPreferences.value?.warningType) {
        timingForm.warningType = projectPreferences.value.warningType
      }
      if (projectPreferences.value?.workerGroup) {
        if (
          containValueInOptions(
            variables.workerGroups,
            projectPreferences.value.workerGroup
          )
        ) {
          timingForm.workerGroup = projectPreferences.value.workerGroup
        }
      }
      if (projectPreferences.value?.tenant) {
        if (
          containValueInOptions(
            variables.tenantList,
            projectPreferences.value.tenant
          )
        ) {
          timingForm.tenantCode = projectPreferences.value.tenant
        }
      }
      if (
        projectPreferences.value?.environmentCode &&
        variables?.environmentList
      ) {
        if (
          containValueInOptions(
            variables.environmentList,
            projectPreferences.value.environmentCode
          )
        ) {
          timingForm.environmentCode = projectPreferences.value.environmentCode
        }
      }
      if (projectPreferences.value?.alertGroups && variables?.alertGroups) {
        if (
          containValueInOptions(
            variables.alertGroups,
            projectPreferences.value.alertGroups
          )
        ) {
          timingForm.warningGroupId = projectPreferences.value.alertGroups
        }
      }
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    const updateIntervalExpression = () => {
      timingState.timingForm.crontab = JSON.stringify({
        hour: intervalHours.value,
        minute: intervalMinutes.value,
        second: intervalSeconds.value,
        repeat: intervalRepeat.value
      })
    }

    const restoreIntervalExpression = (expression: string): boolean => {
      try {
        const interval = JSON.parse(expression)
        intervalHours.value = interval.hour || 0
        intervalMinutes.value = interval.minute || 0
        intervalSeconds.value = interval.second || 0
        intervalRepeat.value = interval.repeat ?? -1
        return true
      } catch {
        return false
      }
    }

    watch(
      () => timingState.timingForm.triggerType,
      (triggerType, previousTriggerType) => {
        if (previousTriggerType === 'CRON') {
          cronExpression.value = timingState.timingForm.crontab
        }

        if (triggerType === 'CRON') {
          timingState.timingForm.crontab = cronExpression.value
          return
        }

        if (
          previousTriggerType !== 'INTERVAL' &&
          !restoreIntervalExpression(timingState.timingForm.crontab)
        ) {
          updateIntervalExpression()
        }
      }
    )

    watch(
      [intervalHours, intervalMinutes, intervalSeconds, intervalRepeat],
      () => {
        if (timingState.timingForm.triggerType === 'INTERVAL') {
          updateIntervalExpression()
        }
      }
    )
    onMounted(() => {
      getWorkerGroups()
      getTenantList()
      getAlertGroups()
      getEnvironmentList()
      initProjectPreferences(projectCode)
    })

    watch(
      () => props.row,
      () => {
        if (!props.row.crontab) {
          restructureTimingForm(timingState.timingForm)
          return
        }

        timingState.timingForm.startEndTime = [
          new Date(props.row.startTime),
          new Date(props.row.endTime)
        ]
        const triggerType = props.row.triggerType || 'CRON'
        timingState.timingForm.triggerType = triggerType
        timingState.timingForm.crontab = props.row.crontab
        timingState.timingForm.missedFirePolicy =
          props.row.missedFirePolicy || 'FIRE_ALL_MISSED'
        if (triggerType === 'CRON') {
          cronExpression.value = props.row.crontab
        } else if (!restoreIntervalExpression(props.row.crontab)) {
          updateIntervalExpression()
        }
        timingState.timingForm.timezoneId = props.row.timezoneId
        timingState.timingForm.failureStrategy = props.row.failureStrategy
        timingState.timingForm.warningType = props.row.warningType
        timingState.timingForm.workflowInstancePriority =
          props.row.workflowInstancePriority
        timingState.timingForm.workerGroup = props.row.workerGroup
        timingState.timingForm.tenantCode = props.row.tenantCode
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
      intervalHours,
      intervalMinutes,
      intervalSeconds,
      intervalRepeat,
      ...toRefs(variables),
      ...toRefs(timingState),
      ...toRefs(props),
      trim
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
        confirmDisabled={this.$props.state === 'ONLINE'}
      >
        <NForm
          ref='timingFormRef'
          rules={this.rules}
          disabled={this.$props.state === 'ONLINE'}
        >
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
          <NFormItem
            label={t('project.workflow.trigger_type')}
            path='triggerType'
          >
            <NSelect
              options={[
                { label: t('project.workflow.cron_trigger'), value: 'CRON' },
                {
                  label: t('project.workflow.interval_trigger'),
                  value: 'INTERVAL'
                }
              ]}
              v-model:value={this.timingForm.triggerType}
            />
          </NFormItem>
          {this.timingForm.triggerType === 'CRON' ? (
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
                        allowInput={this.trim}
                        style={{ width: '80%' }}
                        readonly={true}
                        v-model:value={this.timingForm.crontab}
                      />
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
          ) : (
            <NFormItem label={t('project.workflow.interval')} path='crontab'>
              <div>
                <NSpace align='center'>
                  <NInputNumber
                    min={0}
                    v-model:value={this.intervalHours}
                    style={{ width: '120px' }}
                  />
                  <span>{t('project.workflow.hours')}</span>
                  <NInputNumber
                    min={0}
                    max={59}
                    v-model:value={this.intervalMinutes}
                    style={{ width: '120px' }}
                  />
                  <span>{t('project.workflow.minutes')}</span>
                  <NInputNumber
                    min={0}
                    max={59}
                    v-model:value={this.intervalSeconds}
                    style={{ width: '120px' }}
                  />
                  <span>{t('project.workflow.seconds')}</span>
                </NSpace>
                <NSpace align='center' style={{ marginTop: '12px' }}>
                  <span>{t('project.workflow.repeat')}</span>
                  <NInputNumber
                    min={-1}
                    v-model:value={this.intervalRepeat}
                    style={{ width: '120px' }}
                  />
                  <span>{t('project.workflow.unlimited_repeat_tip')}</span>
                  <NButton type='primary' ghost onClick={this.handlePreview}>
                    {t('project.workflow.execute_time')}
                  </NButton>
                </NSpace>
              </div>
            </NFormItem>
          )}
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
              {this.schedulePreviewList.length > 0 ? (
                <NListItem>
                  <NThing
                    description={t(
                      'project.workflow.next_five_execution_times'
                    )}
                  >
                    {this.schedulePreviewList.map((item: string) => (
                      <NSpace>
                        {item}
                        <br />
                      </NSpace>
                    ))}
                  </NThing>
                </NListItem>
              ) : null}
            </NList>
          </NFormItem>
          <NFormItem
            label={t('project.workflow.missed_fire_policy')}
            path='missedFirePolicy'
          >
            <NSelect
              options={[
                {
                  value: 'SKIP_MISSED',
                  label: t('project.workflow.skip_missed')
                },
                {
                  value: 'FIRE_ONCE_NOW',
                  label: t('project.workflow.fire_once_now')
                },
                {
                  value: 'FIRE_ALL_MISSED',
                  label: t('project.workflow.fire_all_missed')
                }
              ]}
              v-model:value={this.timingForm.missedFirePolicy}
            />
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
          {this.timingForm.warningType !== 'NONE' && (
            <NFormItem
              label={t('project.workflow.alarm_group')}
              path='warningGroupId'
              required
            >
              <NSelect
                options={this.alertGroups}
                placeholder={t('project.workflow.please_choose')}
                v-model:value={this.timingForm.warningGroupId}
                clearable
                filterable
              />
            </NFormItem>
          )}
          <NFormItem
            label={t('project.workflow.workflow_priority')}
            path='workflowInstancePriority'
          >
            <NSelect
              options={this.priorityOptions}
              renderLabel={this.renderLabel}
              v-model:value={this.timingForm.workflowInstancePriority}
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
              filterable
            />
          </NFormItem>
          <NFormItem
            label={t('project.workflow.tenant_code')}
            path='tenantCode'
          >
            <NSelect
              options={this.tenantList}
              v-model:value={this.timingForm.tenantCode}
              filterable
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
              filterable
            />
          </NFormItem>
        </NForm>
      </Modal>
    )
  }
})
