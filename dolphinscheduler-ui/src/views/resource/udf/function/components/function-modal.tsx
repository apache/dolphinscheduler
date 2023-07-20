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
  onMounted,
  ref,
  getCurrentInstance
} from 'vue'
import {
  NUpload,
  NIcon,
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
import { CloudUploadOutlined } from '@vicons/antd'
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
    const treeRef = ref()
    const { state, uploadState } = useForm()

    const {
      variables,
      handleCreateFunc,
      handleRenameFunc,
      getUdfList,
      handleUploadFile
    } = useModal(state, uploadState, ctx)

    const hideModal = () => {
      ctx.emit('update:show')
    }

    const handleCreate = () => {
      handleCreateFunc()
    }

    const handleRename = () => {
      handleRenameFunc(props.row.id)
    }

    const handleUpload = () => {
      uploadState.uploadForm.currentDir = `/${treeRef.value.selectedOption?.fullName}`
      handleUploadFile()
    }

    const customRequest = ({ file }: any) => {
      uploadState.uploadForm.name = file.name
      uploadState.uploadForm.file = file.file
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    onMounted(() => {
      getUdfList()
    })

    watch(
      () => props.row,
      () => {
        variables.uploadShow = false
        state.functionForm.type = props.row.type || 'HIVE'
        state.functionForm.funcName = props.row.funcName
        state.functionForm.className = props.row.className
        state.functionForm.resourceId = props.row.resourceId || -1
        state.functionForm.fullName = props.row.resourceName || ''
        state.functionForm.description = props.row.description
      }
    )
    return {
      treeRef,
      hideModal,
      handleCreate,
      handleRename,
      customRequest,
      handleUpload,
      ...toRefs(state),
      ...toRefs(uploadState),
      ...toRefs(variables),
      trim
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
        confirmClassName='btn-submit'
        cancelClassName='btn-cancel'
        confirmLoading={this.saving}
      >
        <NForm rules={this.rules} ref='functionFormRef'>
          <NFormItem label={t('resource.function.type')} path='type'>
            <NRadioGroup
              v-model={[this.functionForm.type, 'value']}
              name='type'
              class='radio-function-type'
            >
              <NRadio value='HIVE'>HIVE UDF</NRadio>
            </NRadioGroup>
          </NFormItem>
          <NFormItem
            label={t('resource.function.udf_function_name')}
            path='funcName'
          >
            <NInput
              allowInput={this.trim}
              v-model={[this.functionForm.funcName, 'value']}
              placeholder={t('resource.function.enter_udf_unction_name_tips')}
              class='input-function-name'
            />
          </NFormItem>
          <NFormItem
            label={t('resource.function.package_name')}
            path='className'
          >
            <NInput
              allowInput={this.trim}
              v-model={[this.functionForm.className, 'value']}
              placeholder={t('resource.function.enter_package_name_tips')}
              class='input-class-name'
            />
          </NFormItem>
          <NFormItem
            label={t('resource.function.udf_resources')}
            path='fullName'
          >
            <NInputGroup>
              <NTreeSelect
                options={this.udfResourceList}
                label-field='name'
                key-field='fullName'
                check-strategy='child'
                v-model={[this.functionForm.fullName, 'value']}
                placeholder={t(
                  'resource.function.enter_select_udf_resources_tips'
                )}
                defaultValue={this.functionForm.fullName}
                disabled={this.uploadShow}
                showPath={false}
                class='btn-udf-resource-dropdown'
              />
              <NButton
                type='primary'
                ghost
                onClick={() => (this.uploadShow = !this.uploadShow)}
              >
                {t('resource.function.upload_resources')}
              </NButton>
            </NInputGroup>
          </NFormItem>
          {this.uploadShow && (
            <NForm rules={this.uploadRules} ref='uploadFormRef'>
              <NFormItem
                label={t('resource.function.udf_resources_directory')}
                path='pid'
                show-feedback={false}
                style={{ marginBottom: '5px' }}
              >
                <NTreeSelect
                  ref='treeRef'
                  options={this.udfResourceDirList}
                  label-field='fullName'
                  key-field='id'
                  v-model={[this.uploadForm.pid, 'value']}
                  placeholder={t(
                    'resource.function.enter_select_udf_resources_directory_tips'
                  )}
                  defaultValue={this.uploadForm.pid}
                />
              </NFormItem>
              <NFormItem
                label=' '
                show-feedback={false}
                style={{ marginBottom: '5px' }}
              >
                <NInputGroup>
                  <NInput
                    allowInput={this.trim}
                    v-model={[this.uploadForm.name, 'value']}
                    placeholder={t('resource.function.enter_name_tips')}
                  />
                  <NUpload
                    v-model={[this.uploadForm.file, 'value']}
                    customRequest={this.customRequest}
                    showFileList={false}
                    style={{ width: 'auto' }}
                  >
                    <NButton>
                      {t('resource.function.upload')}
                      <NIcon>
                        <CloudUploadOutlined />
                      </NIcon>
                    </NButton>
                  </NUpload>
                </NInputGroup>
              </NFormItem>
              <NFormItem
                label=' '
                path='description'
                show-feedback={false}
                style={{ marginBottom: '5px' }}
              >
                <NInput
                  type='textarea'
                  v-model={[this.uploadForm.description, 'value']}
                  placeholder={t('resource.function.enter_description_tips')}
                  class='input-description'
                />
              </NFormItem>
              <NFormItem label=' '>
                <NButton onClick={this.handleUpload}>
                  {t('resource.function.upload_udf_resources')}
                </NButton>
              </NFormItem>
            </NForm>
          )}

          <NFormItem
            label={t('resource.function.instructions')}
            path='description'
          >
            <NInput
              type='textarea'
              v-model={[this.functionForm.description, 'value']}
              placeholder={t('resource.function.enter_instructions_tips')}
              class='input-description'
            />
          </NFormItem>
        </NForm>
      </Modal>
    )
  }
})
