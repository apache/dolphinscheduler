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

import { defineComponent, PropType, toRefs } from 'vue'
import { useI18n } from 'vue-i18n'
import Modal from '@/components/modal'
import { useForm } from './use-form'
import { useModal } from './use-modal'
import { NForm, NFormItem, NButton, NUpload, NIcon, NInput } from 'naive-ui'
import { CloudUploadOutlined } from '@vicons/antd'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  }
}

export default defineComponent({
  name: 'workflowDefinitionImport',
  props,
  emits: ['update:show', 'update:row', 'updateList'],
  setup(props, ctx) {
    const { importState } = useForm()
    const { handleImportDefinition } = useModal(importState, ctx)
    const hideModal = () => {
      ctx.emit('update:show')
    }

    const handleImport = () => {
      handleImportDefinition()
    }

    const customRequest = ({ file }: any) => {
      importState.importForm.name = file.name
      importState.importForm.file = file.file
    }

    return {
      hideModal,
      handleImport,
      customRequest,
      ...toRefs(importState)
    }
  },

  render() {
    const { t } = useI18n()

    return (
      <Modal
        show={this.$props.show}
        title={t('project.workflow.upload')}
        onCancel={this.hideModal}
        onConfirm={this.handleImport}
        confirmLoading={this.saving}
      >
        <NForm rules={this.importRules} ref='importFormRef'>
          <NFormItem label={t('project.workflow.upload_file')} path='file'>
            <NButton>
              <NUpload
                v-model={[this.importForm.file, 'value']}
                customRequest={this.customRequest}
                showFileList={false}
              >
                <NButton text>
                  {t('project.workflow.upload')}
                  <NIcon>
                    <CloudUploadOutlined />
                  </NIcon>
                </NButton>
              </NUpload>
            </NButton>
          </NFormItem>
          <NFormItem label={t('project.workflow.file_name')} path='name'>
            <NInput
              v-model={[this.importForm.name, 'value']}
              placeholder={''}
              disabled
            />
          </NFormItem>
        </NForm>
      </Modal>
    )
  }
})
