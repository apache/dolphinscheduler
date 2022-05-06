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
  watch
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
  NSwitch,
  NCheckbox,
  NDatePicker
} from 'naive-ui'
import {
  ArrowDownOutlined,
  ArrowUpOutlined,
  DeleteOutlined,
  PlusCircleOutlined
} from '@vicons/antd'
import { IDefinitionData } from '../types'
import styles from '../index.module.scss'

const props = {
  row: {
    type: Object as PropType<IDefinitionData>,
    default: {}
  },
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  taskCode: {
    type: String
  }
}

export default defineComponent({
  name: 'workflowDefinitionStart',
  props,
  emits: ['update:show', 'update:row', 'updateList'],
  setup(props, ctx) {
    const parallelismRef = ref(false)
    const { t } = useI18n()
    const { startState } = useForm()
    const {
      variables,
      handleStartDefinition,
      getWorkerGroups,
      getAlertGroups,
      getEnvironmentList,
      getStartParamsList
    } = useModal(startState, ctx)

    const hideModal = () => {
      ctx.emit('update:show')
    }

    const handleStart = () => {
      handleStartDefinition(props.row.code)
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
      startState.startForm.environmentCode = null
    }

    const addStartParams = () => {
      variables.startParamsList.push({
        prop: '',
        value: ''
      })
    }

    const updateParamsList = (index: number, param: Array<string>) => {
      variables.startParamsList[index].prop = param[0]
      variables.startParamsList[index].value = param[1]
    }

    const removeStartParams = (index: number) => {
      variables.startParamsList.splice(index, 1)
    }

    onMounted(() => {
      getWorkerGroups()
      getAlertGroups()
      getEnvironmentList()
    })

    watch(
      () => props.show,
      () => {
        if (props.show) {
          getStartParamsList(props.row.code)
          if (props.taskCode)
            startState.startForm.startNodeList = props.taskCode
        }
      }
    )

    return {
      t,
      parallelismRef,
      hideModal,
      handleStart,
      generalWarningTypeListOptions,
      generalPriorityList,
      renderLabel,
      updateWorkerGroup,
      removeStartParams,
      addStartParams,
      updateParamsList,
      ...toRefs(variables),
      ...toRefs(startState),
      ...toRefs(props)
    }
  },

  render() {
    const { t } = this

    return (
      <Modal
        show={this.show}
        title={t('project.workflow.set_parameters_before_starting')}
        onCancel={this.hideModal}
        onConfirm={this.handleStart}
        confirmLoading={this.saving}
      >
        <NForm ref='startFormRef'>
          <NFormItem
            label={t('project.workflow.workflow_name')}
            path='workflow_name'
          >
            <div class={styles.formItem} title={this.row.name}>
              {this.row.name}
            </div>
          </NFormItem>
          <NFormItem
            label={t('project.workflow.failure_strategy')}
            path='failureStrategy'
          >
            <NRadioGroup v-model:value={this.startForm.failureStrategy}>
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
              v-model:value={this.startForm.warningType}
            />
          </NFormItem>
          <NFormItem
            label={t('project.workflow.workflow_priority')}
            path='processInstancePriority'
          >
            <NSelect
              options={this.generalPriorityList()}
              renderLabel={this.renderLabel}
              v-model:value={this.startForm.processInstancePriority}
            />
          </NFormItem>
          <NFormItem
            label={t('project.workflow.worker_group')}
            path='workerGroup'
          >
            <NSelect
              options={this.workerGroups}
              onUpdateValue={this.updateWorkerGroup}
              v-model:value={this.startForm.workerGroup}
            />
          </NFormItem>
          <NFormItem
            label={t('project.workflow.environment_name')}
            path='environmentCode'
          >
            <NSelect
              options={this.environmentList.filter((item: any) =>
                item.workerGroups?.includes(this.startForm.workerGroup)
              )}
              v-model:value={this.startForm.environmentCode}
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
              v-model:value={this.startForm.warningGroupId}
              clearable
            />
          </NFormItem>
          <NFormItem
            label={t('project.workflow.complement_data')}
            path='complement_data'
          >
            <NCheckbox
              checkedValue={'COMPLEMENT_DATA'}
              uncheckedValue={'START_PROCESS'}
              v-model:checked={this.startForm.execType}
            >
              {t('project.workflow.whether_complement_data')}
            </NCheckbox>
          </NFormItem>
          {this.startForm.execType &&
            this.startForm.execType !== 'START_PROCESS' && (
              <NSpace>
                <NFormItem
                  label={t('project.workflow.mode_of_dependent')}
                  path='dependentMode'
                >
                  <NRadioGroup v-model:value={this.startForm.dependentMode}>
                    <NSpace>
                      <NRadio value={'OFF_MODE'}>
                        {t('project.workflow.close')}
                      </NRadio>
                      <NRadio value={'ALL_DEPENDENT'}>
                        {t('project.workflow.open')}
                      </NRadio>
                    </NSpace>
                  </NRadioGroup>
                </NFormItem>
                <NFormItem
                  label={t('project.workflow.mode_of_execution')}
                  path='runMode'
                >
                  <NRadioGroup v-model:value={this.startForm.runMode}>
                    <NSpace>
                      <NRadio value={'RUN_MODE_SERIAL'}>
                        {t('project.workflow.serial_execution')}
                      </NRadio>
                      <NRadio value={'RUN_MODE_PARALLEL'}>
                        {t('project.workflow.parallel_execution')}
                      </NRadio>
                    </NSpace>
                  </NRadioGroup>
                </NFormItem>
                {this.startForm.runMode === 'RUN_MODE_PARALLEL' && (
                  <NFormItem
                    label={t('project.workflow.parallelism')}
                    path='expectedParallelismNumber'
                  >
                    <NCheckbox v-model:checked={this.parallelismRef}>
                      {t('project.workflow.custom_parallelism')}
                    </NCheckbox>
                    <NInput
                      disabled={!this.parallelismRef}
                      placeholder={t(
                        'project.workflow.please_enter_parallelism'
                      )}
                      v-model:value={this.startForm.expectedParallelismNumber}
                    />
                  </NFormItem>
                )}
                <NFormItem
                  label={t('project.workflow.schedule_date')}
                  path='startEndTime'
                >
                  <NDatePicker
                    type='datetimerange'
                    clearable
                    v-model:value={this.startForm.startEndTime}
                    placement='top'
                  />
                </NFormItem>
              </NSpace>
            )}
          <NFormItem
            label={t('project.workflow.startup_parameter')}
            path='startup_parameter'
          >
            {this.startParamsList.length === 0 ? (
              <NButton text type='primary' onClick={this.addStartParams}>
                <NIcon>
                  <PlusCircleOutlined />
                </NIcon>
              </NButton>
            ) : (
              <NSpace vertical>
                {this.startParamsList.map((item, index) => (
                  <NSpace class={styles.startup} key={Date.now() + index}>
                    <NInput
                      pair
                      separator=':'
                      placeholder={['prop', 'value']}
                      defaultValue={[item.prop, item.value]}
                      onUpdateValue={(param) =>
                        this.updateParamsList(index, param)
                      }
                    />
                    <NButton
                      text
                      type='error'
                      onClick={() => this.removeStartParams(index)}
                      class='btn-delete-custom-parameter'
                    >
                      <NIcon>
                        <DeleteOutlined />
                      </NIcon>
                    </NButton>
                    <NButton
                      text
                      type='primary'
                      onClick={this.addStartParams}
                      class='btn-create-custom-parameter'
                    >
                      <NIcon>
                        <PlusCircleOutlined />
                      </NIcon>
                    </NButton>
                  </NSpace>
                ))}
              </NSpace>
            )}
          </NFormItem>
          <NFormItem
            label={t('project.workflow.whether_dry_run')}
            path='dryRun'
          >
            <NSwitch
              checkedValue={1}
              uncheckedValue={0}
              v-model:value={this.startForm.dryRun}
            />
          </NFormItem>
        </NForm>
      </Modal>
    )
  }
})
