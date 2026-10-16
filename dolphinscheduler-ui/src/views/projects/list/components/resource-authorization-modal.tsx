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
import { NButton, NDataTable, NPopconfirm, NSelect, NSpace } from 'naive-ui'
import Modal from '@/components/modal'
import {
  projectResourceTypes,
  queryResourceAuthorizations,
  grantProjectResource,
  revokeProjectResource
} from '@/service/modules/projects/authorization'
import type {
  ProjectResourceType,
  ResourceAuthorization
} from '@/service/modules/projects/authorization'
import { queryDataSourceListPaging } from '@/service/modules/data-source'
import { queryAllWorkerGroups } from '@/service/modules/worker-groups'
import { queryTenantList } from '@/service/modules/tenants'
import { queryList } from '@/service/modules/queues'
import { queryAllEnvironmentList } from '@/service/modules/environment'
import { listAlertGroupById } from '@/service/modules/alert-group'
import { queryNamespaceListPaging } from '@/service/modules/k8s-namespace'
import { queryTaskGroupListPaging } from '@/service/modules/task-group'

export default defineComponent({
  name: 'ResourceAuthorizationModal',
  props: {
    showModalRef: Boolean,
    projectCode: { type: Number, required: true }
  },
  emits: ['cancelModal'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const resourceType = ref<ProjectResourceType>('DATASOURCE')
    const resourceKey = ref<string | null>(null)
    const options = ref<{ label: string; value: string }[]>([])
    const grants = ref<ResourceAuthorization[]>([])
    const loading = ref(false)
    const saving = ref(false)
    let requestId = 0
    const typeOptions = computed(() =>
      projectResourceTypes.map((value) => ({
        value,
        label: t(`project.authorization.types.${value}`)
      }))
    )
    const availableOptions = computed(() =>
      options.value.filter(
        (option) =>
          !grants.value.some((grant) => grant.resourceKey === option.value)
      )
    )
    const queryOptions = async (type: ProjectResourceType) => {
      const page = { pageNo: 1, pageSize: 2147483647 }
      switch (type) {
        case 'DATASOURCE':
          return (await queryDataSourceListPaging(page)).totalList.map(
            (item: { id: number; name: string }) => ({
              label: item.name,
              value: String(item.id)
            })
          )
        case 'WORKER_GROUP':
          return [
            ...new Set<string>(['default', ...(await queryAllWorkerGroups())])
          ].map((value) => ({ label: value, value }))
        case 'TENANT':
          return (await queryTenantList()).map(
            (item: { id: number; tenantCode: string }) => ({
              label: item.tenantCode,
              value: String(item.id)
            })
          )
        case 'QUEUE':
          return (await queryList()).map(
            (item: { id: number; queueName: string }) => ({
              label: item.queueName,
              value: String(item.id)
            })
          )
        case 'ENVIRONMENT':
          return (await queryAllEnvironmentList()).map(
            (item: { id: number; name: string }) => ({
              label: item.name,
              value: String(item.id)
            })
          )
        case 'ALERT_GROUP':
          return (await listAlertGroupById()).map(
            (item: { id: number; groupName: string }) => ({
              label: item.groupName,
              value: String(item.id)
            })
          )
        case 'K8S_NAMESPACE':
          return (await queryNamespaceListPaging(page)).totalList.map(
            (item: { id: number; namespace: string; clusterName: string }) => ({
              label: `${item.clusterName} / ${item.namespace}`,
              value: String(item.id)
            })
          )
        case 'TASK_GROUP':
          return (await queryTaskGroupListPaging(page)).totalList.map(
            (item: { id: number; name: string }) => ({
              label: item.name,
              value: String(item.id)
            })
          )
      }
    }
    const load = async () => {
      const currentRequest = ++requestId
      if (!props.showModalRef) return
      loading.value = true
      resourceKey.value = null
      grants.value = []
      options.value = []
      try {
        const [resources, assigned] = await Promise.all([
          queryOptions(resourceType.value),
          queryResourceAuthorizations(props.projectCode, resourceType.value)
        ])
        if (currentRequest !== requestId) return
        options.value = resources
        grants.value = assigned
      } finally {
        if (currentRequest === requestId) loading.value = false
      }
    }
    const grant = async () => {
      if (resourceKey.value === null || saving.value) return
      saving.value = true
      try {
        await grantProjectResource(
          props.projectCode,
          resourceType.value,
          resourceKey.value
        )
        grants.value = await queryResourceAuthorizations(
          props.projectCode,
          resourceType.value
        )
        resourceKey.value = null
      } finally {
        saving.value = false
      }
    }
    const revoke = async (key: string) => {
      saving.value = true
      try {
        await revokeProjectResource(props.projectCode, resourceType.value, key)
        grants.value = await queryResourceAuthorizations(
          props.projectCode,
          resourceType.value
        )
      } finally {
        saving.value = false
      }
    }
    watch(
      () => [props.showModalRef, props.projectCode, resourceType.value],
      load
    )
    const columns = computed(() => [
      {
        title: t('project.authorization.resource'),
        key: 'resourceKey',
        render: (row: ResourceAuthorization) =>
          options.value.find((option) => option.value === row.resourceKey)
            ?.label || row.resourceKey
      },
      {
        title: t('project.list.operation'),
        key: 'operation',
        render: (row: ResourceAuthorization) => (
          <NPopconfirm
            onPositiveClick={() => revoke(row.resourceKey)}
            disabled={saving.value}
          >
            {{
              trigger: () => (
                <NButton size='small' disabled={saving.value}>
                  {t('project.authorization.revoke')}
                </NButton>
              ),
              default: () => t('project.authorization.revoke_confirm')
            }}
          </NPopconfirm>
        )
      }
    ])
    return () => (
      <Modal
        show={props.showModalRef}
        title={t('project.authorization.resources')}
        confirmShow={false}
        onCancel={() => {
          if (!saving.value) emit('cancelModal')
        }}
      >
        <NSpace vertical>
          <NSelect
            value={resourceType.value}
            onUpdateValue={(value) => {
              resourceType.value = value
            }}
            options={typeOptions.value}
            disabled={loading.value || saving.value}
          />
          <NSelect
            v-model:value={resourceKey.value}
            options={availableOptions.value}
            filterable
            clearable
            loading={loading.value}
            disabled={loading.value || saving.value}
            placeholder={t('project.authorization.select_resource')}
          />
          <NButton
            type='primary'
            onClick={grant}
            loading={saving.value}
            disabled={
              loading.value || saving.value || resourceKey.value === null
            }
          >
            {t('project.authorization.grant')}
          </NButton>
          <NDataTable
            loading={loading.value}
            columns={columns.value}
            data={grants.value}
            maxHeight={360}
            rowKey={(row) => row.resourceKey}
          />
        </NSpace>
      </Modal>
    )
  }
})
