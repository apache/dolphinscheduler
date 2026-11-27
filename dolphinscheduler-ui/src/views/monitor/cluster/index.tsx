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

import { computed, defineComponent, onBeforeUnmount, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { NAlert, NSpin } from 'naive-ui'
import { useUserStore } from '@/store/user/user'
import type { UserInfoRes } from '@/service/modules/users/types'
import {
  listMonitorServerNode,
  queryTaskExecutors,
  queryWorkflowExecutors
} from '@/service/modules/monitor'
import type {
  ServerNode,
  ServerNodeType
} from '@/service/modules/monitor/types'
import ClusterOverview from './cluster-overview'
import type { ClusterNodeInfo } from './cluster-overview'
import type { QueryRuntimeInstances } from './running-instances-modal'
import { toClusterNodes } from './cluster-data'

export default defineComponent({
  name: 'ClusterMonitor',
  setup() {
    const { t, locale } = useI18n()
    const user = useUserStore()
    const nodes = ref<ClusterNodeInfo[]>([])
    const loading = ref(false)
    const failed = ref<string[]>([])
    const refreshedAt = ref('')
    const types: ServerNodeType[] = ['API', 'MASTER', 'WORKER', 'ALERT_SERVER']
    const isAdmin = computed(
      () => (user.getUserInfo as UserInfoRes).userType === 'ADMIN_USER'
    )
    let requestId = 0

    const refresh = async () => {
      const request = ++requestId
      loading.value = true
      const results = await Promise.allSettled(
        types.map((type) => listMonitorServerNode(type))
      )
      if (request !== requestId) return
      const errors: string[] = []
      nodes.value = results.flatMap((result, index) => {
        const type = types[index]
        if (result.status === 'fulfilled')
          return toClusterNodes(type, result.value as ServerNode[])
        errors.push(type)
        return nodes.value
          .filter(
            (node) => node.role === (type === 'ALERT_SERVER' ? 'ALERT' : type)
          )
          .map((node) => ({
            ...node,
            status: 'unknown' as const,
            cpu: null,
            memory: null,
            threads: undefined,
            disk: undefined,
            haRole: 'UNKNOWN' as const,
            coordinator: false,
            lastHeartbeatTime: null
          }))
      })
      failed.value = errors
      refreshedAt.value = new Date().toLocaleString(
        locale.value.replace('_', '-')
      )
      loading.value = false
    }
    const queryInstances: QueryRuntimeInstances = ({ node, kind }) => {
      if (!isAdmin.value)
        return Promise.reject(new Error(t('monitor.cluster.no_permission')))
      return kind === 'workflows'
        ? queryWorkflowExecutors(node.address)
        : queryTaskExecutors(node.address)
    }
    onMounted(refresh)
    onBeforeUnmount(() => {
      requestId++
    })
    return () => (
      <NSpin show={loading.value}>
        {failed.value.length > 0 && (
          <NAlert type='warning' style={{ marginBottom: '16px' }}>
            {t('monitor.cluster.load_error', {
              roles: failed.value.join(', ')
            })}
          </NAlert>
        )}
        <ClusterOverview
          nodes={nodes.value}
          snapshotTime={
            refreshedAt.value
              ? `${t('monitor.cluster.refreshed_at')} ${refreshedAt.value}`
              : ''
          }
          canQueryExecutors={isAdmin.value}
          queryInstances={queryInstances}
          onRefresh={refresh}
        />
      </NSpin>
    )
  }
})
