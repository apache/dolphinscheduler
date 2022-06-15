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

const envK8sConfigPlaceholder = `apiVersion: v1
clusters:
- cluster:
    certificate-authority-data: LS0tLS1CZJQ0FURS0tLS0tCg==
    server: https://127.0.0.1:6443
  name: kubernetes
contexts:
- context:
    cluster: kubernetes
    user: kubernetes-admin
  name: kubernetes-admin@kubernetes
current-context: kubernetes-admin@kubernetes
kind: Config
preferences: {}
users:
- name: kubernetes-admin
  user:
    client-certificate-data: LS0tLS1CZJQ0FURS0tLS0tCg= 
`

const envYarnConfigPlaceholder = 'In development...'

const ClusterModal = defineComponent({
  name: 'ClusterModal',
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
        variables.model.name = ''
        variables.model.k8s_config = ''
        variables.model.yarn_config = ''
        variables.model.description = ''
      }
      ctx.emit('cancelModal', props.showModalRef)
    }

    const confirmModal = () => {
      handleValidate(props.statusRef)
    }

    const setModal = (row: any) => {
      variables.model.code = row.code
      variables.model.name = row.name
      if (row.config) {
        const config = JSON.parse(row.config)
        variables.model.k8s_config = config.k8s || ''
        variables.model.yarn_config = config.yarn || ''
      } else {
        variables.model.k8s_config = ''
        variables.model.yarn_config = ''
      }
      variables.model.description = row.description
    }

    watch(
      () => props.statusRef,
      () => {
        if (props.statusRef === 0) {
          variables.model.name = ''
          variables.model.k8s_config = ''
          variables.model.yarn_config = ''
          variables.model.description = ''
        } else {
          setModal(props.row)
        }
      }
    )

    watch(
      () => props.row,
      () => {
        setModal(props.row)
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
              ? t('security.cluster.create_cluster')
              : t('security.cluster.edit_cluster')
          }
          show={this.showModalRef}
          onCancel={this.cancelModal}
          onConfirm={this.confirmModal}
          confirmDisabled={!this.model.name || !this.model.description}
          confirmClassName='btn-submit'
          cancelClassName='btn-cancel'
          confirmLoading={this.saving}
        >
          {{
            default: () => (
              <NForm model={this.model} rules={this.rules} ref='clusterFormRef'>
                <NFormItem
                  label={t('security.cluster.cluster_name')}
                  path='name'
                >
                  <NInput
                    class='input-cluster-name'
                    placeholder={t('security.cluster.cluster_name_tips')}
                    v-model={[this.model.name, 'value']}
                  />
                </NFormItem>
                <NFormItem
                  label={t('security.cluster.kubernetes_config')}
                  path='k8s_config'
                >
                  <NInput
                    class='input-cluster-config'
                    placeholder={envK8sConfigPlaceholder}
                    type='textarea'
                    autosize={{ minRows: 16 }}
                    v-model={[this.model.k8s_config, 'value']}
                  />
                </NFormItem>
                <NFormItem
                  label={t('security.cluster.yarn_config')}
                  path='yarn_config'
                >
                  <NInput
                    class='input-yarn-config'
                    placeholder={envYarnConfigPlaceholder}
                    disabled={true}
                    v-model={[this.model.yarn_config, 'value']}
                  />
                </NFormItem>
                <NFormItem
                  label={t('security.cluster.cluster_desc')}
                  path='description'
                >
                  <NInput
                    class='input-cluster-desc'
                    placeholder={t('security.cluster.cluster_description_tips')}
                    v-model={[this.model.description, 'value']}
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

export default ClusterModal
