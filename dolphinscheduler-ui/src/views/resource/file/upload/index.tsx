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
import {
  NForm,
  NFormItem,
  NUpload,
  NUploadDragger,
  NSpace,
  NIcon,
  NText
} from 'naive-ui'
import { UploadOutlined, LoadingOutlined } from '@vicons/antd'
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
    const { onSelectFile, handleUploadFile } = useUpload(state)

    const hideModal = () => {
      ctx.emit('update:show')
    }

    const handleFile = () => {
      console.log(state.uploadForm.files)
      handleUploadFile(ctx.emit, hideModal, resetForm)
    }

    return {
      hideModal,
      handleFile,
      onSelectFile,
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
        <NForm rules={this.rules} ref='uploadFormRef' model={this.uploadForm}>
          <NFormItem path='files'>
            <NUpload class='btn-upload' on-change={this.onSelectFile} multiple>
              <NUploadDragger>
                <NSpace vertical>
                  <NIcon size='48' depth='3'>
                    {this.saving ? <LoadingOutlined /> : <UploadOutlined />}
                  </NIcon>
                  <NText>{t('resource.file.upload_tips')}</NText>
                </NSpace>
              </NUploadDragger>
            </NUpload>
          </NFormItem>
        </NForm>
      </Modal>
    )
  }
})
