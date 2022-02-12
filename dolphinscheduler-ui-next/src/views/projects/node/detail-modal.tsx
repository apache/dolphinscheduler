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

import { defineComponent, PropType, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import Modal from '@/components/modal'
import Detail from './detail'
import type { IDataNode, ITask } from './types'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  nodeData: {
    type: Object as PropType<IDataNode>,
    default: {
      taskType: 'SHELL'
    }
  },
  type: {
    type: String as PropType<string>,
    default: ''
  },
  taskDefinition: {
    type: Object as PropType<ITask>
  }
}

const NodeDetailModal = defineComponent({
  name: 'NodeDetailModal',
  props,
  emits: ['cancel', 'update'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const detailRef = ref()
    const onConfirm = () => {
      detailRef.value.onSubmit()
    }
    const onCancel = () => {
      emit('cancel')
    }

    return {
      t,
      detailRef,
      onConfirm,
      onCancel
    }
  },
  render() {
    const { t, show, onConfirm, onCancel } = this
    return (
      <Modal
        show={show}
        title={`${t('project.node.current_node_settings')}`}
        onConfirm={onConfirm}
        confirmLoading={false}
        onCancel={onCancel}
      >
        <Detail ref='detailRef' taskType='SHELL' projectCode={111} />
      </Modal>
    )
  }
})

export default NodeDetailModal
