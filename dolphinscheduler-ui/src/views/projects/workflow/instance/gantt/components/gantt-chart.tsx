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

import { computed, defineComponent, nextTick, PropType, ref, watch } from 'vue'
import { useResizeObserver, useWindowSize } from '@vueuse/core'
import { useI18n } from 'vue-i18n'
import { NButton, NIcon, NTooltip, useThemeVars } from 'naive-ui'
import { FileTextOutlined, InfoCircleOutlined } from '@vicons/antd'
import { tasksState } from '@/common/common'
import type { ITaskState } from '@/common/types'
import { useTimezoneStore } from '@/store/timezone/timezone'
import { formatDuration } from '../model'
import { availableGanttHeight } from '../layout'
import type { GanttModel } from '../model'
import type { GanttRow } from '../type'
import styles from '../index.module.scss'

export default defineComponent({
  name: 'GanttChart',
  props: { model: { type: Object as PropType<GanttModel>, required: true } },
  emits: { viewLog: (ignoredRow: GanttRow) => true },
  setup(props, { emit }) {
    const { t } = useI18n()
    const timezone = useTimezoneStore()
    const theme = useThemeVars()
    const axis = ref<HTMLElement>()
    const viewport = ref<HTMLElement>()
    const width = ref(600)
    const viewportHeight = ref<number>()
    const { height: windowHeight } = useWindowSize()
    const updateViewportHeight = () => {
      const element = viewport.value
      if (!element) return
      const container = element.closest('.n-scrollbar-container')
      const containerBottom =
        container?.getBoundingClientRect().bottom ?? window.innerHeight
      viewportHeight.value = availableGanttHeight(
        element.getBoundingClientRect().top,
        containerBottom
      )
    }
    useResizeObserver(axis, (entries) => {
      width.value = entries[0].contentRect.width
    })
    useResizeObserver(viewport, updateViewportHeight)
    watch(
      windowHeight,
      () => {
        void nextTick(updateViewportHeight)
      },
      { immediate: true }
    )
    watch(
      () => props.model.rows.length,
      () => {
        void nextTick(updateViewportHeight)
      }
    )
    const ticks = computed(() => {
      const count = Math.max(1, Math.floor(width.value / 110))
      return Array.from({ length: count + 1 }, (_, index) => index / count)
    })
    const state = (row: GanttRow) =>
      tasksState(t)[row.state as ITaskState]?.desc ||
      (row.state === 'NOT_SUBMITTED'
        ? t('project.workflow.gantt_pending')
        : row.state)
    const color = (row: GanttRow) => {
      if (['SUCCESS', 'FORCED_SUCCESS'].includes(row.state))
        return theme.value.successColor
      if (row.state === 'FAILURE') return theme.value.errorColor
      if (row.state === 'RUNNING_EXECUTION') return theme.value.infoColor
      if (['KILL', 'STOP', 'PAUSE'].includes(row.state))
        return theme.value.warningColor
      return theme.value.textColor3
    }
    const date = (time: number | null) =>
      time === null
        ? '—'
        : new Date(time).toLocaleString(undefined, {
            timeZone: timezone.getTimezone,
            hour12: false
          })
    const details = (row: GanttRow) => (
      <div class={styles.tooltip}>
        <strong>{row.name}</strong>
        <div>{state(row)}</div>
        <div>
          {t('project.workflow.gantt_start')}: {date(row.start)}
        </div>
        <div>
          {t('project.workflow.gantt_end')}:{' '}
          {row.state === 'RUNNING_EXECUTION'
            ? t('project.workflow.executing')
            : date(row.end)}
        </div>
        <div>
          {t('project.workflow.gantt_duration')}: {formatDuration(row.duration)}
        </div>
        <div>
          {t('project.workflow.gantt_share')}: {row.percent.toFixed(2)}%
        </div>
      </div>
    )
    return () => (
      <section class={styles.chart}>
        <div class={styles.toolbar}>
          <div class={styles.timelineTitle}>
            <strong>{t('project.workflow.gantt_timeline')}</strong>
            <NTooltip trigger='hover'>
              {{
                trigger: () => (
                  <NButton
                    text
                    size='tiny'
                    aria-label={t('project.workflow.gantt_help')}
                  >
                    <NIcon size={14}>
                      <InfoCircleOutlined />
                    </NIcon>
                  </NButton>
                ),
                default: () => (
                  <div class={styles.help}>
                    <div>{t('project.workflow.gantt_axis_note')}</div>
                    <div>{t('project.workflow.gantt_share_note')}</div>
                  </div>
                )
              }}
            </NTooltip>
          </div>
          <span class={styles.elapsed}>
            {t('project.workflow.gantt_elapsed')}{' '}
            <strong>{formatDuration(props.model.duration)}</strong>
          </span>
        </div>
        <div
          ref={viewport}
          class={styles.viewport}
          style={{
            maxHeight:
              viewportHeight.value === undefined
                ? undefined
                : `${viewportHeight.value}px`
          }}
          tabindex={0}
          aria-label={t('project.workflow.gantt_timeline')}
        >
          <div
            class={styles.grid}
            role='table'
            aria-label={t('project.workflow.gantt_timeline')}
          >
            <div class={[styles.nameCell, styles.corner]} role='columnheader'>
              <span>{t('project.workflow.gantt_task')}</span>
              <span>{t('project.workflow.gantt_duration_share')}</span>
            </div>
            <div ref={axis} class={styles.axis} role='columnheader'>
              {ticks.value.map((fraction, index) => (
                <span
                  key={index}
                  class={styles.tick}
                  style={{
                    left: `${fraction * 100}%`,
                    transform:
                      index === 0
                        ? 'none'
                        : index === ticks.value.length - 1
                        ? 'translateX(-100%)'
                        : 'translateX(-50%)'
                  }}
                >
                  <span>
                    {formatDuration(fraction * props.model.axisDuration)}
                  </span>
                  <small>{Math.round(fraction * 100)}%</small>
                </span>
              ))}
            </div>
            {props.model.rows.map((row, index) => (
              <div key={row.code} role='row' class={styles.row}>
                <div class={styles.nameCell} role='cell'>
                  <span class={styles.index}>
                    {String(index + 1).padStart(2, '0')}
                  </span>
                  <div class={styles.taskInfo}>
                    <div class={styles.taskTitle}>
                      <span class={styles.taskName} title={row.name}>
                        {row.name}
                      </span>
                      <NTooltip>
                        {{
                          trigger: () => (
                            <NButton
                              text
                              size='tiny'
                              class={styles.logButton}
                              disabled={!row.logAvailable}
                              aria-label={`${row.name} · ${t(
                                'project.task.view_log'
                              )}`}
                              onClick={() => emit('viewLog', row)}
                            >
                              <NIcon size={14}>
                                <FileTextOutlined />
                              </NIcon>
                            </NButton>
                          ),
                          default: () =>
                            row.logAvailable
                              ? t('project.task.view_log')
                              : t('project.workflow.gantt_no_log')
                        }}
                      </NTooltip>
                    </div>
                    <span class={styles.taskState}>
                      <i style={{ background: color(row) }} />
                      {state(row)}
                      {row.taskType && ` · ${row.taskType}`}
                    </span>
                  </div>
                  <div class={styles.metrics}>
                    <span>{formatDuration(row.duration)}</span>
                    <small>
                      {row.duration === null
                        ? '—'
                        : `${row.percent.toFixed(2)}%`}
                    </small>
                  </div>
                </div>
                <div
                  class={styles.lane}
                  role='cell'
                  style={{
                    backgroundSize: `${100 / (ticks.value.length - 1)}% 100%`
                  }}
                >
                  {row.start !== null && row.duration !== null ? (
                    <NTooltip>
                      {{
                        trigger: () => (
                          <span
                            class={[
                              styles.bar,
                              row.state === 'RUNNING_EXECUTION' &&
                                styles.running
                            ]}
                            style={{
                              left: `min(calc(100% - var(--gantt-min-bar-width)), ${
                                ((row.start! - props.model.start) /
                                  props.model.axisDuration) *
                                100
                              }%)`,
                              width: `${
                                (row.duration! / props.model.axisDuration) * 100
                              }%`,
                              backgroundColor: color(row)
                            }}
                            aria-label={`${row.name} · ${state(
                              row
                            )} · ${formatDuration(
                              row.duration
                            )} · ${row.percent.toFixed(2)}%`}
                          >
                            {(row.duration! / props.model.axisDuration) *
                              width.value >
                              95 && <span>{formatDuration(row.duration)}</span>}
                          </span>
                        ),
                        default: () => details(row)
                      }}
                    </NTooltip>
                  ) : (
                    <span class={styles.noBar}>
                      {row.start === null
                        ? state(row)
                        : t('project.workflow.gantt_time_unavailable')}
                    </span>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>
    )
  }
})
