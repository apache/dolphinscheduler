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

import { defineComponent, toRefs, PropType } from 'vue'
import { NForm, NFormItem, NInput } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import Modal from '@/components/modal'
import { useForm } from './use-form'
import { useFolder } from './use-folder'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  }
}

export default defineComponent({
  name: 'ResourceFileFolder',
  props,
  emits: ['updateList', 'update:show'],
  setup(props, ctx) {
    const { state, resetForm } = useForm()
    const { handleCreateFolder } = useFolder(state)

    const hideModal = () => {
      ctx.emit('update:show')
    }

    const handleFolder = () => {
      handleCreateFolder(ctx.emit, hideModal, resetForm)
    }

    return {
      hideModal,
      handleFolder,
      ...toRefs(state)
    }
  },
  render() {
    const { t } = useI18n()
    return (
      <Modal
        show={this.$props.show}
        title={t('resource.file.create_folder')}
        onCancel={this.hideModal}
        onConfirm={this.handleFolder}
        confirmClassName='btn-submit'
        cancelClassName='btn-cancel'
        confirmLoading={this.saving}
      >
        <NForm rules={this.rules} ref='folderFormRef'>
          <NFormItem label={t('resource.file.folder_name')} path='name'>
            <NInput
              v-model={[this.folderForm.name, 'value']}
              placeholder={t('resource.file.enter_name_tips')}
              class='input-directory-name'
            />
          </NFormItem>
          <NFormItem label={t('resource.file.description')} path='description'>
            <NInput
              type='textarea'
              v-model={[this.folderForm.description, 'value']}
              placeholder={t('resource.file.enter_description_tips')}
              class='input-description'
            />
          </NFormItem>
        </NForm>
      </Modal>
    )
  }
})
