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
import { NForm, NFormItem, NInput, NInputNumber, NButton } from 'naive-ui'
import { useModal } from './use-modal'
import { useI18n } from 'vue-i18n'
import { testConnectHost } from '@/service/modules/task-remote-host'

const TaskRemoteHostModal = defineComponent({
  name: 'TaskRemoteHostModal',
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
        variables.model.ip = ''
        variables.model.port = 22
        variables.model.account = ''
        variables.model.password = ''
        variables.model.description = ''
      }
      ctx.emit('cancelModal', props.showModalRef)
    }

    const onTest = async () => {
      const data = {
        name: variables.model.name,
        ip: variables.model.ip,
        port: variables.model.port,
        account: variables.model.account,
        password: variables.model.password,
        description: variables.model.description
      }
      const res = await testConnectHost(data)
      window.$message.success(
        res != 'SUCCESS'
          ? res.msg
          : `${t('security.task_remote_host.test_connect')} ${t(
              'security.task_remote_host.success'
            )}`
      )
    }

    const confirmModal = () => {
      handleValidate(props.statusRef)
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    watch(
      () => props.showModalRef,
      () => {
        props.showModalRef
      }
    )

    watch(
      () => props.statusRef,
      () => {
        if (props.statusRef === 0) {
          variables.model.name = ''
          variables.model.ip = ''
          variables.model.port = 22
          variables.model.account = ''
          variables.model.password = ''
          variables.model.description = ''
        } else {
          variables.model.code = props.row.code
          variables.model.name = props.row.name
          variables.model.ip = props.row.ip
          variables.model.port = props.row.port
          variables.model.account = props.row.account
          variables.model.password = props.row.password
          variables.model.description = props.row.description
        }
      }
    )

    watch(
      () => props.row,
      () => {
        variables.model.code = props.row.code
        variables.model.name = props.row.name
        variables.model.ip = props.row.ip
        variables.model.port = props.row.port
        variables.model.account = props.row.account
        variables.model.password = props.row.password
        variables.model.description = props.row.description
      }
    )

    return { t, ...toRefs(variables), cancelModal, confirmModal, trim, onTest }
  },
  render() {
    const { t, onTest } = this
    return (
      <div>
        <Modal
          title={
            this.statusRef === 0
              ? t('security.task_remote_host.create_task_remote_host')
              : t('security.task_remote_host.edit_task_remote_host')
          }
          show={this.showModalRef}
          onCancel={this.cancelModal}
          onConfirm={this.confirmModal}
          confirmDisabled={
            !this.model.name ||
            !this.model.ip ||
            !this.model.port ||
            !this.model.account ||
            !this.model.password ||
            !this.model.description
          }
          confirmClassName='btn-submit'
          cancelClassName='btn-cancel'
          confirmLoading={this.saving}
        >
          {{
            default: () => (
              <NForm
                model={this.model}
                rules={this.rules}
                ref='taskRemoteHostFormRef'
              >
                <NFormItem
                  label={t('security.task_remote_host.task_remote_host_name')}
                  path='name'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-task-remote-host-name'
                    placeholder={t(
                      'security.task_remote_host.task_remote_host_name_tips'
                    )}
                    v-model={[this.model.name, 'value']}
                  />
                </NFormItem>
                <NFormItem
                  label={t('security.task_remote_host.task_remote_host_ip')}
                  path='ip'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-task-remote-host-name'
                    placeholder={t(
                      'security.task_remote_host.task_remote_host_ip_tips'
                    )}
                    v-model={[this.model.ip, 'value']}
                  />
                </NFormItem>
                <NFormItem
                  label={t('security.task_remote_host.task_remote_host_port')}
                  path='port'
                  show-require-mark
                >
                  <NInputNumber
                    class='input-task-remote-host-port'
                    v-model={[this.model.port, 'value']}
                    show-button={false}
                    placeholder={t(
                      'security.task_remote_host.task_remote_host_port_tips'
                    )}
                    style={{ width: '100%' }}
                  />
                </NFormItem>
                <NFormItem
                  label={t(
                    'security.task_remote_host.task_remote_host_account'
                  )}
                  path='account'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-task-remote-host-account'
                    placeholder={t(
                      'security.task_remote_host.task_remote_host_account_tips'
                    )}
                    v-model={[this.model.account, 'value']}
                  />
                </NFormItem>
                <NFormItem
                  label={t(
                    'security.task_remote_host.task_remote_host_password'
                  )}
                  path='password'
                  show-require-mark
                >
                  <NInput
                    allowInput={this.trim}
                    type='password'
                    class='input-task-remote-host-password'
                    placeholder={t(
                      'security.task_remote_host.task_remote_host_password_tips'
                    )}
                    v-model={[this.model.password, 'value']}
                  />
                </NFormItem>
                <NFormItem
                  label={t('security.task_remote_host.task_remote_host_desc')}
                  path='description'
                >
                  <NInput
                    allowInput={this.trim}
                    class='input-environment-desc'
                    placeholder={t(
                      'security.task_remote_host.task_remote_host_description_tips'
                    )}
                    v-model={[this.model.description, 'value']}
                  />
                </NFormItem>
              </NForm>
            ),
            'btn-middle': () => (
              <NButton
                class='btn-test-connection'
                type='primary'
                size='small'
                onClick={onTest}
              >
                {t('datasource.test_connect')}
              </NButton>
            )
          }}
        </Modal>
      </div>
    )
  }
})

export default TaskRemoteHostModal
