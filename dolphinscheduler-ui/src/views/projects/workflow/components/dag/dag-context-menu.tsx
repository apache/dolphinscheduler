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

import { genTaskCodeList } from '@/service/modules/task-definition'
import type { Cell } from '@antv/x6'
import { defineComponent, onMounted, PropType, inject, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import styles from './menu.module.scss'
import { uuid } from '@/common/common'
import { IWorkflowTaskInstance } from './types'
import { NButton } from 'naive-ui'

const props = {
  startDisplay: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  menuDisplay: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  taskInstance: {
    type: Object as PropType<IWorkflowTaskInstance>,
    require: true
  },
  cell: {
    type: Object as PropType<Cell>,
    require: true
  },
  visible: {
    type: Boolean as PropType<boolean>,
    default: true
  },
  left: {
    type: Number as PropType<number>,
    default: 0
  },
  top: {
    type: Number as PropType<number>,
    default: 0
  }
}

export default defineComponent({
  name: 'dag-context-menu',
  props,
  emits: ['hide', 'start', 'edit', 'viewLog', 'copyTask', 'removeTasks'],
  setup(props, ctx) {
    const graph = inject('graph', ref())
    const route = useRoute()
    const projectCode = Number(route.params.projectCode)

    const hide = () => {
      ctx.emit('hide', false)
    }

    const startRunning = () => {
      ctx.emit('start', Number(props.cell?.id))
    }

    const handleEdit = () => {
      ctx.emit('edit', Number(props.cell?.id))
    }

    const handleViewLog = () => {
      if (props.taskInstance) {
        ctx.emit('viewLog', props.taskInstance.id, props.taskInstance.taskType)
      }
    }

    const handleCopy = () => {
      const genNums = 1
      const type = props.cell?.data.taskType
      const taskName = uuid(props.cell?.data.taskName + '_')
      const targetCode = Number(props.cell?.id)
      const flag = props.cell?.data.flag

      genTaskCodeList(genNums, projectCode).then((res) => {
        const [code] = res
        ctx.emit('copyTask', taskName, code, targetCode, type, flag, {
          x: props.left + 100,
          y: props.top + 100
        })
      })
    }

    const handleDelete = () => {
      graph.value?.removeCell(props.cell)
      ctx.emit('removeTasks', [Number(props.cell?.id)])
    }

    onMounted(() => {
      document.addEventListener('click', () => {
        hide()
      })
    })

    return {
      startRunning,
      handleEdit,
      handleCopy,
      handleDelete,
      handleViewLog
    }
  },
  render() {
    const { t } = useI18n()

    return (
      this.visible && (
        <div
          class={styles['dag-context-menu']}
          style={{ left: `${this.left}px`, top: `${this.top}px` }}
        >
          {this.startDisplay && (
            <NButton
              class={`${styles['menu-item']}`}
              onClick={this.startRunning}
            >
              {t('project.node.start')}
            </NButton>
          )}
          {this.menuDisplay && (
            <>
              <NButton
                class={`${styles['menu-item']}`}
                onClick={this.handleEdit}
              >
                {t('project.node.edit')}
              </NButton>
              <NButton
                class={`${styles['menu-item']}`}
                onClick={this.handleCopy}
              >
                {t('project.node.copy')}
              </NButton>
              <NButton
                class={`${styles['menu-item']}`}
                onClick={this.handleDelete}
              >
                {t('project.node.delete')}
              </NButton>
            </>
          )}
          {this.taskInstance && (
            <NButton
              class={`${styles['menu-item']}`}
              onClick={this.handleViewLog}
            >
              {t('project.node.view_log')}
            </NButton>
          )}
        </div>
      )
    )
  }
})
