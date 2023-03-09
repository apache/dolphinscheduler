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
  getCurrentInstance,
  PropType,
  toRefs,
  watch
} from 'vue'
import { NForm, NFormItem, NInput } from 'naive-ui'
import { useForm } from './use-form'
import Modal from '@/components/modal'
import { useUserStore } from '@/store/user/user'
import type { UserInfoRes } from '@/service/modules/users/types'

const props = {
  showModalRef: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  statusRef: {
    type: Number as PropType<number>,
    default: 0
  },
  row: {
    type: Object as PropType<any>,
    default: {}
  }
}

const ProjectModal = defineComponent({
  name: 'ProjectModal',
  props,
  emits: ['cancelModal', 'confirmModal'],
  setup(props, ctx) {
    const { variables, t, handleValidate } = useForm(props, ctx)

    const userStore = useUserStore()

    const cancelModal = () => {
      if (props.statusRef === 0) {
        variables.model.projectName = ''
        variables.model.description = ''
      } else {
        variables.model.userName = props.row.userName
        variables.model.projectName = props.row.name
        variables.model.description = props.row.description
      }
      ctx.emit('cancelModal', props.showModalRef)
    }

    const confirmModal = () => {
      handleValidate(props.statusRef)
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    watch(
      () => props.statusRef,
      () => {
        if (props.statusRef === 0) {
          variables.model.projectName = ''
          variables.model.userName = (
            userStore.getUserInfo as UserInfoRes
          ).userName
          variables.model.description = ''
        } else {
          variables.model.projectName = props.row.name
          variables.model.userName = props.row.userName
          variables.model.description = props.row.description
        }
      }
    )

    watch(
      () => props.row,
      () => {
        variables.model.projectName = props.row.name
        variables.model.userName = props.row.userName
        variables.model.description = props.row.description
      }
    )

    return { ...toRefs(variables), t, cancelModal, confirmModal, trim }
  },
  render() {
    const { t } = this
    return (
      <Modal
        title={
          this.statusRef === 0
            ? t('project.list.create_project')
            : t('project.list.edit_project')
        }
        show={this.showModalRef}
        onConfirm={this.confirmModal}
        onCancel={this.cancelModal}
        confirmDisabled={!this.model.projectName || !this.model.userName}
        confirmClassName='btn-submit'
        cancelClassName='btn-cancel'
        confirmLoading={this.saving}
      >
        <NForm rules={this.rules} ref='projectFormRef'>
          <NFormItem label={t('project.list.project_name')} path='projectName'>
            <NInput
              allowInput={this.trim}
              v-model={[this.model.projectName, 'value']}
              placeholder={t('project.list.project_tips')}
              class='input-project-name'
            />
          </NFormItem>
          <NFormItem label={t('project.list.owned_users')} path='userName'>
            <NInput
              allowInput={this.trim}
              disabled={true}
              v-model={[this.model.userName, 'value']}
              placeholder={t('project.list.username_tips')}
            />
          </NFormItem>
          <NFormItem
            label={t('project.list.project_description')}
            path='description'
          >
            <NInput
              allowInput={this.trim}
              v-model={[this.model.description, 'value']}
              type='textarea'
              placeholder={t('project.list.description_tips')}
            />
          </NFormItem>
        </NForm>
      </Modal>
    )
  }
})

export default ProjectModal
