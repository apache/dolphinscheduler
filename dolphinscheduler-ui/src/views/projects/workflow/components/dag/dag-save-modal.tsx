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
  ref,
  onMounted,
  watch,
  getCurrentInstance
} from 'vue'
import Modal from '@/components/modal'
import { useI18n } from 'vue-i18n'
import {
  NForm,
  NFormItem,
  NInput,
  NSelect,
  NSwitch,
  NInputNumber,
  NDynamicInput,
  NCheckbox,
  NGridItem,
  NGrid
} from 'naive-ui'
import { useRoute } from 'vue-router'
import { verifyName } from '@/service/modules/process-definition'
import './x6-style.scss'
import { positiveIntegerRegex } from '@/utils/regex'
import type { SaveForm, WorkflowDefinition, WorkflowInstance } from './types'

const props = {
  visible: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  // If this prop is passed, it means from definition detail
  definition: {
    type: Object as PropType<WorkflowDefinition>,
    default: undefined
  },
  instance: {
    type: Object as PropType<WorkflowInstance>,
    default: undefined
  }
}

export default defineComponent({
  name: 'dag-save-modal',
  props,
  emits: ['update:show', 'save'],
  setup(props, context) {
    const route = useRoute()
    const { t } = useI18n()

    const projectCode = Number(route.params.projectCode)

    const formValue = ref<SaveForm>({
      name: '',
      description: '',
      executionType: 'PARALLEL',
      timeoutFlag: false,
      timeout: 0,
      globalParams: [],
      release: false,
      sync: false
    })
    const formRef = ref()

    const rule = {
      name: {
        required: true,
        message: t('project.dag.dag_name_empty')
      },
      timeout: {
        validator() {
          if (
            formValue.value.timeoutFlag &&
            !positiveIntegerRegex.test(String(formValue.value.timeout))
          ) {
            return new Error(t('project.dag.positive_integer'))
          }
        }
      },
      globalParams: {
        validator() {
          const props = new Set()

          const keys = formValue.value.globalParams.map((item) => item.key)
          const keysSet = new Set(keys)
          if (keysSet.size !== keys.length) {
            return new Error(t('project.dag.prop_repeat'))
          }

          for (const param of formValue.value.globalParams) {
            const prop = param.value
            const direct = param.direct
            if (direct === 'IN' && !prop) {
              return new Error(t('project.dag.prop_empty'))
            }

            props.add(prop)
          }
        }
      }
    }
    const onSubmit = () => {
      formRef.value.validate(async (valid: any) => {
        if (!valid) {
          const params = {
            name: formValue.value.name,
            code: props.definition?.processDefinition.code
          } as { name: string; code?: number }
          if (
            props.definition?.processDefinition.name !== formValue.value.name
          ) {
            verifyName(params, projectCode).then(() =>
              context.emit('save', formValue.value)
            )
          } else {
            context.emit('save', formValue.value)
          }
        }
      })
    }
    const onCancel = () => {
      context.emit('update:show', false)
    }

    const updateModalData = () => {
      const process = props.definition?.processDefinition
      if (process) {
        formValue.value.name = process.name
        formValue.value.description = process.description
        formValue.value.executionType = process.executionType || 'PARALLEL'
        if (process.timeout && process.timeout > 0) {
          formValue.value.timeoutFlag = true
          formValue.value.timeout = process.timeout
        }
        formValue.value.globalParams = process.globalParamList.map((param) => ({
          key: param.prop,
          value: param.value,
          direct: param.direct,
          type: param.type
        }))
      }
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    onMounted(() => updateModalData())

    watch(
      () => props.definition?.processDefinition,
      () => updateModalData()
    )

    return () => (
      <Modal
        show={props.visible}
        title={t('project.dag.basic_info')}
        onConfirm={onSubmit}
        onCancel={onCancel}
        autoFocus={false}
      >
        <NForm model={formValue.value} rules={rule} ref={formRef}>
          <NFormItem label={t('project.dag.workflow_name')} path='name'>
            <NInput
              allowInput={trim}
              v-model:value={formValue.value.name}
              class='input-name'
            />
          </NFormItem>
          <NFormItem label={t('project.dag.description')} path='description'>
            <NInput
              allowInput={trim}
              type='textarea'
              v-model:value={formValue.value.description}
              class='input-description'
            />
          </NFormItem>
          <NFormItem label={t('project.dag.timeout_alert')} path='timeoutFlag'>
            <NSwitch v-model:value={formValue.value.timeoutFlag} />
          </NFormItem>
          {formValue.value.timeoutFlag && (
            <NFormItem showLabel={false} path='timeout'>
              <NInputNumber
                v-model:value={formValue.value.timeout}
                show-button={false}
                min={0}
                v-slots={{
                  suffix: () => t('project.dag.minute')
                }}
              />
            </NFormItem>
          )}
          {!props.instance && (
            <NFormItem
              label={t('project.dag.process_execute_type')}
              path='executionType'
            >
              <NSelect
                options={[
                  { value: 'PARALLEL', label: t('project.dag.parallel') },
                  { value: 'SERIAL_WAIT', label: t('project.dag.serial_wait') },
                  {
                    value: 'SERIAL_DISCARD',
                    label: t('project.dag.serial_discard')
                  },
                  {
                    value: 'SERIAL_PRIORITY',
                    label: t('project.dag.serial_priority')
                  }
                ]}
                v-model:value={formValue.value.executionType}
              />
            </NFormItem>
          )}
          <NFormItem
            label={t('project.dag.global_variables')}
            path='globalParams'
          >
            <NDynamicInput
              v-model:value={formValue.value.globalParams}
              onCreate={() => {
                return {
                  key: '',
                  direct: 'IN',
                  type: 'VARCHAR',
                  value: ''
                }
              }}
              class='input-global-params'
            >
              {{
                default: (param: {
                  value: { key: string; direct: string; type: string; value: string }
                }) => (
                  <NGrid xGap={12} cols={24}>
                    <NGridItem span={6}>
                      <NInput
                        v-model:value={param.value.key}
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
          {props.definition && !props.instance && (
            <NFormItem path='timeoutFlag' showLabel={false}>
              <NCheckbox v-model:checked={formValue.value.release}>
                {t('project.dag.online_directly')}
              </NCheckbox>
            </NFormItem>
          )}
          {props.instance && (
            <NFormItem path='sync' showLabel={false}>
              <NCheckbox v-model:checked={formValue.value.sync}>
                {t('project.dag.update_directly')}
              </NCheckbox>
            </NFormItem>
          )}
        </NForm>
      </Modal>
    )
  }
})
