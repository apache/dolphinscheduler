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

import { defineComponent, onMounted, toRefs } from 'vue'
import { useSidebar } from './use-sidebar'
import styles from './dag-sidebar.module.scss'
import { NEllipsis, NIcon } from 'naive-ui'
import { StarFilled, StarOutlined } from '@vicons/antd'

const DagSidebar = defineComponent({
  name: 'DagSidebar',
  emits: ['Dragstart'],
  setup(props, context) {
    const { variables, getTaskList } = useSidebar()

    const handleDragstart = (task: string) => {
      context.emit('Dragstart', task)
    }

    const handleCollection = () => {}

    onMounted(() => {
      getTaskList()
    })

    return {
      ...toRefs(variables),
      handleDragstart,
      handleCollection
    }
  },
  render() {
    return (
      <div class={styles.sidebar}>
        {this.taskList.map((task: any) => {
          return (
            <div
              class={styles['draggable']}
              draggable='true'
              onDragstart={() => this.handleDragstart(task)}
            >
              <em
                class={styles['sidebar-icon']}
                style={{ backgroundImage: task.icon }}
              ></em>
              <NEllipsis style={{ width: '60px' }}>{task.name}</NEllipsis>
              <div
                class={styles.stars}
                onMouseenter={() => {
                  task.starHover = true
                }}
                onMouseleave={() => {
                  task.starHover = false
                }}
                onClick={() => this.handleCollection()}
              >
                <div class={styles.fav}>
                  <NIcon
                    size='18'
                    color={
                      task.collection || task.starHover ? '#288FFF' : '#ccc'
                    }
                  >
                    {task.collection ? <StarFilled /> : <StarOutlined />}
                  </NIcon>
                </div>
              </div>
            </div>
          )
        })}
      </div>
    )
  }
})

export { DagSidebar }
