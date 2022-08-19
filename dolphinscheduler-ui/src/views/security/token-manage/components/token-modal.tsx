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
import {
  NForm,
  NFormItem,
  NInput,
  NSelect,
  NDatePicker,
  NButton,
  NIcon,
  NSpace
} from 'naive-ui'
import { ReloadOutlined } from '@vicons/antd'
import { useModal } from './use-modal'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/store/user/user'
import { subDays } from 'date-fns'
import type { UserInfoRes } from '@/service/modules/users/types'

const TokenModal = defineComponent({
  name: 'TokenModal',
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
    const { variables, handleValidate, getListData, getToken } = useModal(
      props,
      ctx
    )
    const { t } = useI18n()
    const userStore = useUserStore()

    const cancelModal = () => {
      if (props.statusRef === 0) {
        variables.model.userId =
          (userStore.getUserInfo as UserInfoRes).userType === 'GENERAL_USER'
            ? (userStore.getUserInfo as UserInfoRes).id
            : null
        variables.model.expireTime = Date.now()
        variables.model.token = ''
      } else {
        variables.model.userId = props.row.userId
        variables.model.expireTime = new Date(props.row.expireTime).getTime()
        variables.model.token = props.row.token
      }
      ctx.emit('cancelModal', props.showModalRef)
    }

    const confirmModal = () => {
      handleValidate(props.statusRef)
    }

    const changeUser = () => {
      if (props.statusRef !== 0) {
        variables.model.token = ''
      }
    }

    const trim = getCurrentInstance()?.appContext.config.globalProperties.trim

    watch(
      () => props.showModalRef,
      () => {
        props.showModalRef &&
          (userStore.getUserInfo as UserInfoRes).userType !== 'GENERAL_USER' &&
          getListData()
      }
    )

    watch(
      () => props.statusRef,
      () => {
        if (props.statusRef === 0) {
          variables.model.userId =
            (userStore.getUserInfo as UserInfoRes).userType === 'GENERAL_USER'
              ? (userStore.getUserInfo as UserInfoRes).id
              : null
          variables.model.expireTime = Date.now()
          variables.model.token = ''
        } else {
          variables.model.id = props.row.id
          variables.model.userId = props.row.userId
          variables.model.expireTime = new Date(props.row.expireTime).getTime()
          variables.model.token = props.row.token
        }
      }
    )

    watch(
      () => props.row,
      () => {
        variables.model.id = props.row.id
        variables.model.userId = props.row.userId
        variables.model.expireTime = new Date(props.row.expireTime).getTime()
        variables.model.token = props.row.token
      }
    )

    return {
      t,
      ...toRefs(variables),
      cancelModal,
      confirmModal,
      getToken,
      changeUser,
      userStore,
      trim
    }
  },
  render() {
    const { t, getToken, changeUser, userStore } = this

    return (
      <div>
        <Modal
          title={
            this.statusRef === 0
              ? t('security.token.create_token')
              : t('security.token.edit_token')
          }
          show={this.showModalRef}
          onCancel={this.cancelModal}
          onConfirm={this.confirmModal}
          confirmDisabled={
            !this.model.userId || !this.model.expireTime || !this.model.token
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
                ref='alertGroupFormRef'
              >
                <NFormItem
                  label={t('security.token.expiration_time')}
                  path='expireTime'
                >
                  <NDatePicker
                    is-date-disabled={(ts: any) => ts < subDays(new Date(), 1)}
                    style={{ width: '100%' }}
                    type='datetime'
                    clearable
                    v-model={[this.model.expireTime, 'value']}
                  />
                </NFormItem>
                {(userStore.getUserInfo as UserInfoRes).userType !==
                  'GENERAL_USER' && (
                  <NFormItem label={t('security.token.user')} path='userId'>
                    <NSelect
                      class='input-username'
                      filterable
                      placeholder={t('security.token.user_tips')}
                      options={this.model.generalOptions}
                      v-model={[this.model.userId, 'value']}
                      onUpdateValue={changeUser}
                    />
                  </NFormItem>
                )}
                <NFormItem label={t('security.token.token')} path='token'>
                  <NSpace>
                    <NInput
                      allowInput={this.trim}
                      class='input-token'
                      style={{ width: '504px' }}
                      disabled
                      placeholder={t('security.token.token_tips')}
                      v-model={[this.model.token, 'value']}
                    />
                    <NButton
                      class='btn-generate-token'
                      strong
                      secondary
                      circle
                      type='info'
                      onClick={() => getToken()}
                    >
                      {{
                        icon: () => (
                          <NIcon>
                            <ReloadOutlined />
                          </NIcon>
                        )
                      }}
                    </NButton>
                  </NSpace>
                </NFormItem>
              </NForm>
            )
          }}
        </Modal>
      </div>
    )
  }
})

export default TokenModal
