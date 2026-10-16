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

import { computed, defineComponent, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  NButton,
  NCheckbox,
  NCheckboxGroup,
  NSelect,
  NSpace,
  NText
} from 'naive-ui'
import Modal from '@/components/modal'
import {
  projectPermissions,
  queryMemberCandidates,
  queryMemberPermissions,
  replaceMemberPermissions
} from '@/service/modules/projects/authorization'
import type { ProjectPermission } from '@/service/modules/projects/authorization'

export default defineComponent({
  name: 'MemberAuthorizationModal',
  props: {
    showModalRef: Boolean,
    projectCode: { type: Number, required: true }
  },
  emits: ['cancelModal', 'updated'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const users = ref<{ label: string; value: number }[]>([])
    const userId = ref<number | null>(null)
    const permissions = ref<ProjectPermission[]>([])
    const loading = ref(false)
    const saving = ref(false)
    let candidatesRequestId = 0
    let permissionsRequestId = 0
    const permissionOptions = computed(() =>
      projectPermissions.map((value) => ({
        value,
        label: t(`project.authorization.permissions.${value}`)
      }))
    )
    watch(
      () => [props.showModalRef, props.projectCode],
      async () => {
        const currentRequest = ++candidatesRequestId
        ++permissionsRequestId
        if (!props.showModalRef) return
        userId.value = null
        permissions.value = []
        users.value = []
        loading.value = true
        try {
          const candidates = await queryMemberCandidates(props.projectCode)
          if (currentRequest !== candidatesRequestId) return
          users.value = candidates.map((user) => ({
            label: user.userName,
            value: user.id
          }))
        } finally {
          if (currentRequest === candidatesRequestId) loading.value = false
        }
      }
    )
    watch(userId, async (value) => {
      const currentRequest = ++permissionsRequestId
      permissions.value = []
      if (value === null) return
      loading.value = true
      try {
        const assigned = await queryMemberPermissions(props.projectCode, value)
        if (currentRequest === permissionsRequestId)
          permissions.value = assigned
      } finally {
        if (currentRequest === permissionsRequestId) loading.value = false
      }
    })
    const save = async (assigned: ProjectPermission[]) => {
      if (userId.value === null || loading.value || saving.value) return
      saving.value = true
      try {
        await replaceMemberPermissions(
          props.projectCode,
          userId.value,
          assigned
        )
        permissions.value = assigned
        emit('updated')
        emit('cancelModal')
      } finally {
        saving.value = false
      }
    }
    return () => (
      <Modal
        show={props.showModalRef}
        title={t('project.authorization.members')}
        confirmShow={false}
        onCancel={() => {
          if (!saving.value) emit('cancelModal')
        }}
      >
        <NSpace vertical>
          <NSelect
            v-model:value={userId.value}
            options={users.value}
            filterable
            placeholder={t('project.authorization.select_member')}
            loading={loading.value}
            disabled={saving.value || loading.value}
          />
          <NCheckboxGroup
            v-model:value={permissions.value}
            disabled={userId.value === null || saving.value || loading.value}
          >
            <NSpace vertical>
              {permissionOptions.value.map((option) => (
                <NCheckbox
                  key={option.value}
                  value={option.value}
                  label={option.label}
                />
              ))}
            </NSpace>
          </NCheckboxGroup>
          <NText depth={3}>
            {t('project.authorization.explicit_permissions')}
          </NText>
          <NSpace>
            <NButton
              type='primary'
              loading={saving.value}
              disabled={userId.value === null || loading.value || saving.value}
              onClick={() => save([...permissions.value])}
            >
              {t('project.authorization.save_permissions')}
            </NButton>
            <NButton
              disabled={userId.value === null || loading.value || saving.value}
              onClick={() => save([])}
            >
              {t('project.authorization.revoke_member')}
            </NButton>
          </NSpace>
        </NSpace>
      </Modal>
    )
  }
})
