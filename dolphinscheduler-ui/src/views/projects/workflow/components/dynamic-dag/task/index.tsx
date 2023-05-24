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
  getCurrentInstance,
  PropType,
  toRefs,
  watch
} from 'vue'
import { NForm, NFormItem, NInput, NSelect } from 'naive-ui'
import { useTaskForm } from './use-task-form'
import { useI18n } from 'vue-i18n'
import Modal from '@/components/modal'
import MonacoEditor from '@/components/monaco-editor'
import type { SelectOption } from 'naive-ui'

const props = {
  showModal: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  task: {
    type: String as PropType<string>
  },
  formData: {
    type: Object as PropType<object>
  }
}

const TaskForm = defineComponent({
  name: 'TaskForm',
  props,
  emits: ['cancelModal', 'confirmModal'],
  setup(props, ctx) {
    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim
    const { variables, handleValidate } = useTaskForm(props.formData)
    const { t } = useI18n()

    const cancelModal = () => {
      ctx.emit('cancelModal')
    }

    const confirmModal = () => {
      handleValidate()
      ctx.emit('confirmModal')
    }

    const onUpdateValue = (v: any, f: any) => {
      f.modelField.indexOf('.') >= 0
        ? ((variables.model as any)[f.modelField.split('.')[0]][
            f.modelField.split('.')[1]
          ] = v)
        : ((variables.model as any)[f.modelField] = v)
    }

    const setDefaultValue = (f: any) => {
      return f.modelField.indexOf('.') >= 0
        ? (variables.model as any)[f.modelField.split('.')[0]][
            f.modelField.split('.')[1]
          ]
        : (variables.model as any)[f.modelField]
    }

    watch(variables.model, () => {
      //console.log(variables.model)
    })

    return {
      ...toRefs(variables),
      cancelModal,
      confirmModal,
      onUpdateValue,
      setDefaultValue,
      t,
      trim
    }
  },
  render() {
    return (
      <Modal
        title={this.task}
        show={this.showModal}
        onCancel={this.cancelModal}
        onConfirm={this.confirmModal}
      >
        <NForm model={this.model} rules={this.rules} ref={'taskForm'}>
          {(this.formStructure as Array<any>).map((f) => {
            return (
              <NFormItem label={this.t(f.label)} path={f.field}>
                {f.type === 'input' && (
                  <NInput
                    allowInput={this.trim}
                    placeholder={f.placeholder ? this.t(f.placeholder) : ''}
                    defaultValue={this.setDefaultValue(f)}
                    onUpdateValue={(v) => this.onUpdateValue(v, f)}
                    clearable={f.clearable}
                  />
                )}
                {f.type === 'select' && (
                  <NSelect
                    placeholder={f.placeholder ? this.t(f.placeholder) : ''}
                    defaultValue={this.setDefaultValue(f)}
                    onUpdateValue={(v) => this.onUpdateValue(v, f)}
                    options={
                      f.optionsLocale
                        ? f.options.map((o: SelectOption) => {
                            return {
                              label: this.t(o.label as string),
                              value: o.value
                            }
                          })
                        : f.options
                    }
                  />
                )}
                {f.type === 'studio' && (
                  <MonacoEditor
                    defaultValue={this.setDefaultValue(f)}
                    onUpdateValue={(v) => this.onUpdateValue(v, f)}
                  />
                )}
              </NFormItem>
            )
          })}
        </NForm>
      </Modal>
    )
  }
})

export { TaskForm }
