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
  computed,
  getCurrentInstance
} from 'vue'
import { NForm, NFormItem, NInput } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import Modal from '@/components/modal'
import { noSpace } from '@/utils/trim'
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
    const { folderState: state } = useForm()

    const { handleCreateResource, handleRenameResource } = useModal(state, ctx)

    const hideModal = () => {
      ctx.emit('update:show')
    }

    const handleCreate = () => {
      handleCreateResource()
    }

    const handleRename = () => {
      handleRenameResource(props.row.id)
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    watch(
      () => props.row,
      () => {
        state.folderForm.name = props.row.alias
        state.folderForm.description = props.row.description
      }
    )
    const fileEdit = computed(() => props.row.id && !props.row.directory)

    return {
      fileEdit,
      hideModal,
      handleCreate,
      handleRename,
      ...toRefs(state),
      trim
    }
  },
  render() {
    const { t } = useI18n()

    return (
      <Modal
        show={this.$props.show}
        title={
          this.row.id ? t('resource.udf.edit') : t('resource.udf.create_folder')
        }
        onCancel={this.hideModal}
        onConfirm={this.row.id ? this.handleRename : this.handleCreate}
        confirmClassName='btn-submit'
        cancelClassName='btn-cancel'
        confirmLoading={this.saving}
      >
        <NForm rules={this.rules} ref='folderFormRef'>
          <NFormItem
            label={
              this.fileEdit
                ? t('resource.udf.file_name')
                : t('resource.udf.folder_name')
            }
            path='name'
          >
            <NInput
              allowInput={this.fileEdit ? this.trim : noSpace}
              v-model={[this.folderForm.name, 'value']}
              placeholder={t('resource.udf.enter_name_tips')}
              class='input-directory-name'
            />
          </NFormItem>
          <NFormItem label={t('resource.udf.description')} path='description'>
            <NInput
              allowInput={this.trim}
              type='textarea'
              v-model={[this.folderForm.description, 'value']}
              placeholder={t('resource.udf.enter_description_tips')}
              class='input-description'
            />
          </NFormItem>
        </NForm>
      </Modal>
    )
  }
})
