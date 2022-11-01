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

import { defineComponent, PropType } from 'vue'
import { NForm } from 'naive-ui'
import { useTaskForm } from './use-task-form'
import Modal from '@/components/modal'

const props = {
  showModal: {
    type: Boolean as PropType<boolean>,
    default: false
  }
}

const TaskForm = defineComponent({
  name: 'TaskForm',
  props,
  emits: ['cancelModal', 'confirmModal'],
  setup(props, ctx) {
    const { variables } = useTaskForm()

    const cancelModal = () => {
      ctx.emit('cancelModal')
    }

    const confirmModal = () => {
      ctx.emit('confirmModal')
    }

    return { ...variables, cancelModal, confirmModal }
  },
  render() {
    return (
      <Modal
        title={''}
        show={this.showModal}
        onCancel={this.cancelModal}
        onConfirm={this.confirmModal}>
        <NForm
          ref={'TaskForm'}>

        </NForm>
      </Modal>
    )
  }
})

export { TaskForm }