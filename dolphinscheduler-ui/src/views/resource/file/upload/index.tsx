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
import { NButton, NForm, NFormItem, NInput, NUpload } from 'naive-ui'

import { useI18n } from 'vue-i18n'
import Modal from '@/components/modal'
import { useForm } from './use-form'
import { useUpload } from './use-upload'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  }
}

export default defineComponent({
  name: 'ResourceFileUpload',
  props,
  emits: ['updateList', 'update:show'],
  setup(props, ctx) {
    const { state, resetForm } = useForm()
    const { handleUploadFile } = useUpload(state)

    const hideModal = () => {
      ctx.emit('update:show')
    }

    const customRequest = ({ file }: any) => {
      state.uploadForm.name = file.name
      state.uploadForm.file = file.file
      state.uploadFormNameRef.validate({
        trigger: 'input'
      })
    }

    const handleFile = () => {
      handleUploadFile(ctx.emit, hideModal, resetForm)
    }

    const removeFile = () => {
      state.uploadForm.name = ''
    }

    return {
      hideModal,
      customRequest,
      handleFile,
      removeFile,
      ...toRefs(state)
    }
  },
  render() {
    const { t } = useI18n()
    return (
      <Modal
        show={this.$props.show}
        title={t('resource.file.upload_files')}
        onCancel={this.hideModal}
        onConfirm={this.handleFile}
        confirmClassName='btn-submit'
        cancelClassName='btn-cancel'
        confirmLoading={this.saving}
      >
        <NForm rules={this.rules} ref='uploadFormRef'>
          <NFormItem
            label={t('resource.file.file_name')}
            path='name'
            ref='uploadFormNameRef'
          >
            <NInput
              v-model={[this.uploadForm.name, 'value']}
              placeholder={t('resource.file.enter_name_tips')}
              class='input-file-name'
            />
          </NFormItem>
          <NFormItem label={t('resource.file.description')} path='description'>
            <NInput
              type='textarea'
              v-model={[this.uploadForm.description, 'value']}
              placeholder={t('resource.file.enter_description_tips')}
              class='input-description'
            />
          </NFormItem>
          <NFormItem label={t('resource.file.upload_files')} path='file'>
            <NUpload
              v-model={[this.uploadForm.file, 'value']}
              customRequest={this.customRequest}
              class='btn-upload'
              max={1}
              onRemove={this.removeFile}
            >
              <NButton>{t('resource.file.upload_files')}</NButton>
            </NUpload>
          </NFormItem>
        </NForm>
      </Modal>
    )
  }
})
