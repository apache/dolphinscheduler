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
import { NForm, NFormItem, NInput, NSelect } from 'naive-ui'
import { useModal } from './use-modal'
import { useI18n } from 'vue-i18n'

const AlarmGroupModal = defineComponent({
  name: 'AlarmGroupModal',
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
        variables.model.groupName = ''
        variables.model.alertInstanceIds = []
        variables.model.description = ''
      }
      ctx.emit('cancelModal', props.showModalRef)
    }

    const confirmModal = () => {
      handleValidate(props.statusRef)
    }

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
          variables.model.groupName = ''
          variables.model.alertInstanceIds = []
          variables.model.description = ''
        } else {
          variables.model.id = props.row.id
          variables.model.groupName = props.row.groupName
          variables.model.alertInstanceIds = props.row.alertInstanceIds
            .split(',')
            .map((item: string) => Number(item))
          variables.model.description = props.row.description
        }
      }
    )

    watch(
      () => props.row,
      () => {
        variables.model.id = props.row.id
        variables.model.groupName = props.row.groupName
        variables.model.alertInstanceIds = props.row.alertInstanceIds
          .split(',')
          .map((item: string) => Number(item))
        variables.model.description = props.row.description
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
              ? t('security.alarm_group.create_alarm_group')
              : t('security.alarm_group.edit_alarm_group')
          }
          show={this.showModalRef}
          onCancel={this.cancelModal}
          onConfirm={this.confirmModal}
          confirmDisabled={
            !this.model.groupName || this.model.alertInstanceIds.length < 1
          }
          confirmLoading={this.saving}
        >
          {{
            default: () => (
              <NForm
                model={this.model}
                rules={this.rules}
                ref='alertGroupFormRef'
              >
                <NFormItem
                  label={t('security.alarm_group.alert_group_name')}
                  path='groupName'
                >
                  <NInput
                    placeholder={t(
                      'security.alarm_group.alert_group_name_tips'
                    )}
                    v-model={[this.model.groupName, 'value']}
                  />
                </NFormItem>
                <NFormItem
                  label={t('security.alarm_group.alarm_plugin_instance')}
                  path='alertInstanceIds'
                >
                  <NSelect
                    multiple
                    placeholder={t(
                      'security.alarm_group.alarm_plugin_instance_tips'
                    )}
                    options={this.model.generalOptions}
                    v-model={[this.model.alertInstanceIds, 'value']}
                  />
                </NFormItem>
                <NFormItem
                  label={t('security.alarm_group.alarm_group_description')}
                  path='description'
                >
                  <NInput
                    type='textarea'
                    placeholder={t(
                      'security.alarm_group.alarm_group_description_tips'
                    )}
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

export default AlarmGroupModal
