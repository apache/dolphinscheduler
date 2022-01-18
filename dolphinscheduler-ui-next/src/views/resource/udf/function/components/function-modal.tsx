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

import { defineComponent, toRefs, PropType, watch, onMounted } from 'vue'
import {
  NForm,
  NFormItem,
  NInput,
  NInputGroup,
  NRadio,
  NTreeSelect,
  NButton,
  NRadioGroup
} from 'naive-ui'
import { useI18n } from 'vue-i18n'
import Modal from '@/components/modal'
import { useForm } from './use-form'
import { useModal } from './use-modal'
import type { IUdf } from '../types'

const props = {
  row: {
    type: Object as PropType<IUdf>,
    default: {}
  },
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  }
}

export default defineComponent({
  name: 'ResourceFileFolder',
  props,
  emits: ['update:show', 'updateList'],
  setup(props, ctx) {
    const { state } = useForm()

    const { variables, handleCreateFunc, handleRenameFunc, getUdfList } =
      useModal(state, ctx)

    const hideModal = () => {
      ctx.emit('update:show')
    }

    const handleCreate = () => {
      handleCreateFunc()
    }

    const handleRename = () => {
      handleRenameFunc(props.row.id)
    }

    onMounted(() => {
      getUdfList()
    })

    watch(
      () => props.row,
      () => {
        state.functionForm.type = props.row.type
        state.functionForm.funcName = props.row.funcName
        state.functionForm.className = props.row.className
        state.functionForm.resourceId = props.row.resourceId
        state.functionForm.description = props.row.description
      }
    )
    return {
      hideModal,
      handleCreate,
      handleRename,
      ...toRefs(state),
      ...toRefs(variables)
    }
  },
  render() {
    const { t } = useI18n()

    return (
      <Modal
        show={this.$props.show}
        title={
          this.row.id
            ? t('resource.function.edit_udf_function')
            : t('resource.function.create_udf_function')
        }
        onCancel={this.hideModal}
        onConfirm={this.row.id ? this.handleRename : this.handleCreate}
      >
        <NForm
          rules={this.rules}
          ref='functionFormRef'
          label-placement='left'
          label-width='160'
        >
          <NFormItem label={t('resource.function.type')} path='type'>
            <NRadioGroup
              v-model={[this.functionForm.type, 'value']}
              name='type'
            >
              <NRadio value='HIVE' checked>
                HIVE UDF
              </NRadio>
            </NRadioGroup>
          </NFormItem>
          <NFormItem
            label={t('resource.function.udf_function_name')}
            path='funcName'
          >
            <NInput
              v-model={[this.functionForm.funcName, 'value']}
              placeholder={t('resource.function.enter_udf_unction_name_tips')}
            />
          </NFormItem>
          <NFormItem
            label={t('resource.function.package_name')}
            path='className'
          >
            <NInput
              v-model={[this.functionForm.className, 'value']}
              placeholder={t('resource.function.enter_package_name_tips')}
            />
          </NFormItem>
          <NFormItem
            label={t('resource.function.udf_resources')}
            path='resourceId'
          >
            <NInputGroup>
              <NTreeSelect
                options={this.udfResourceList}
                label-field='fullName'
                key-field='id'
                v-model={[this.functionForm.resourceId, 'value']}
                placeholder={t(
                  'resource.function.enter_select_udf_resources_directory_tips'
                )}
                defaultValue={this.functionForm.resourceId}
              ></NTreeSelect>
              <NButton type='primary' ghost>
                {t('resource.function.upload_resources')}
              </NButton>
            </NInputGroup>
          </NFormItem>
          <NFormItem
            label={t('resource.function.instructions')}
            path='description'
          >
            <NInput
              type='textarea'
              v-model={[this.functionForm.description, 'value']}
              placeholder={t('resource.function.enter_instructions_tips')}
            />
          </NFormItem>
        </NForm>
      </Modal>
    )
  }
})
