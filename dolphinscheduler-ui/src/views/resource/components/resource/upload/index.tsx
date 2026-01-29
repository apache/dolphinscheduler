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
  getCurrentInstance,
  watch
} from 'vue'
import { NButton, NForm, NFormItem, NInput, NUpload } from 'naive-ui'

import { useI18n } from 'vue-i18n'
import Modal from '@/components/modal'
import { useForm } from './use-form'
import { useUpload } from './use-upload'
import { ResourceType } from '@/views/resource/components/resource/types'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  resourceType: {
    type: String as PropType<ResourceType>,
    default: undefined
  },
  isReupload: {
    type: Boolean as PropType<boolean>,
    default: false
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
  name: 'ResourceFileUpload',
  props,
  emits: ['updateList', 'update:show'],
  setup(props, ctx) {
    const { state, resetForm } = useForm()
    const { handleUploadFile } = useUpload(state)

    const hideModal = () => {
      resetForm()
      ctx.emit('update:show')
    }

    const customRequest = ({ file }: any) => {
      state.uploadForm.name = file.name
      state.uploadForm.file = file.file
      state.uploadFormRef.validate()
    }

    const handleFile = () => {
      handleUploadFile(ctx.emit, hideModal, resetForm)
    }

    const removeFile = () => {
      state.uploadForm.name = ''
      state.uploadForm.file = ''
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    watch(
      () => props.show,
      () => {
        state.uploadForm.type = props.resourceType!
        state.uploadForm.isReupload = props.isReupload
        if (props.isReupload && props.show) {
          state.uploadForm.fullName = props.fullName
          state.uploadForm.name = props.name
          state.uploadForm.user_name = props.userName
        }
      }
    )

    return {
      hideModal,
      customRequest,
      handleFile,
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
              allowInput={this.trim}
              v-model={[this.uploadForm.name, 'value']}
              placeholder={t('resource.file.enter_name_tips')}
              class='input-file-name'
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
