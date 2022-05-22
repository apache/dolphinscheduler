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
import { modifyTaskGroupQueuePriority } from '@/service/modules/task-group'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  data: {
    type: Object as PropType<any>
  }
}

const FormModal = defineComponent({
  name: 'FormModal',
  props,
  emits: ['confirm', 'cancel'],
  setup(props, { emit }) {
    const { state, t } = useForm()

    onMounted(() => {
      state.formData.queueId = props.data.queueId
      state.formData.priority = props.data.priority
    })

    const onConfirm = async () => {
      if (state.saving) return
      state.saving = true
      try {
        const value = state.formData.priority + ''
        if (value) {
          await modifyTaskGroupQueuePriority(state.formData)
          emit('confirm')
        }
        state.saving = false
      } catch (err) {
        state.saving = false
      }
    }

    const onCancel = () => {
      state.formData.priority = 0
      emit('cancel')
    }

    return { ...toRefs(state), t, onConfirm, onCancel }
  },
  render() {
    const { t, onConfirm, onCancel, show } = this
    return (
      <Modal
        title={t('resource.task_group_queue.edit_priority')}
        show={show}
        onConfirm={onConfirm}
        onCancel={onCancel}
        confirmLoading={this.saving}
      >
        <NForm rules={this.rules} ref='formRef'>
          <NFormItem
            label={t('resource.task_group_queue.priority')}
            path='priority'
          >
            <NInput v-model:value={this.formData.priority} />
          </NFormItem>
        </NForm>
      </Modal>
    )
  }
})

export default FormModal
