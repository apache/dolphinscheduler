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

import { defineComponent, toRefs, PropType, getCurrentInstance } from 'vue'
import { NForm, NFormItem, NInput, NUpload, NButton, NIcon } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import Modal from '@/components/modal'
import { useForm } from './use-form'
import { useModal } from './use-modal'
import { CloudUploadOutlined } from '@vicons/antd'

const props = {
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
    const { uploadState: state } = useForm()
    const { handleUploadFile } = useModal(state, ctx)

    const hideModal = () => {
      state.uploadForm.name = ''
      state.uploadForm.description = ''
      state.uploadForm.file = ''
      ctx.emit('update:show')
    }

    const handleFolder = () => {
      handleUploadFile()
    }

    const customRequest = ({ file }: any) => {
      state.uploadForm.name = file.name
      state.uploadForm.file = file.file
      state.uploadFormRef.validate()
    }

    const removeFile = () => {
      state.uploadForm.name = ''
      state.uploadForm.file = ''
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    return {
      hideModal,
      handleFolder,
      customRequest,
      removeFile,
      ...toRefs(state),
      trim
    }
  },
  render() {
    const { t } = useI18n()
    return (
      <Modal
        show={this.$props.show}
        title={t('resource.udf.file_upload')}
        onCancel={this.hideModal}
        onConfirm={this.handleFolder}
        confirmClassName='btn-submit'
        cancelClassName='btn-cancel'
        confirmLoading={this.saving}
      >
        <NForm rules={this.rules} ref='uploadFormRef'>
          <NFormItem label={t('resource.udf.file_name')} path='name'>
            <NInput
              allowInput={this.trim}
              v-model={[this.uploadForm.name, 'value']}
              placeholder={t('resource.udf.enter_name_tips')}
              class='input-file-name'
            />
          </NFormItem>
          <NFormItem label={t('resource.udf.description')} path='description'>
            <NInput
              allowInput={this.trim}
              type='textarea'
              v-model={[this.uploadForm.description, 'value']}
              placeholder={t('resource.udf.enter_description_tips')}
              class='input-description'
            />
          </NFormItem>
          <NFormItem label={t('resource.udf.upload_files')} path='file'>
            <NUpload
              v-model={[this.uploadForm.file, 'value']}
              customRequest={this.customRequest}
              max={1}
              class='btn-upload'
              onRemove={this.removeFile}
            >
              <NButton>
                {t('resource.udf.upload')}
                <NIcon>
                  <CloudUploadOutlined />
                </NIcon>
              </NButton>
            </NUpload>
          </NFormItem>
        </NForm>
      </Modal>
    )
  }
})
