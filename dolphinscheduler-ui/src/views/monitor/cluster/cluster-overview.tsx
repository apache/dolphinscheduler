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

import { useI18n } from 'vue-i18n'
import {
  computed,
  defineComponent,
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref,
  watch,
  type PropType
} from 'vue'
import {
  NEmpty,
  NButton,
  NDataTable,
  NDescriptions,
  NDescriptionsItem,
  NDrawer,
  NDrawerContent,
  NIcon,
  NProgress,
  NTabPane,
  NTabs,
  NTag,
  NTooltip,
  useThemeVars
} from 'naive-ui'
import {
  BellTwotone,
  CheckCircleFilled,
  DeploymentUnitOutlined,
  DownOutlined,
  GlobalOutlined,
  HddTwotone,
  InfoCircleOutlined,
  ReloadOutlined
} from '@vicons/antd'
import styles from './index.module.scss'
import RunningInstancesModal from './running-instances-modal'
import type {
  QueryRuntimeInstances,
  RuntimeTarget
} from './running-instances-modal'

export interface ClusterNodeInfo {
  id: string
  name: string
  lastHeartbeatTime?: string | null
  createTime?: string
  directory?: string
  processId?: number
  rpcAddress?: string
  role: 'API' | 'MASTER' | 'WORKER' | 'ALERT'
  address: string
  status: 'normal' | 'busy' | 'unknown'
  haRole?: 'ACTIVE' | 'STANDBY' | 'UNKNOWN'
  cpu: number | null
  memory: number | null
  group?: string
  coordinator?: boolean
  threads?: number
  disk?: number
}

const roleNames = {
  API: 'API Server',
  MASTER: 'Master',
  WORKER: 'Worker',
  ALERT: 'Alert Server'
}
const roleIcons = {
  API: HddTwotone,
  MASTER: HddTwotone,
  WORKER: HddTwotone,
  ALERT: BellTwotone
}

