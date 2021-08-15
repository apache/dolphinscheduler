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
  <div class="pre_tasks-model">
    <m-list-box>
      <div slot="text">{{$t('Pre tasks')}}</div>
      <div slot="content">
        <el-select
            ref="preTasksSelector"
            style="width: 100%;"
            filterable
            multiple
            size="small"
            v-model="preTasks"
            :disabled="isDetails"
            :id="preTasksSelectorId">
          <el-option
              v-for="task in preTaskList"
              :key="task.id"
              :value="task.id"
              :label="task.name">
          </el-option>
        </el-select>
      </div>
    </m-list-box>
  </div>
</template>
<script>
  import disabledState from '@/module/mixin/disabledState'
  import mListBox from './_source/listBox'

  export default {
    name: 'pre_tasks',
    mixins: [disabledState],
    props: {
      backfillItem: Object
    },
    data () {
      return {
        preTasksSelectorId: '_preTasksSelectorId', // Refresh target vue-component by changing id
        preTasks: [],
        preTasksOld: []
      }
    },
    mounted () {
      this.preTasks = this.backfillItem.preTasks || this.preTasks
      this.preTasksOld = this.preTasks

      // Refresh target vue-component by changing id
      this.$nextTick(() => {
        this.preTasksSelectorId = 'preTasksSelectorId'
      })
    },
    computed: {
      preTaskList: function () {
        let currentTaskId = this.backfillItem.id || this.id
        let cacheTasks = Object.assign({}, this.store.state.dag.tasks)
        let keys = Object.keys(cacheTasks)
        for (let i = 0; i < keys.length; i++) {
          let key = keys[i]
          if ((!cacheTasks[key].id || !cacheTasks[key].name) || (currentTaskId && cacheTasks[key].id === currentTaskId)) {
            // Clean undefined and current task data
            delete cacheTasks[key]
          }
        }

        return cacheTasks
      },
      // preTaskIds used to create new connection
      preTasksToAdd: function () {
        let toAddTasks = this.preTasks.filter(taskId => {
          return (this.preTasksOld.indexOf(taskId) === -1)
        })
        return toAddTasks
      },
      // preTaskIds used to delete connection
      preTasksToDelete: function () {
        return this.preTasksOld.filter(taskId => this.preTasks.indexOf(taskId) === -1)
      }
    },
    methods: {
      // Pass data to parent-level to process dag
      _verification () {
        this.$emit('on-pre-tasks', {
          preTasks: this.preTasks,
          preTasksToAdd: this.preTasksToAdd,
          preTasksToDelete: this.preTasksToDelete
        })
        return true
      }
    },
    components: { mListBox }
  }
</script>
