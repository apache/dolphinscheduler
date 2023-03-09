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
import Modal from '@/components/modal'
import { NForm, NFormItem, NInput, NSelect } from 'naive-ui'
import { useModal } from './use-modal'
import { useI18n } from 'vue-i18n'

const WorkerGroupModal = defineComponent({
  name: 'WorkerQueueModal',
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
    const { variables, handleValidate, getListData } = useModal(props, ctx)
    const { t } = useI18n()

    const cancelModal = () => {
      if (props.statusRef === 0) {
        variables.model.name = ''
        variables.model.addrList = []
      }
      ctx.emit('cancelModal', props.showModalRef)
    }

    const confirmModal = () => {
      handleValidate(props.statusRef)
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    watch(
      () => props.showModalRef,
      () => {
        props.showModalRef && getListData()
      }
    )

    watch(
      () => props.statusRef,
      () => {
        if (props.statusRef === 0) {
          variables.model.name = ''
          variables.model.addrList = []
        } else {
          variables.model.id = props.row.id
          variables.model.name = props.row.name
          variables.model.addrList = props.row.addrList.split(',')
        }
      }
    )

    watch(
      () => props.row,
      () => {
        variables.model.id = props.row.id
        variables.model.name = props.row.name
        variables.model.addrList = props.row.addrList.split(',')
      }
    )

    return { t, ...toRefs(variables), cancelModal, confirmModal, trim }
  },
  render() {
    const { t } = this
    return (
      <div>
        <Modal
          title={
            this.statusRef === 0
              ? t('security.worker_group.create_worker_group')
              : t('security.worker_group.edit_worker_group')
          }
          show={this.showModalRef}
          onCancel={this.cancelModal}
          onConfirm={this.confirmModal}
          confirmDisabled={!this.model.name || this.model.addrList.length < 1}
          confirmClassName='btn-submit'
          cancelClassName='btn-cancel'
          confirmLoading={this.saving}
        >
          {{
            default: () => (
              <NForm
                model={this.model}
                rules={this.rules}
                ref='workerGroupFormRef'
              >
                <NFormItem
                  label={t('security.worker_group.group_name')}
                  path='name'
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-worker-group-name'
                    placeholder={t('security.worker_group.group_name_tips')}
                    v-model={[this.model.name, 'value']}
                  />
                </NFormItem>
                <NFormItem
                  label={t('security.worker_group.worker_addresses')}
                  path='addrList'
                >
                  <NSelect
                    class='select-worker-address'
                    multiple
                    placeholder={t(
                      'security.worker_group.worker_addresses_tips'
                    )}
                    options={this.model.generalOptions}
                    v-model={[this.model.addrList, 'value']}
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

export default WorkerGroupModal
