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

import { defineComponent, onMounted, PropType, toRefs, watch } from 'vue'
import Modal from '@/components/modal'
import { NForm, NFormItem, NInput, NSelect } from 'naive-ui'
import { useModalData } from './use-modalData'

const TenantModal = defineComponent({
  name: 'tenant-modal',
  emits: ['cancelModal', 'confirmModal'],
  setup(props, ctx) {
    const { variables, getListData, handleValidate} = useModalData(props, ctx)

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

    onMounted(() => {
      getListData()
    })

    watch(() => props.row, () => {
      variables.model.id = props.row.id
      variables.model.tenantCode = props.row.tenantCode
      variables.model.queueId = props.row.queueId
      variables.model.description = props.row.description
    })

    return { ...toRefs(variables), cancelModal, confirmModal }
  },
  props: {
    showModalRef: {
      type: Boolean as PropType<boolean>,
      default: false,
    },
    statusRef: {
      type: Number as PropType<number>,
      default: 0,
    },
    row: {
      type: Object as PropType<any>,
      default: {},
    }
  },
  render() {
    return (
      <div>
        <Modal
          title={this.statusRef === 0 ? '创建租户' : '编辑租户'}
          show={this.showModalRef}
          onCancel={this.cancelModal}
          onConfirm={this.confirmModal}
        >
          {{
            default: () => (
              <NForm
                model={this.model}
                rules={this.rules}
                ref="tenantFormRef"
                label-placement="left"
                label-width={140}
                require-mark-placement="left"
                size="small"
                style="{ maxWidth: '240px' }"
              >
                <NFormItem label="操作系统租户" path="tenantCode">
                  <NInput disabled={this.statusRef === 1} placeholder="请输入操作系统租户" v-model={[this.model.tenantCode, 'value']} />
                </NFormItem>
                <NFormItem label="队列" path="queueId">
                  <NSelect
                    placeholder="Select"
                    options={this.model.generalOptions}
                    v-model={[this.model.queueId, 'value']}
                  />
                </NFormItem>
                <NFormItem label="描述" path="description">
                  <NInput
                    placeholder="请输入描述"
                    v-model={[this.model.description, 'value']}
                    type="textarea"
                  />
                </NFormItem>
              </NForm>
            ),
          }}
        </Modal>
      </div>
    )
  },
})

export default TenantModal
