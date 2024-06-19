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
  getCurrentInstance,
  computed
} from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import Modal from '@/components/modal'
import { useForm } from './use-form'
import { useModal } from './use-modal'
import {
  NForm,
  NFormItem,
  NIcon,
  NInput,
  NSpace,
  NRadio,
  NRadioGroup,
  NSelect,
  NSwitch,
  NCheckbox,
  NDatePicker,
  NRadioButton,
  NInputNumber,
  NDynamicInput,
  NGrid,
  NGridItem
} from 'naive-ui'
import {
  ArrowDownOutlined,
  ArrowUpOutlined
} from '@vicons/antd'
import { IDefinitionData } from '../types'
import styles from '../index.module.scss'
import { queryProjectPreferenceByProjectCode } from '@/service/modules/projects-preference'

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
    const { t } = useI18n()
    const route = useRoute()
    const { startState } = useForm()
    const {
      variables,
      handleStartDefinition,
      getWorkerGroups,
      getTenantList,
      getAlertGroups,
      getEnvironmentList,
      getStartParamsList
    } = useModal(startState, ctx)

    const hideModal = () => {
      ctx.emit('update:show')
    }

    const handleStart = () => {
      handleStartDefinition(props.row.code, props.row.version)
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

    const showTaskDependType = computed(
      () => route.name === 'workflow-definition-detail'
    )

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

    const projectPreferences = ref({} as any)

    const initProjectPreferences = async (projectCode: number) => {
      if (projectCode) {
        await queryProjectPreferenceByProjectCode(projectCode).then(
          (result: any) => {
            if (result?.preferences && result.state === 1) {
              projectPreferences.value = JSON.parse(result.preferences)
            }
          }
        )
      }
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

    const restructureForm = async (form: any) => {
      await initProjectPreferences(props.row.projectCode)
      if (projectPreferences.value?.taskPriority) {
        form.processInstancePriority = projectPreferences.value.taskPriority
      }
      if (projectPreferences.value?.warningType) {
        form.warningType = projectPreferences.value.warningType
      }
      if (projectPreferences.value?.workerGroup) {
        if (
          containValueInOptions(
            variables.workerGroups,
            projectPreferences.value.workerGroup
          )
        ) {
          form.workerGroup = projectPreferences.value.workerGroup
        }
      }
      if (projectPreferences.value?.tenant) {
        if (
          containValueInOptions(
            variables.tenantList,
            projectPreferences.value.tenant
          )
        ) {
          form.tenantCode = projectPreferences.value.tenant
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
          form.environmentCode = projectPreferences.value.environmentCode
        }
      }
      if (projectPreferences.value?.alertGroups && variables?.alertGroups) {
        if (
          containValueInOptions(
            variables.alertGroups,
            projectPreferences.value.alertGroups
          )
        ) {
          form.warningGroupId = projectPreferences.value.alertGroups
        }
      }
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

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    onMounted(() => {
      getWorkerGroups()
      getTenantList()
      getAlertGroups()
      getEnvironmentList()
    })

    watch(
      () => props.show,
      () => {
        if (props.show) {
          getStartParamsList(props.row.code)
          restructureForm(startState.startForm)
          if (props.taskCode)
            startState.startForm.startNodeList = props.taskCode
        }
      }
    )

    return {
      t,
      showTaskDependType,
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
      ...toRefs(props),
      trim
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
        <NForm ref='startFormRef' model={this.startForm} rules={this.rules}>
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
          {this.showTaskDependType && (
            <NFormItem
              label={t('project.workflow.node_execution')}
              path='taskDependType'
            >
              <NRadioGroup v-model:value={this.startForm.taskDependType}>
                <NSpace>
                  <NRadio value='TASK_POST'>
                    {t('project.workflow.backward_execution')}
                  </NRadio>
                  <NRadio value='TASK_PRE'>
                    {t('project.workflow.forward_execution')}
                  </NRadio>
                  <NRadio value='TASK_ONLY'>
                    {t('project.workflow.current_node_execution')}
                  </NRadio>
                </NSpace>
              </NRadioGroup>
            </NFormItem>
          )}
          <NFormItem
            label={t('project.workflow.notification_strategy')}
            path='warningType'
          >
            <NSelect
              options={this.generalWarningTypeListOptions()}
              v-model:value={this.startForm.warningType}
            />
          </NFormItem>
          {this.startForm.warningType !== 'NONE' && (
            <NFormItem
              label={t('project.workflow.alarm_group')}
              path='warningGroupId'
              required
            >
              <NSelect
                options={this.alertGroups}
                placeholder={t('project.workflow.please_choose')}
                v-model:value={this.startForm.warningGroupId}
                clearable
                filterable
              />
            </NFormItem>
          )}
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
              filterable
            />
          </NFormItem>
          <NFormItem
            label={t('project.workflow.tenant_code')}
            path='tenantCode'
          >
            <NSelect
              options={this.tenantList}
              v-model:value={this.startForm.tenantCode}
              filterable
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
              filterable
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
              <NSpace vertical class={styles['width-100']}>
                <NFormItem
                  label={t('project.workflow.mode_of_dependent')}
                  path='complementDependentMode'
                >
                  <NRadioGroup
                    v-model:value={this.startForm.complementDependentMode}
                  >
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
                {this.startForm.complementDependentMode === 'ALL_DEPENDENT' && (
                  <NFormItem
                    label={t('project.workflow.all_level_dependent')}
                    path='allLevelDependent'
                  >
                    <NRadioGroup
                      v-model:value={this.startForm.allLevelDependent}
                    >
                      <NSpace>
                        <NRadio value={'false'}>
                          {t('project.workflow.close')}
                        </NRadio>
                        <NRadio value={'true'}>
                          {t('project.workflow.open')}
                        </NRadio>
                      </NSpace>
                    </NRadioGroup>
                  </NFormItem>
                )}
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
                    feedback={t(
                      'project.workflow.warning_too_large_parallelism_number'
                    )}
                    validationStatus={'warning'}
                    showFeedback={
                      parseInt(this.startForm.expectedParallelismNumber) > 10
                    }
                  >
                    <NInputNumber
                      placeholder={t(
                        'project.workflow.please_enter_parallelism'
                      )}
                      v-model:value={this.startForm.expectedParallelismNumber}
                      min='1'
                    />
                  </NFormItem>
                )}
                <NFormItem
                  label={t('project.workflow.order_of_execution')}
                  path='executionOrder'
                >
                  <NRadioGroup v-model:value={this.startForm.executionOrder}>
                    <NSpace>
                      <NRadio value={'DESC_ORDER'}>
                        {t('project.workflow.descending_order')}
                      </NRadio>
                      <NRadio value={'ASC_ORDER'}>
                        {t('project.workflow.ascending_order')}
                      </NRadio>
                    </NSpace>
                  </NRadioGroup>
                </NFormItem>
                <NFormItem
                  label={t('project.workflow.schedule_date')}
                  path={
                    this.startForm.dataDateType === 1
                      ? 'startEndTime'
                      : 'scheduleTime'
                  }
                >
                  <NSpace vertical class={styles['width-100']}>
                    <NRadioGroup
                      name='data-date'
                      v-model:value={this.startForm.dataDateType}
                    >
                      {[
                        { label: t('project.workflow.select_date'), value: 1 },
                        { label: t('project.workflow.enter_date'), value: 2 }
                      ].map((item) => (
                        <NRadioButton {...item} key={item.value} />
                      ))}
                    </NRadioGroup>

                    {this.startForm.dataDateType === 1 ? (
                      <NDatePicker
                        type='datetimerange'
                        clearable
                        v-model:value={this.startForm.startEndTime}
                        placement='top'
                      />
                    ) : (
                      <NInput
                        allowInput={this.trim}
                        clearable
                        type='textarea'
                        v-model:value={this.startForm.scheduleTime}
                        placeholder={t('project.workflow.schedule_date_tips')}
                      />
                    )}
                  </NSpace>
                </NFormItem>
              </NSpace>
            )}
          <NFormItem
            label={t('project.workflow.startup_parameter')}
            path='startup_parameter'
          >
            <NDynamicInput
                v-model:value={this.startParamsList}
                onCreate={() => {
                  return {
                    key: '',
                    direct: 'IN',
                    type: 'VARCHAR',
                    value: ''
                  }
                }}
                class='input-startup-params'
            >
              {{
                default: (param: {
                  value: { prop: string; direct: string; type: string; value: string }
                }) => (
                    <NGrid xGap={12} cols={24}>
                      <NGridItem span={6}>
                        <NInput
                            v-model:value={param.value.prop}
                            placeholder={t('project.dag.key')}
                        />
                      </NGridItem>
                      <NGridItem span={5}>
                        <NSelect
                            options={[
                              { value: 'IN', label: 'IN' },
                              { value: 'OUT', label: 'OUT' }
                            ]}
                            v-model:value={param.value.direct}
                            defaultValue={'IN'}
                        />
                      </NGridItem>
                      <NGridItem span={7}>
                        <NSelect
                            options={[
                              { value: 'VARCHAR', label: 'VARCHAR' },
                              { value: 'INTEGER', label: 'INTEGER' },
                              { value: 'LONG', label: 'LONG' },
                              { value: 'FLOAT', label: 'FLOAT' },
                              { value: 'DOUBLE', label: 'DOUBLE' },
                              { value: 'DATE', label: 'DATE' },
                              { value: 'TIME', label: 'TIME' },
                              { value: 'BOOLEAN', label: 'BOOLEAN' },
                              { value: 'LIST', label: 'LIST' },
                              { value: 'FILE', label: 'FILE' }
                            ]}
                            v-model:value={param.value.type}
                            defaultValue={'VARCHAR'}
                        />
                      </NGridItem>
                      <NGridItem span={6}>
                        <NInput
                            v-model:value={param.value.value}
                            placeholder={t('project.dag.value')}
                        />
                      </NGridItem>
                    </NGrid>
                )
              }}
            </NDynamicInput>
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
          <NFormItem label={t('project.workflow.whether_test')} path='testFlag'>
            <NSwitch
              checkedValue={1}
              uncheckedValue={0}
              v-model:value={this.startForm.testFlag}
            />
          </NFormItem>
        </NForm>
      </Modal>
    )
  }
})
