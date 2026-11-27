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

import type {
  ServerNode,
  ServerNodeType
} from '@/service/modules/monitor/types'
import type { ClusterNodeInfo } from './cluster-overview'

interface Heartbeat {
  serverStatus?: string
  cpuUsage?: number
  memoryUsage?: number
  threadPoolUsage?: number
  diskUsage?: number
  workerGroup?: string
  httpPort?: number
  active?: boolean
  isActive?: boolean
  coordinator?: boolean
  isCoordinator?: boolean
}

const percentage = (value: unknown): number | null =>
  typeof value === 'number' &&
  Number.isFinite(value) &&
  value >= 0 &&
  value <= 1
    ? Math.round(value * 10000) / 100
    : null

export function toClusterNodes(
  type: ServerNodeType,
  servers: ServerNode[]
): ClusterNodeInfo[] {
  return servers.map((server) => {
    let heartbeat: Heartbeat | null = null
    if (!server.metadataStale && server.heartBeatInfo) {
      try {
        const parsed = JSON.parse(server.heartBeatInfo)
        if (parsed && typeof parsed === 'object' && !Array.isArray(parsed))
          heartbeat = parsed
      } catch {
        // A registration remains visible even when its metadata cannot be decoded.
      }
    }
    const status =
      heartbeat?.serverStatus === 'NORMAL'
        ? 'normal'
        : heartbeat?.serverStatus === 'BUSY'
        ? 'busy'
        : 'unknown'
    const active =
      typeof heartbeat?.active === 'boolean'
        ? heartbeat.active
        : heartbeat?.isActive
    const httpPort = heartbeat?.httpPort
    const port =
      type === 'API' &&
      typeof httpPort === 'number' &&
      httpPort > 0 &&
      httpPort <= 65535
        ? httpPort
        : server.port
    const address = `${server.host}:${port}`
    return {
      id: `${type}:${server.host}:${server.port}:${server.id}:${server.createTime}`,
      name: address,
      role: type === 'ALERT_SERVER' ? 'ALERT' : type,
      address,
      rpcAddress: type === 'API' ? `${server.host}:${server.port}` : undefined,
      status,
      cpu: percentage(heartbeat?.cpuUsage),
      memory: percentage(heartbeat?.memoryUsage),
      threads: percentage(heartbeat?.threadPoolUsage) ?? undefined,
      disk: percentage(heartbeat?.diskUsage) ?? undefined,
      group: type === 'WORKER' ? heartbeat?.workerGroup : undefined,
      coordinator:
        heartbeat?.coordinator === true || heartbeat?.isCoordinator === true,
      haRole:
        typeof active === 'boolean'
          ? active
            ? 'ACTIVE'
            : 'STANDBY'
          : 'UNKNOWN',
      lastHeartbeatTime: heartbeat ? server.lastHeartbeatTime : null,
      createTime: server.createTime,
      directory: server.serverDirectory,
      processId: server.id
    }
  })
}
