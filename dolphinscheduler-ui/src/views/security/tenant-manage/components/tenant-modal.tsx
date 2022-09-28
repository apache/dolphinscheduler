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
import { useModalData } from './use-modalData'
import { useI18n } from 'vue-i18n'

const TenantModal = defineComponent({
  name: 'tenant-modal',
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
    const { variables, getListData, handleValidate } = useModalData(props, ctx)
    const { t } = useI18n()

    const cancelModal = () => {
      if (props.statusRef === 0) {
        variables.model.tenantCode = ''
        variables.model.description = ''
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
        props.showModalRef && getListData(props.statusRef)
      }
    )

    watch(
      () => props.statusRef,
      () => {
        if (props.statusRef === 0) {
          variables.model.tenantCode = ''
          variables.model.description = ''
          variables.model.queueId = null
        } else {
          variables.model.id = props.row.id
          variables.model.tenantCode = props.row.tenantCode
          variables.model.queueId = props.row.queueId
          variables.model.description = props.row.description
        }
      }
    )

    watch(
      () => props.row,
      () => {
        variables.model.id = props.row.id
        variables.model.tenantCode = props.row.tenantCode
        variables.model.queueId = props.row.queueId
        variables.model.description = props.row.description
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
              ? t('security.tenant.create_tenant')
              : t('security.tenant.edit_tenant')
          }
          show={this.showModalRef}
          onCancel={this.cancelModal}
          onConfirm={this.confirmModal}
          confirmClassName='btn-submit'
          cancelClassName='btn-cancel'
          confirmLoading={this.saving}
        >
          {{
            default: () => (
              <NForm
                model={this.model}
                rules={this.rules}
                ref='tenantFormRef'
                require-mark-placement='left'
                size='small'
                style="{ maxWidth: '240px' }"
              >
                <NFormItem
                  label={t('security.tenant.tenant_code')}
                  path='tenantCode'
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-tenant-code'
                    disabled={this.statusRef === 1}
                    placeholder={t('security.tenant.tenant_code_tips')}
                    v-model={[this.model.tenantCode, 'value']}
                  />
                </NFormItem>
                <NFormItem
                  label={t('security.tenant.queue_name')}
                  path='queueId'
                >
                  <NSelect
                    class='select-queue'
                    placeholder={t('security.tenant.queue_name_tips')}
                    options={this.model.generalOptions}
                    v-model={[this.model.queueId, 'value']}
                  />
                </NFormItem>
                <NFormItem
                  label={t('security.tenant.description')}
                  path='description'
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-description'
                    placeholder={t('security.tenant.description_tips')}
                    v-model={[this.model.description, 'value']}
                    type='textarea'
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

export default TenantModal
