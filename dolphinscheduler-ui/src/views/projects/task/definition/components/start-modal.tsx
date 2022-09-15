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
  onMounted,
  watch,
  getCurrentInstance
} from 'vue'
import { useI18n } from 'vue-i18n'
import Modal from '@/components/modal'
import { useStart } from './use-start'
import {
  NForm,
  NFormItem,
  NButton,
  NIcon,
  NInput,
  NSpace,
  NSelect,
  NSwitch
} from 'naive-ui'
import { DeleteOutlined, PlusCircleOutlined } from '@vicons/antd'

const props = {
  row: {
    type: Object,
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
  name: 'task-definition-start',
  props,
  emits: ['update:show', 'update:row', 'updateList'],
  setup(props, ctx) {
    const { t } = useI18n()
    const {
      variables,
      handleStartDefinition,
      getWorkerGroups,
      getAlertGroups,
      getEnvironmentList,
      getStartParamsList
    } = useStart(ctx)

    const generalWarningTypeListOptions = () => [
      {
        value: 'NONE',
        label: t('project.task.none_send')
      },
      {
        value: 'SUCCESS',
        label: t('project.task.success_send')
      },
      {
        value: 'FAILURE',
        label: t('project.task.failure_send')
      },
      {
        value: 'ALL',
        label: t('project.task.all_send')
      }
    ]

    const hideModal = () => {
      ctx.emit('update:show')
    }

    const handleStart = () => {
      handleStartDefinition(props.row.taskCode)
    }

    const updateWorkerGroup = () => {
      variables.startForm.environmentCode = null
    }

    const addStartParams = () => {
      variables.startState.startParamsList.push({
        prop: '',
        value: ''
      })
    }

    const updateParamsList = (index: number, param: Array<string>) => {
      variables.startState.startParamsList[index].prop = param[0]
      variables.startState.startParamsList[index].value = param[1]
    }

    const removeStartParams = (index: number) => {
      variables.startState.startParamsList.splice(index, 1)
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    onMounted(() => {
      getWorkerGroups()
      getAlertGroups()
      getEnvironmentList()
    })

    watch(
      () => props.show,
      () => {
        if (props.show) {
          getStartParamsList(props.row.processDefinitionCode)
        }
      }
    )

    return {
      t,
      hideModal,
      handleStart,
      updateWorkerGroup,
      removeStartParams,
      addStartParams,
      updateParamsList,
      generalWarningTypeListOptions,
      ...toRefs(variables),
      ...toRefs(variables.startState),
      ...toRefs(props),
      trim
    }
  },

  render() {
    const { t } = this

    return (
      <Modal
        show={this.show}
        title={t('project.task.set_parameters_before_starting')}
        onCancel={this.hideModal}
        onConfirm={this.handleStart}
        confirmLoading={this.saving}
      >
        <NForm ref='startFormRef' model={this.startForm}>
          <NFormItem label={t('project.task.task_name')} path='task_name'>
            <div title={this.row.taskName}>{this.row.taskName}</div>
          </NFormItem>
          <NFormItem
            label={t('project.task.notification_strategy')}
            path='warningType'
          >
            <NSelect
              options={this.generalWarningTypeListOptions()}
              v-model:value={this.startForm.warningType}
            />
          </NFormItem>
          <NFormItem label={t('project.task.worker_group')} path='workerGroup'>
            <NSelect
              options={this.workerGroups}
              onUpdateValue={this.updateWorkerGroup}
              v-model:value={this.startForm.workerGroup}
            />
          </NFormItem>
          <NFormItem
            label={t('project.task.environment_name')}
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
            label={t('project.task.alarm_group')}
            path='warningGroupId'
          >
            <NSelect
              options={this.alertGroups}
              placeholder={t('project.task.please_choose')}
              v-model:value={this.startForm.warningGroupId}
              clearable
            />
          </NFormItem>
          <NFormItem
            label={t('project.task.startup_parameter')}
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
                  <NSpace key={Date.now() + index}>
                    <NInput
                      allowInput={this.trim}
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
          <NFormItem label={t('project.task.whether_dry_run')} path='dryRun'>
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
