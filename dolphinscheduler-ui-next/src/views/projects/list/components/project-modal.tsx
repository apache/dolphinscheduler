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

import { defineComponent, PropType, toRefs, onMounted } from 'vue'
import { NForm, NFormItem, NInput } from 'naive-ui'
import { useForm } from '../use-form'
import Modal from '@/components/modal'
import { createProject, updateProject } from '@/service/modules/projects'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  data: {
    type: Object as PropType<any>
  },
  status: {
    type: Number as PropType<number>,
    default: 0
  }
}

const ProjectModal = defineComponent({
  name: 'ProjectModal',
  props,
  emits: ['confirm', 'cancel'],
  setup(props, { emit }) {
    const { state, t } = useForm()

    onMounted(() => {
      if (props.status === 1) {
        state.projectForm.projectName = props.data.projectName
        state.projectForm.description = props.data.description
      }
    })

    const onConfirm = () => {
      ;(props.status === 1
        ? updateProject(state.projectForm, props.data.code)
        : createProject(state.projectForm)
      ).then(() => {
        emit('confirm')
      })
    }

    const onCancel = () => {
      state.projectForm.projectName = ''
      state.projectForm.description = ''
      state.projectForm.userName = ''
      emit('cancel')
    }

    return { ...toRefs(state), t, onConfirm, onCancel }
  },
  render() {
    const { t, onConfirm, onCancel, show, status } = this
    return (
      <Modal
        title={
          status === 0
            ? t('project.list.create_project')
            : t('project.list.edit_project')
        }
        show={show}
        onConfirm={onConfirm}
        onCancel={onCancel}
        confirmDisabled={
          !this.projectForm.projectName || !this.projectForm.userName
        }
      >
        <NForm rules={this.rules} ref='projectFormRef'>
          <NFormItem label={t('project.list.project_name')} path='projectName'>
            <NInput
              v-model={[this.projectForm.projectName, 'value']}
              placeholder={t('project.list.project_tips')}
            />
          </NFormItem>
          <NFormItem label={t('project.list.owned_users')} path='userName'>
            <NInput
              v-model={[this.projectForm.userName, 'value']}
              placeholder={t('project.list.username_tips')}
            />
          </NFormItem>
          <NFormItem
            label={t('project.list.project_description')}
            path='description'
          >
            <NInput
              v-model={[this.projectForm.description, 'value']}
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