export default defineComponent({
  name: 'ClusterOverview',
  props: {
    nodes: { type: Array as PropType<ClusterNodeInfo[]>, required: true },
    snapshotTime: { type: String, default: '' },
    canQueryExecutors: { type: Boolean, default: false },
    queryInstances: {
      type: Function as PropType<QueryRuntimeInstances>,
      default: undefined
    }
  },
  emits: ['refresh'],
  setup(props, { emit }) {
    const { t } = useI18n()
    const stateNames = computed(() => ({
      normal: t('monitor.cluster.normal'),
      busy: t('monitor.cluster.busy'),
      unknown: t('monitor.cluster.unknown')
    }))
    const haRoleNames = computed(() => ({
      ACTIVE: 'Active',
      STANDBY: 'Standby',
      UNKNOWN: t('monitor.cluster.role_unknown')
    }))
    const theme = useThemeVars()
    const canvas = ref<HTMLDivElement>()
    const paths = ref<{ d: string; arrow?: boolean }[]>([])
    const expanded = ref<string[]>([])
    const selected = ref<ClusterNodeInfo | null>(null)
    const activeTab = ref('topology')
    const runtimeTarget = ref<RuntimeTarget | null>(null)
    let observer: ResizeObserver | undefined
    let animationFrame = 0

    const workers = computed(() => {
      const groups = new Map<string, ClusterNodeInfo[]>()
      props.nodes
        .filter((node) => node.role === 'WORKER')
        .forEach((node) => {
          const group = node.group || t('monitor.cluster.group_unknown')
          groups.set(group, [...(groups.get(group) || []), node])
        })
      return Array.from(groups, ([name, nodes]) => ({ name, nodes }))
    })
    const normal = computed(
      () => props.nodes.filter((node) => node.status === 'normal').length
    )
    const busy = computed(
      () => props.nodes.filter((node) => node.status === 'busy').length
    )
    const unknown = computed(
      () => props.nodes.filter((node) => node.status === 'unknown').length
    )

    const drawConnections = () => {
      const element = canvas.value
      if (!element || activeTab.value !== 'topology') return
      const bounds = element.getBoundingClientRect()
      if (bounds.width < 640) {
        paths.value = []
        return
      }
      const connections: { d: string; arrow?: boolean }[] = []
      const connect = (source: string, target: string) => {
        const points = (selector: string, bottom: boolean) =>
          Array.from(element.querySelectorAll<HTMLElement>(selector)).map(
            (node) => {
              const rect = node.getBoundingClientRect()
              return {
                x: rect.left - bounds.left + rect.width / 2,
                y: (bottom ? rect.bottom + 8 : rect.top - 12) - bounds.top
              }
            }
          )
        const sources = points(source, true)
        const targets = points(target, false)
        if (!sources.length || !targets.length) return
        const sourceY = Math.max(...sources.map((point) => point.y))
        const targetY = Math.min(...targets.map((point) => point.y))
        if (targetY <= sourceY) return
        const xs = [...sources, ...targets].map((point) => point.x)
        const hubY = (sourceY + targetY) / 2
        sources.forEach((point) => {
          connections.push({
            d: `M ${point.x} ${point.y} V ${hubY}`
          })
        })
        connections.push({
          d: `M ${Math.min(...xs)} ${hubY} H ${Math.max(...xs)}`
        })
        targets.forEach((point) => {
          connections.push({
            d: `M ${point.x} ${hubY} V ${point.y}`,
            arrow: true
          })
        })
      }
      connect(
        '[data-tier="API"] [data-flow-nodes] [data-node]',
        '[data-tier="MASTER"] [data-flow-nodes] [data-node] .cluster-node-icon'
      )
      connect(
        '[data-tier="MASTER"] [data-flow-nodes] [data-node]',
        '[data-worker-group] .cluster-group-icon'
      )
      paths.value = connections
    }
    const scheduleConnections = () => {
      cancelAnimationFrame(animationFrame)
      animationFrame = requestAnimationFrame(drawConnections)
    }
    const toggleGroup = async (name: string) => {
      expanded.value = expanded.value.includes(name)
        ? expanded.value.filter((group) => group !== name)
        : [...expanded.value, name]
      await nextTick()
      scheduleConnections()
    }
    onMounted(() => {
      observer = new ResizeObserver(scheduleConnections)
      if (canvas.value) observer.observe(canvas.value)
      scheduleConnections()
    })
    onBeforeUnmount(() => {
      observer?.disconnect()
      cancelAnimationFrame(animationFrame)
    })
    watch([() => props.nodes, activeTab], async () => {
      const id = selected.value?.id
      if (id)
        selected.value = props.nodes.find((node) => node.id === id) || null
      if (
        runtimeTarget.value &&
        !props.nodes.some((node) => node.id === runtimeTarget.value?.node.id)
      )
        runtimeTarget.value = null
      await nextTick()
      scheduleConnections()
    })

    const renderHaRole = (node: ClusterNodeInfo) => (
      <NTag
        size='small'
        round
        bordered={false}
        type={node.haRole === 'ACTIVE' ? 'success' : 'default'}
        class={styles.haRole}
        data-ha-role={node.haRole || 'UNKNOWN'}
      >
        {haRoleNames.value[node.haRole || 'UNKNOWN']}
      </NTag>
    )

    const renderRuntimeActions = (node: ClusterNodeInfo) =>
      props.canQueryExecutors &&
      props.queryInstances &&
      (node.role === 'MASTER' || node.role === 'WORKER') ? (
        <span class={styles.runtimeActions}>
          {node.role === 'MASTER' && (
            <NButton
              text
              type='primary'
              onClick={() =>
                (runtimeTarget.value = { node, kind: 'workflows' })
              }
            >
              {t('monitor.cluster.running_workflows')}
            </NButton>
          )}
          <NButton
            text
            type='primary'
            onClick={() => (runtimeTarget.value = { node, kind: 'tasks' })}
          >
            {t('monitor.cluster.running_tasks')}
          </NButton>
        </span>
      ) : null

    const renderNode = (node: ClusterNodeInfo, small = false) => {
      const Icon = roleIcons[node.role]
      return (
        <NTooltip key={node.id} delay={200}>
          {{
            trigger: () => (
              <button
                type='button'
                class={[
                  styles.node,
                  styles[node.status],
                  small && styles.smallNode
                ]}
                data-node={node.id}
                aria-label={t('monitor.cluster.node_detail_label', {
                  name: node.name,
                  status: stateNames.value[node.status],
                  role:
                    node.role === 'ALERT'
                      ? haRoleNames.value[node.haRole || 'UNKNOWN']
                      : ''
                })}
                onClick={() => (selected.value = node)}
              >
                <span class={['cluster-node-icon', styles.nodeIcon]}>
                  <NIcon size={small ? 30 : 44} component={Icon} />
                  {(node.role === 'API' || node.role === 'MASTER') && (
                    <span class={styles.roleBadge}>
                      <NIcon
                        size={12}
                        component={
                          node.role === 'API'
                            ? GlobalOutlined
                            : DeploymentUnitOutlined
                        }
                      />
                    </span>
                  )}
                  <i class={styles.statusDot} />
                </span>
                <span class={styles.nodeName}>{node.name}</span>
                <span class={styles.nodeCaption}>
                  {node.role === 'ALERT'
                    ? renderHaRole(node)
                    : node.coordinator
                    ? 'Coordinator'
                    : node.status === 'normal'
                    ? '\u00a0'
                    : stateNames.value[node.status]}
                </span>
              </button>
            ),
            default: () =>
              `${node.address} · ${stateNames.value[node.status]}${
                node.role === 'ALERT'
                  ? ` · ${haRoleNames.value[node.haRole || 'UNKNOWN']}`
                  : ''
              }`
          }}
        </NTooltip>
      )
    }

    const renderGroup = (group: { name: string; nodes: ClusterNodeInfo[] }) => {
      const isExpanded = expanded.value.includes(group.name)
      const busyCount = group.nodes.filter(
        (node) => node.status === 'busy'
      ).length
      const unknownCount = group.nodes.filter(
        (node) => node.status === 'unknown'
      ).length
      return (
        <div class={styles.workerGroup} key={group.name}>
          <button
            type='button'
            class={[
              styles.groupButton,
              busyCount > 0 && styles.busy,
              busyCount === 0 && unknownCount > 0 && styles.unknown
            ]}
            data-worker-group={group.name}
            aria-expanded={isExpanded}
            aria-label={t('monitor.cluster.group_label', {
              name: group.name,
              count: group.nodes.length,
              action: t(
                isExpanded
                  ? 'monitor.cluster.collapse'
                  : 'monitor.cluster.expand'
              )
            })}
            onClick={() => toggleGroup(group.name)}
          >
            <span class={['cluster-group-icon', styles.groupIcon]}>
              <NIcon
                class={styles.groupBackIcon}
                size={43}
                component={HddTwotone}
              />
              <NIcon
                class={styles.groupFrontIcon}
                size={43}
                component={HddTwotone}
              />
              {busyCount > 0 && <i class={styles.statusDot} />}
            </span>
            <span class={styles.groupName}>{group.name}</span>
            <span class={styles.groupCount}>
              {t('monitor.cluster.worker_count', { count: group.nodes.length })}
              <NIcon
                size={10}
                component={DownOutlined}
                class={isExpanded ? styles.expandedChevron : undefined}
              />
            </span>
            <span class={styles.groupState}>
              {busyCount > 0 || unknownCount > 0 ? (
                [
                  busyCount > 0 &&
                    t('monitor.cluster.busy_count', { count: busyCount }),
                  unknownCount > 0 &&
                    t('monitor.cluster.unknown_count', { count: unknownCount })
                ]
                  .filter(Boolean)
                  .join(' · ')
              ) : (
                <>
                  <NIcon size={12} component={CheckCircleFilled} />
                  {t('monitor.cluster.all_normal')}
                </>
              )}
            </span>
          </button>
          {isExpanded && (
            <div class={styles.groupMembers} data-expanded-group={group.name}>
              {group.nodes.map((node) => renderNode(node, true))}
            </div>
          )}
        </div>
      )
    }

    const statusTag = (node: ClusterNodeInfo) => (
      <NTag
        size='small'
        bordered={false}
        type={
          node.status === 'normal'
            ? 'success'
            : node.status === 'busy'
            ? 'warning'
            : 'default'
        }
      >
        {stateNames.value[node.status]}
      </NTag>
    )

    return () => {
      const detail = selected.value
      return (
        <section
          class={styles.page}
          style={{
            '--cluster-surface': theme.value.cardColor,
            '--cluster-text': theme.value.textColor1,
            '--cluster-secondary': theme.value.textColor3,
            '--cluster-line': theme.value.dividerColor,
            '--cluster-primary': theme.value.primaryColor,
            '--cluster-success': theme.value.successColor,
            '--cluster-warning': theme.value.warningColor
          }}
        >
          <div class={styles.pageHeading}>
            <div>
              <h1>{t('monitor.cluster.title')}</h1>
              <p>{t('monitor.cluster.subtitle')}</p>
            </div>
            <div class={styles.headingActions}>
              <span class={styles.snapshot}>{props.snapshotTime}</span>
              <NButton size='small' onClick={() => emit('refresh')}>
                {{
                  icon: () => <NIcon component={ReloadOutlined} />,
                  default: () => t('monitor.cluster.refresh')
                }}
              </NButton>
            </div>
          </div>
          <div class={styles.overview}>
            <span class={styles.total}>
              <strong>{props.nodes.length}</strong>
              {t('monitor.cluster.service_instances')}
            </span>
            <span>
              <i class={[styles.dot, styles.normalDot]} />
              {normal.value} {t('monitor.cluster.normal')}
            </span>
            <span>
              <i class={[styles.dot, styles.busyDot]} />
              {busy.value} {t('monitor.cluster.busy')}
            </span>
            <span>
              <i class={[styles.dot, styles.unknownDot]} />
              {unknown.value} {t('monitor.cluster.unknown')}
            </span>
            <span class={styles.groupTotal}>
              {t('monitor.cluster.group_count', {
                count: workers.value.length
              })}
            </span>
          </div>
          <div class={styles.content}>
            <NTabs
              value={activeTab.value}
              onUpdateValue={(value) => (activeTab.value = value)}
              animated={false}
            >
              <NTabPane
                name='topology'
                tab={t('monitor.cluster.topology')}
                displayDirective='show'
              >
                {!props.nodes.length && (
                  <NEmpty
                    description={t('monitor.cluster.no_nodes')}
                    style={{ padding: '80px 0' }}
                  />
                )}
                <div
                  class={styles.canvas}
                  ref={canvas}
                  style={{ display: props.nodes.length ? undefined : 'none' }}
                >
                  <svg
                    class={styles.connections}
                    role='img'
                    aria-label={t('monitor.cluster.topology_description')}
                  >
                    <defs>
                      <marker
                        id='cluster-flow-arrow'
                        viewBox='0 0 10 10'
                        refX='8'
                        refY='5'
                        markerWidth='5'
                        markerHeight='5'
                        orient='auto'
                      >
                        <path d='M 0 0 L 10 5 L 0 10 z' />
                      </marker>
                    </defs>
                    {paths.value.map((path, index) => (
                      <path
                        key={index}
                        d={path.d}
                        fill='none'
                        marker-end={
                          path.arrow ? 'url(#cluster-flow-arrow)' : undefined
                        }
                      />
                    ))}
                  </svg>
                  <div class={styles.layer} data-tier='API'>
                    <div class={styles.layerNodes} data-flow-nodes>
                      {props.nodes
                        .filter((node) => node.role === 'API')
                        .map((node) => renderNode(node))}
                    </div>
                  </div>
                  <div class={styles.layer} data-tier='MASTER'>
                    <div class={styles.layerNodes} data-flow-nodes>
                      {props.nodes
                        .filter((node) => node.role === 'MASTER')
                        .map((node) => renderNode(node))}
                    </div>
                    <aside class={styles.alerts}>
                      <span>Alert Server</span>
                      <div>
                        {props.nodes
                          .filter((node) => node.role === 'ALERT')
                          .map((node) => renderNode(node, true))}
                      </div>
                    </aside>
                  </div>
                  <div
                    class={[styles.layer, styles.workerLayer]}
                    data-tier='WORKER'
                  >
                    <div class={styles.workerGroups}>
                      {workers.value.map(renderGroup)}
                    </div>
                  </div>
                </div>
              </NTabPane>
              <NTabPane
                name='list'
                tab={`${t('monitor.cluster.node_list')} (${
                  props.nodes.length
                })`}
                displayDirective='show'
              >
                <NDataTable
                  rowKey={(node: ClusterNodeInfo) => node.id}
                  data={props.nodes}
                  bordered={false}
                  scrollX={680}
                  columns={[
                    {
                      title: t('monitor.cluster.node'),
                      key: 'id',
                      render: (node: ClusterNodeInfo) => (
                        <span>
                          {node.name}
                          <small class={styles.tableAddress}>
                            {node.address}
                          </small>
                        </span>
                      )
                    },
                    {
                      title: t('monitor.cluster.role_group'),
                      key: 'role',
                      render: (node: ClusterNodeInfo) => (
                        <span class={styles.roleCell}>
                          <span>
                            {roleNames[node.role]}
                            {node.group ? ` / ${node.group}` : ''}
                          </span>
                          {node.role === 'ALERT' && renderHaRole(node)}
                        </span>
                      )
                    },
                    {
                      title: t('monitor.cluster.status'),
                      key: 'status',
                      render: statusTag
                    },
                    {
                      title: 'CPU',
                      key: 'cpu',
                      render: (node: ClusterNodeInfo) =>
                        node.cpu === null ? '—' : `${node.cpu}%`
                    },
                    {
                      title: t('monitor.cluster.memory_usage'),
                      key: 'memory',
                      render: (node: ClusterNodeInfo) =>
                        node.memory === null ? '—' : `${node.memory}%`
                    },
                    {
                      title: t('monitor.cluster.details'),
                      key: 'action',
                      render: (node: ClusterNodeInfo) => (
                        <span class={styles.runtimeActions}>
                          <NButton
                            text
                            type='primary'
                            onClick={() => (selected.value = node)}
                          >
                            {t('monitor.cluster.view')}
                          </NButton>
                          {renderRuntimeActions(node)}
                        </span>
                      )
                    }
                  ]}
                />
              </NTabPane>
            </NTabs>
          </div>
          <div class={styles.note}>
            <NIcon component={InfoCircleOutlined} />
            <span>{t('monitor.cluster.unknown_note')}</span>
          </div>
          <NDrawer
            show={detail !== null}
            width={360}
            onUpdateShow={(show) => {
              if (!show) selected.value = null
            }}
          >
            <NDrawerContent title={detail?.name} closable>
              {detail && (
                <div class={styles.detail}>
                  <div class={styles.detailIntro}>
                    <span>{detail.address}</span>
                    {statusTag(detail)}
                  </div>
                  <NDescriptions column={1} labelPlacement='left'>
                    {detail.processId !== undefined && (
                      <NDescriptionsItem
                        label={t('monitor.cluster.process_id')}
                      >
                        {detail.processId}
                      </NDescriptionsItem>
                    )}
                    {detail.createTime && (
                      <NDescriptionsItem
                        label={t('monitor.cluster.startup_time')}
                      >
                        {detail.createTime}
                      </NDescriptionsItem>
                    )}
                    {detail.rpcAddress && (
                      <NDescriptionsItem
                        label={t('monitor.cluster.rpc_address')}
                      >
                        {detail.rpcAddress}
                      </NDescriptionsItem>
                    )}
                    {detail.directory && (
                      <NDescriptionsItem label={t('monitor.cluster.directory')}>
                        {detail.directory}
                      </NDescriptionsItem>
                    )}
                    <NDescriptionsItem
                      label={t('monitor.cluster.service_type')}
                    >
                      {roleNames[detail.role]}
                    </NDescriptionsItem>
                    {detail.role === 'ALERT' && (
                      <NDescriptionsItem label={t('monitor.cluster.ha_role')}>
                        {renderHaRole(detail)}
                      </NDescriptionsItem>
                    )}
                    {detail.group && (
                      <NDescriptionsItem
                        label={t('monitor.cluster.worker_group')}
                      >
                        {detail.group}
                      </NDescriptionsItem>
                    )}
                    {detail.coordinator && (
                      <NDescriptionsItem
                        label={t('monitor.cluster.coordinator_role')}
                      >
                        Coordinator
                      </NDescriptionsItem>
                    )}
                    <NDescriptionsItem
                      label={t('monitor.cluster.metadata_time')}
                    >
                      {detail.status === 'unknown'
                        ? t('monitor.cluster.unavailable')
                        : detail.lastHeartbeatTime || '—'}
                    </NDescriptionsItem>
                  </NDescriptions>
                  {detail.status === 'unknown' ? (
                    <p class={styles.detailNotice}>
                      {t('monitor.cluster.metadata_unavailable')}
                    </p>
                  ) : (
                    <div class={styles.metrics}>
                      {(
                        [
                          ['CPU', detail.cpu],
                          [t('monitor.cluster.memory'), detail.memory],
                          ...(detail.disk === undefined
                            ? []
                            : [[t('monitor.cluster.disk'), detail.disk]]),
                          ...(detail.threads === undefined
                            ? []
                            : [
                                [
                                  t('monitor.cluster.thread_pool'),
                                  detail.threads
                                ]
                              ])
                        ] as [string, number | null][]
                      ).map(
                        ([name, value]) =>
                          value !== null && (
                            <div key={name}>
                              <p>
                                {name}
                                <span>{value}%</span>
                              </p>
                              <NProgress
                                type='line'
                                percentage={value}
                                showIndicator={false}
                                height={5}
                                status={value >= 90 ? 'warning' : 'default'}
                              />
                            </div>
                          )
                      )}
                    </div>
                  )}
                  {renderRuntimeActions(detail)}
                </div>
              )}
            </NDrawerContent>
          </NDrawer>
          {props.canQueryExecutors && props.queryInstances && (
            <RunningInstancesModal
              target={runtimeTarget.value}
              queryInstances={props.queryInstances}
              onClose={() => (runtimeTarget.value = null)}
            />
          )}
        </section>
      )
    }
  }
})
