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
import Modal from '@/components/modal'
import { NForm, NFormItem, NInput, NSelect } from 'naive-ui'
import { useModal } from './use-modal'
import { useI18n } from 'vue-i18n'
import { DATA_TYPES_MAP, DEFAULT_DATA_TYPE } from "@/views/projects/parameter/data_type"

const ParameterModal = defineComponent({
  name: 'ParameterModal',
  props: {
    showModalRef: {
      type: Boolean as PropType<boolean>,
      default: false
    },
    statusRef: {
      type: Number as PropType<number>,
      default: 0
    },
    row: {
      type: Object as PropType<any>,
      default: {}
    }
  },
  emits: ['cancelModal', 'confirmModal'],
  setup(props, ctx) {
    const { variables, handleValidate } = useModal(props, ctx)
    const { t } = useI18n()

    const cancelModal = () => {
      if (props.statusRef === 0) {
        variables.model.projectParameterName = ''
        variables.model.projectParameterValue = ''
        variables.model.projectParameterDataType = DEFAULT_DATA_TYPE
      } else {
        variables.model.projectParameterName = props.row.paramName
        variables.model.projectParameterValue = props.row.paramValue
        variables.model.projectParameterDataType = props.row.paramDataType
      }
      ctx.emit('cancelModal', props.showModalRef)
    }

    const confirmModal = () => {
      handleValidate(props.statusRef)
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    watch(
      () => props.showModalRef,
      () => {
        props.showModalRef
      }
    )

    watch(
      () => props.statusRef,
      () => {
        if (props.statusRef === 0) {
          variables.model.projectParameterName = ''
          variables.model.projectParameterValue = ''
          variables.model.projectParameterDataType = DEFAULT_DATA_TYPE
        } else {
          variables.model.code = props.row.code
          variables.model.projectParameterName = props.row.paramName
          variables.model.projectParameterValue = props.row.paramValue
          variables.model.projectParameterDataType = props.row.paramDataType
        }
      }
    )

    watch(
      () => props.row,
      () => {
        variables.model.code = props.row.code
        variables.model.projectParameterName = props.row.paramName
        variables.model.projectParameterValue = props.row.paramValue
        variables.model.projectParameterDataType = props.row.paramDataType
      }
    )

    return { t, ...toRefs(variables), cancelModal, confirmModal, trim }
  },
  render() {
    const { t } = this
    return (
      <div>
        <Modal
          title={
            this.statusRef === 0
              ? t('project.parameter.create_parameter')
              : t('project.parameter.edit_parameter')
          }
          show={this.showModalRef}
          onCancel={this.cancelModal}
          onConfirm={this.confirmModal}
          confirmDisabled={
            !this.model.projectParameterName ||
            !this.model.projectParameterValue ||
            !this.model.projectParameterDataType
          }
          confirmClassName='btn-submit'
          cancelClassName='btn-cancel'
          confirmLoading={this.saving}
        >
          {{
            default: () => (
              <NForm model={this.model} rules={this.rules} ref='formRef'>
                <NFormItem label={t('project.parameter.name')} path='name'>
                  <NInput
                    allowInput={this.trim}
                    placeholder={t('project.parameter.name_tips')}
                    v-model={[this.model.projectParameterName, 'value']}
                  />
                </NFormItem>
                <NFormItem label={t('project.parameter.value')} path='value'>
                  <NInput
                    allowInput={this.trim}
                    placeholder={t('project.parameter.value_tips')}
                    v-model={[this.model.projectParameterValue, 'value']}
                  />
                </NFormItem>
                <NFormItem label={t('project.parameter.data_type')} path='data_type'>
                  <NSelect
                      placeholder={t('project.parameter.data_type_tips')}
                      options={Object.keys(DATA_TYPES_MAP).map((item) => {
                        return { value: item, label: item }
                      })}
                      v-model={[this.model.projectParameterDataType, 'value']}
                  />
                </NFormItem>
              </NForm>
            )
          }}
        </Modal>
      </div>
    )
  }
})

export default ParameterModal
