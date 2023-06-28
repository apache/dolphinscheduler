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
  toRefs,
  PropType,
  watch,
  getCurrentInstance
} from 'vue'
import { NForm, NFormItem, NInput } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import Modal from '@/components/modal'
import { useForm } from './use-form'
import { useRename } from './use-rename'
import type { ResourceType } from '@/views/resource/components/resource/types'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  resourceType: {
    type: String as PropType<ResourceType>,
    default: undefined
  },
  name: {
    type: String as PropType<string>,
    default: ''
  },
  description: {
    type: String as PropType<string>,
    default: ''
  },
  fullName: {
    type: String as PropType<string>,
    default: ''
  },
  userName: {
    type: String as PropType<string>,
    default: ''
  }
}

export default defineComponent({
  name: 'ResourceFileRename',
  props,
  emits: ['updateList', 'update:show'],
  setup(props, ctx) {
    const { state, resetForm } = useForm(
      props.resourceType!,
      props.fullName,
      props.name,
      props.description,
      props.userName
    )
    const { handleRenameFile } = useRename(state)
    const hideModal = () => {
      ctx.emit('update:show', false)
    }

    const handleFile = () => {
      handleRenameFile(ctx.emit, hideModal, resetForm)
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    watch(
      () => props.show,
      () => {
        state.renameForm.fullName = props.fullName
        state.renameForm.name = props.name
        state.renameForm.description = props.description
        state.renameForm.user_name = props.userName
      }
    )

    return { hideModal, handleFile, ...toRefs(state), trim }
  },
  render() {
    const { t } = useI18n()
    return (
      <Modal
        show={this.$props.show}
        title={t('resource.file.rename')}
        onCancel={this.hideModal}
        onConfirm={this.handleFile}
        confirmClassName='btn-submit'
        cancelClassName='btn-cancel'
        confirmLoading={this.saving}
      >
        <NForm rules={this.rules} ref='renameFormRef'>
          <NFormItem label={t('resource.file.name')} path='name'>
            <NInput
              allowInput={this.trim}
              v-model={[this.renameForm.name, 'value']}
              placeholder={t('resource.file.enter_name_tips')}
              class='input-name'
            />
          </NFormItem>
        </NForm>
      </Modal>
    )
  }
})
