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

import { defineComponent } from 'vue'
import {
  TaskType,
  TASK_TYPES_MAP
} from '@/views/projects/task/constants/task-type'
import Styles from './dag.module.scss'

export default defineComponent({
  name: 'workflow-dag-sidebar',
  emits: ['dragStart'],
  setup(props, context) {
    const allTaskTypes = Object.keys(TASK_TYPES_MAP).map((type) => ({
      type,
      ...TASK_TYPES_MAP[type as TaskType]
    }))

    return () => (
      <div class={Styles.sidebar}>
        {allTaskTypes.map((task) => (
          <div
            class={Styles.draggable}
            draggable='true'
            onDragstart={(e) => {
              context.emit('dragStart', e, task.type as TaskType)
            }}
          >
            <em
              class={[
                Styles['sidebar-icon'],
                Styles['icon-' + task.type.toLocaleLowerCase()]
              ]}
            ></em>
            <span>{task.alias}</span>
          </div>
        ))}
      </div>
    )
  }
})
