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
import type { NodeData } from '@/views/projects/workflow/components/dag/types'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  taskDefinition: {
    type: Object as PropType<NodeData>,
    default: { code: 0, taskType: 'SHELL', name: '' }
  },
  projectCode: {
    type: Number as PropType<number>,
    required: true
  },
  readonly: {
    type: Boolean as PropType<boolean>,
    default: false
  }
}

const NodeDetailModal = defineComponent({
  name: 'NodeDetailModal',
  props,
  emits: ['cancel', 'submit'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const detailRef = ref()

    // TODO
    const mapFormToTaskDefinition = (form: any) => {
      return {
        // "code": form.code,
        name: form.name,
        description: form.desc,
        taskType: 'SHELL',
        taskParams: {
          resourceList: [],
          localParams: form.localParams,
          rawScript: form.shell,
          dependence: {},
          conditionResult: {
            successNode: [],
            failedNode: []
          },
          waitStartTimeout: {},
          switchResult: {}
        },
        flag: form.runFlag,
        taskPriority: 'MEDIUM',
        workerGroup: form.workerGroup,
        failRetryTimes: '0',
        failRetryInterval: '1',
        timeoutFlag: 'CLOSE',
        timeoutNotifyStrategy: '',
        timeout: 0,
        delayTime: '0',
        environmentCode: form.environmentCode
      }
    }
    const onConfirm = () => {
      emit('submit', {
        formRef: detailRef.value.formRef,
        form: mapFormToTaskDefinition(detailRef.value.form)
      })
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
    const {
      t,
      show,
      onConfirm,
      onCancel,
      projectCode,
      taskDefinition,
      readonly
    } = this
    return (
      <Modal
        show={show}
        title={`${t('project.node.current_node_settings')}`}
        onConfirm={onConfirm}
        confirmLoading={false}
        onCancel={onCancel}
      >
        <Detail
          ref='detailRef'
          taskType={taskDefinition.taskType}
          projectCode={projectCode}
        />
      </Modal>
    )
  }
})

export default NodeDetailModal
