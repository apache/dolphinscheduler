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
  DateValue,
  GanttRow,
  IGanttRes,
  TaskInstance,
  WorkflowInstance
} from './type'

const terminalWorkflowStates = new Set(['SUCCESS', 'FAILURE', 'STOP', 'PAUSE'])
const terminalTaskStates = new Set([
  'SUCCESS',
  'FORCED_SUCCESS',
  'FAILURE',
  'KILL',
  'STOP',
  'PAUSE'
])
export const isWorkflowActive = (state: string) =>
  !terminalWorkflowStates.has(state)

function timestamp(value?: DateValue): number | null {
  if (value === null || value === undefined || value === '') return null
  // API Jackson dates are UTC unless a timezone is explicitly included.
  const normalized = typeof value === 'string' ? value.replace(' ', 'T') : ''
  const parsed =
    typeof value === 'number'
      ? value
      : Date.parse(
          /(?:Z|[+-]\d{2}:?\d{2})$/i.test(normalized)
            ? normalized
            : `${normalized}Z`
        )
  return Number.isFinite(parsed) ? parsed : null
}

export function buildGanttModel(
  workflow: WorkflowInstance,
  tasks: TaskInstance[],
  gantt?: IGanttRes,
  now = Date.now()
) {
  // Calibrate formatted server dates against the epoch timestamps returned by view-gantt.
  let reference: IGanttRes['tasks'][number] | undefined
  const ganttTasksByName = new Map<string, IGanttRes['tasks']>()
  for (const task of gantt?.tasks || []) {
    if (!reference && task.isoStart && Number.isFinite(task.startDate?.[0]))
      reference = task
    const matches = ganttTasksByName.get(task.taskName)
    if (matches) matches.push(task)
    else ganttTasksByName.set(task.taskName, [task])
  }
  const formatted = timestamp(reference?.isoStart)
  const offset =
    reference && formatted !== null
      ? Math.floor(reference.startDate[0] / 1000) * 1000 - formatted
      : 0
  const date = (value?: DateValue) => {
    const parsed = timestamp(value)
    return parsed === null
      ? null
      : parsed + (typeof value === 'number' ? 0 : offset)
  }
  const latest = new Map<number, TaskInstance>()
  for (const task of tasks) {
    const previous = latest.get(task.taskCode)
    if (
      !previous ||
      (previous.flag === 'NO' && task.flag !== 'NO') ||
      (previous.flag === task.flag && task.id > previous.id) ||
      (previous.flag !== 'YES' && task.flag === 'YES')
    )
      latest.set(task.taskCode, task)
  }
  const definitions = new Map(
    (workflow.dagData?.taskDefinitionList || []).map((task) => [
      task.code,
      task
    ])
  )
  latest.forEach((task) => {
    if (!definitions.has(task.taskCode))
      definitions.set(task.taskCode, {
        code: task.taskCode,
        name: task.name,
        taskType: task.taskType
      })
  })
  const nameCounts = new Map<string, number>()
  latest.forEach((task) =>
    nameCounts.set(task.name, (nameCounts.get(task.name) || 0) + 1)
  )
  const order = new Map(
    (gantt?.taskNames || []).map((code, index) => [code, index])
  )
  const rows: GanttRow[] = [...definitions.values()]
    .sort(
      (a, b) =>
        (order.get(a.code) ?? Infinity) - (order.get(b.code) ?? Infinity)
    )
    .map((definition) => {
      const task = latest.get(definition.code)
      let start = date(task?.startTime)
      let end = date(task?.endTime)
      const precise =
        task && nameCounts.get(task.name) === 1
          ? ganttTasksByName
              .get(task.name)
              ?.find((item) => !item.status || item.status === task.state)
          : undefined
      // The gantt endpoint has no instance ID. Only use precision for an unambiguous matching attempt.
      if (
        start !== null &&
        precise &&
        Math.abs(precise.startDate[0] - start) < 1000
      ) {
        start = precise.startDate[0]
        if (end !== null && Number.isFinite(precise.endDate[0]))
          end = precise.endDate[0]
      }
      if (
        start !== null &&
        end === null &&
        task &&
        !terminalTaskStates.has(task.state)
      )
        end = now
      if (start !== null && end !== null) end = Math.max(start, end)
      return {
        code: definition.code,
        name: task?.name || definition.name,
        taskType: task?.taskType || definition.taskType || '',
        id: task?.id,
        state: task?.state || 'NOT_SUBMITTED',
        start,
        end,
        duration: start !== null && end !== null ? end - start : null,
        percent: 0,
        logAvailable: Boolean(task?.id && (task.logPath || task.startTime))
      }
    })
  const starts = rows.flatMap((row) => (row.start === null ? [] : [row.start]))
  const workflowStart = date(workflow.startTime)
  const start = Math.min(...starts, workflowStart ?? Infinity)
  const safeStart = Number.isFinite(start) ? start : now
  const ends = rows.flatMap((row) => (row.end === null ? [] : [row.end]))
  const end = Math.max(
    safeStart,
    ...ends,
    date(workflow.endTime) ??
      (isWorkflowActive(workflow.state) ? now : safeStart)
  )
  const duration = end - safeStart
  rows.forEach((row) => {
    row.percent =
      duration > 0 && row.duration !== null
        ? (row.duration / duration) * 100
        : 0
  })
  const count = (...states: string[]) =>
    rows.filter((row) => states.includes(row.state)).length
  const stats = {
    total: rows.length,
    submitted: rows.filter((row) => row.id !== undefined).length,
    pending: count('NOT_SUBMITTED'),
    running: count('RUNNING_EXECUTION'),
    success: count('SUCCESS', 'FORCED_SUCCESS'),
    failed: count('FAILURE'),
    stopped: count('PAUSE', 'STOP', 'KILL'),
    waiting: rows.filter(
      (row) =>
        row.id !== undefined &&
        row.state !== 'RUNNING_EXECUTION' &&
        !terminalTaskStates.has(row.state)
    ).length
  }
  return {
    rows,
    stats,
    start: safeStart,
    end,
    duration,
    axisDuration: duration > 0 ? duration : 1000
  }
}

export type GanttModel = ReturnType<typeof buildGanttModel>

export function formatDuration(milliseconds: number | null): string {
  if (milliseconds === null) return '—'
  if (milliseconds < 1000) return `${Math.round(milliseconds)} ms`
  if (milliseconds < 60000)
    return `${Number((milliseconds / 1000).toFixed(2))} s`
  const seconds = Math.floor(milliseconds / 1000)
  if (seconds < 3600) return `${Math.floor(seconds / 60)}m ${seconds % 60}s`
  return `${Math.floor(seconds / 3600)}h ${Math.floor((seconds % 3600) / 60)}m`
}
