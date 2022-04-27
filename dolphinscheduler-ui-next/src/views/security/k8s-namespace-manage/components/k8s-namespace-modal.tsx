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
import {
  NForm,
  NFormItem,
  NInput,
  NInputGroup,
  NInputGroupLabel
} from 'naive-ui'
import { useModal } from './use-modal'
import { useI18n } from 'vue-i18n'

const K8sNamespaceModal = defineComponent({
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
        variables.model.namespace = ''
        variables.model.k8s = ''
        variables.model.tag = ''
        variables.model.limitsCpu = ''
        variables.model.limitsMemory = ''
        variables.model.owner = ''
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
          variables.model.namespace = ''
          variables.model.k8s = ''
          variables.model.tag = ''
          variables.model.limitsCpu = ''
          variables.model.limitsMemory = ''
          variables.model.owner = ''
        } else {
          variables.model.id = props.row.id
          variables.model.namespace = props.row.namespace
          variables.model.k8s = props.row.k8s
          variables.model.tag = props.row.tag
          variables.model.limitsCpu = props.row.limitsCpu + ''
          variables.model.limitsMemory = props.row.limitsMemory + ''
          variables.model.owner = props.row.owner
        }
      }
    )

    watch(
      () => props.row,
      () => {
        variables.model.id = props.row.id
        variables.model.namespace = props.row.namespace
        variables.model.k8s = props.row.k8s
        variables.model.tag = props.row.tag
        variables.model.limitsCpu = props.row.limitsCpu + ''
        variables.model.limitsMemory = props.row.limitsMemory + ''
        variables.model.owner = props.row.owner
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
              ? t('security.k8s_namespace.create_namespace')
              : t('security.k8s_namespace.edit_namespace')
          }
          show={this.showModalRef}
          onCancel={this.cancelModal}
          onConfirm={this.confirmModal}
          confirmDisabled={!this.model.namespace || !this.model.k8s}
          confirmLoading={this.saving}
        >
          {{
            default: () => (
              <NForm
                model={this.model}
                rules={this.rules}
                ref='k8sNamespaceFormRef'
              >
                <NFormItem
                  label={t('security.k8s_namespace.k8s_namespace')}
                  path='namespace'
                >
                  <NInput
                    placeholder={t('security.k8s_namespace.k8s_namespace_tips')}
                    v-model={[this.model.namespace, 'value']}
                  />
                </NFormItem>
                <NFormItem
                  label={t('security.k8s_namespace.k8s_cluster')}
                  path='k8s'
                >
                  <NInput
                    placeholder={t('security.k8s_namespace.k8s_cluster_tips')}
                    v-model={[this.model.k8s, 'value']}
                  />
                </NFormItem>
                <NFormItem label={t('security.k8s_namespace.tag')} path='tag'>
                  <NInput
                    placeholder={t('security.k8s_namespace.tag_tips')}
                    v-model={[this.model.tag, 'value']}
                  />
                </NFormItem>
                <NFormItem
                  label={t('security.k8s_namespace.limit_cpu')}
                  path='limitsCpu'
                >
                  <NInputGroup>
                    <NInput
                      placeholder={t('security.k8s_namespace.limit_cpu_tips')}
                      v-model={[this.model.limitsCpu, 'value']}
                    />
                    <NInputGroupLabel>CORE</NInputGroupLabel>
                  </NInputGroup>
                </NFormItem>
                <NFormItem
                  label={t('security.k8s_namespace.limit_memory')}
                  path='limitsMemory'
                >
                  <NInputGroup>
                    <NInput
                      placeholder={t(
                        'security.k8s_namespace.limit_memory_tips'
                      )}
                      v-model={[this.model.limitsMemory, 'value']}
                    />
                    <NInputGroupLabel>GB</NInputGroupLabel>
                  </NInputGroup>
                </NFormItem>
                <NFormItem
                  label={t('security.k8s_namespace.owner')}
                  path='owner'
                >
                  <NInput
                    placeholder={t('security.k8s_namespace.owner_tips')}
                    v-model={[this.model.owner, 'value']}
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

export default K8sNamespaceModal
