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
import { NForm, NFormItem, NInput } from 'naive-ui'
import { useModal } from './use-modal'
import { useI18n } from 'vue-i18n'

const YarnQueueModal = defineComponent({
  name: 'YarnQueueModal',
  props: {
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
  },
  emits: ['cancelModal', 'confirmModal'],
  setup(props, ctx) {
    const { variables, handleValidate } = useModal(props, ctx)
    const { t } = useI18n()

    const cancelModal = () => {
      if (props.statusRef === 0) {
        variables.model.queue = ''
        variables.model.queueName = ''
      }
      ctx.emit('cancelModal', props.showModalRef)
    }

    const confirmModal = () => {
      handleValidate(props.statusRef)
    }

    watch(
      () => props.statusRef,
      () => {
        if (props.statusRef === 0) {
          variables.model.queue = ''
          variables.model.queueName = ''
        } else {
          variables.model.id = props.row.id
          variables.model.queue = props.row.queue
          variables.model.queueName = props.row.queueName
        }
      }
    )

    watch(
      () => props.row,
      () => {
        variables.model.id = props.row.id
        variables.model.queue = props.row.queue
        variables.model.queueName = props.row.queueName
      }
    )

    return { t, ...toRefs(variables), cancelModal, confirmModal }
  },
  render() {
    const { t } = this
    return (
      <div>
        <Modal
          title={
            this.statusRef === 0
              ? t('security.yarn_queue.create_queue')
              : t('security.yarn_queue.edit_queue')
          }
          show={this.showModalRef}
          onCancel={this.cancelModal}
          onConfirm={this.confirmModal}
          confirmDisabled={!this.model.queueName || !this.model.queue}
          confirmClassName='btn-submit'
          cancelClassName='btn-cancel'
          confirmLoading={this.saving}
        >
          {{
            default: () => (
              <NForm
                model={this.model}
                rules={this.rules}
                ref='yarnQueueFormRef'
              >
                <NFormItem
                  label={t('security.yarn_queue.queue_name')}
                  path='queueName'
                >
                  <NInput
                    class='input-queue-name'
                    placeholder={t('security.yarn_queue.queue_name_tips')}
                    v-model={[this.model.queueName, 'value']}
                  />
                </NFormItem>
                <NFormItem
                  label={t('security.yarn_queue.queue_value')}
                  path='queue'
                >
                  <NInput
                    class='input-queue-value'
                    placeholder={t('security.yarn_queue.queue_value_tips')}
                    v-model={[this.model.queue, 'value']}
                  />
                </NFormItem>
              </NForm>
            )
          }}
        </Modal>
      </div>
    )
  }
})

export default YarnQueueModal
