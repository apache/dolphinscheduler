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
<template>
  <div class="dag-taskbar">
    <div class="taskbar-title">
      <h4>{{$t('Toolbar')}}</h4>
    </div>
    <div class="tasks">
      <template v-for="taskType in tasksTypeList">
        <draggable-box
          :key="taskType.name"
          @onDragstart="(e) => $emit('on-drag-start', e, taskType)"
        >
          <div class="task-item">
            <em :class="`icos-${taskType.name.toLocaleLowerCase()}`"></em>
            <span>{{ taskType.name }}</span>
          </div>
        </draggable-box>
      </template>
    </div>
  </div>
</template>

<script>
  import draggableBox from './draggableBox.vue'
  import { tasksType } from '../config.js'

  export default {
    name: 'dag-taskbar',
    components: {
      draggableBox
    },
    data () {
      const tasksTypeList = Object.keys(tasksType).map((type) => {
        return {
          name: type,
          desc: tasksType[type].desc
        }
      })

      return {
        tasksTypeList
      }
    }
  }
</script>

<style lang="scss" scoped>
@import "./taskbar";
</style>
