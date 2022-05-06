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

import { defineComponent, PropType, toRefs, watch } from 'vue'
import Modal from '@/components/modal'
import { NForm, NFormItem, NSelect } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { useMove } from './use-move'

const props = {
  show: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  row: {
    type: Object as PropType<any>,
    default: {}
  }
}

const MoveModal = defineComponent({
  name: 'MoveModal',
  props,
  emits: ['refresh', 'cancel'],
  setup(props, ctx) {
    const { t } = useI18n()
    const { variables, handleValidate, getListData } = useMove()

    const cancelModal = () => {
      variables.model.targetProcessDefinitionCode = ''
      ctx.emit('cancel')
    }

    const confirmModal = () => {
      handleValidate()
    }

    watch(
      () => props.show,
      () => {
        variables.taskCode = props.row.taskCode
        variables.processDefinitionCode = props.row.processDefinitionCode
        variables.model.targetProcessDefinitionCode =
          props.row.processDefinitionCode

        props.show && getListData()
      }
    )

    watch(
      () => variables.refreshTaskDefinition,
      () => {
        if (variables.refreshTaskDefinition) {
          ctx.emit('refresh')
          variables.refreshTaskDefinition = false
        }
      }
    )

    return { t, ...toRefs(variables), cancelModal, confirmModal }
  },
  render() {
    const { t, show, cancelModal, confirmModal } = this

    return (
      <Modal
        title={t('project.task.move')}
        show={show}
        onCancel={cancelModal}
        onConfirm={confirmModal}
        confirmDisabled={!this.model.targetProcessDefinitionCode}
        confirmLoading={this.saving}
      >
        <NForm
          model={this.model}
          rules={this.rules}
          ref='taskDefinitionFormRef'
        >
          <NFormItem
            label={t('project.task.workflow_name')}
            path='alertInstanceIds'
          >
            <NSelect
              placeholder={t('project.task.workflow_name_tips')}
              options={this.model.generalOptions}
              v-model={[this.model.targetProcessDefinitionCode, 'value']}
            />
          </NFormItem>
        </NForm>
      </Modal>
    )
  }
})

export default MoveModal
