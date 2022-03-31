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

import { defineComponent, onMounted, ref, toRefs } from 'vue'
import { useForm } from './use-form'
import { NButton, NForm, NFormItem, NInput } from 'naive-ui'
import { useUserinfo } from './use-userinfo'
import { useUpdate } from './use-update'
import Card from '@/components/card'
import Modal from '@/components/modal'
import Info from './components/info'
import utils from '@/utils'
import { useUserStore } from '@/store/user/user'
import type { UserInfoRes } from '@/service/modules/users/types'

const profile = defineComponent({
  name: 'profile',
  setup() {
    const userInfo = useUserStore().userInfo as UserInfoRes
    const showModalRef = ref(false)
    const { state, t } = useForm()
    const { handleUpdate } = useUpdate(state)
    const { getUserInfo } = useUserinfo()

    onMounted(async () => {
      await getUserInfo()
    })

    const onCancel = () => {
      showModalRef.value = false
      state.profileForm.email = userInfo.email
      state.profileForm.phone = userInfo.phone
      state.profileForm.username = userInfo.userName
    }

    const onConfirm = async () => {
      showModalRef.value = false
      await handleUpdate()
      await getUserInfo()
    }

    return { ...toRefs(state), showModalRef, t, onCancel, onConfirm }
  },
  render() {
    const { t, onCancel, onConfirm } = this

    return (
      <div>
        <Card title={t('profile.profile')}>
          {{
            default: () => <Info />,
            'header-extra': () => (
              <NButton
                type='info'
                size='small'
                onClick={() => (this.showModalRef = !this.showModalRef)}
              >
                {t('profile.edit')}
              </NButton>
            )
          }}
        </Card>
        <Modal
          title={t('profile.edit_profile')}
          show={this.showModalRef}
          onCancel={onCancel}
          onConfirm={onConfirm}
          confirmDisabled={
            !this.profileForm.username ||
            !this.profileForm.email ||
            !utils.regex.email.test(this.profileForm.email)
          }
          confirmLoading={this.saving}
        >
          {{
            default: () => (
              <NForm rules={this.rules} ref='profileFormRef'>
                <NFormItem label={t('profile.username')} path='username'>
                  <NInput
                    v-model={[this.profileForm.username, 'value']}
                    placeholder={t('profile.username_tips')}
                  />
                </NFormItem>
                <NFormItem label={t('profile.email')} path='email'>
                  <NInput
                    v-model={[this.profileForm.email, 'value']}
                    placeholder={t('profile.email_tips')}
                  />
                </NFormItem>
                <NFormItem label={t('profile.phone')} path='phone'>
                  <NInput
                    v-model={[this.profileForm.phone, 'value']}
                    placeholder={t('profile.phone_tips')}
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

export default profile
