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
      <div slot="text">{{ $t("Pre tasks") }}</div>
      <div slot="content">
        <el-select
          style="width: 100%"
          filterable
          multiple
          size="small"
          v-model="preTasks"
          :disabled="isDetails"
        >
          <el-option
            v-for="task in options"
            :key="task.code"
            :value="task.code"
            :label="task.name"
          >
          </el-option>
        </el-select>
      </div>
    </m-list-box>
  </div>
</template>
<script>
  import disabledState from '@/module/mixin/disabledState'
  import mListBox from './_source/listBox'
  import { mapState } from 'vuex'
  import { findComponentDownward } from '@/module/util/'

  export default {
    name: 'pre_tasks',
    mixins: [disabledState],
    inject: ['dagChart'],
    props: {
      code: {
        type: Number,
        default: 0
      }
    },
    data () {
      return {
        options: [],
        preTasks: []
      }
    },
    mounted () {
      const canvas = this.getDagCanvasRef()
      const edges = canvas.getEdges()
      this.preTasks = canvas.getPrevNodes(this.code).map(node => node.id)
      this.options = this.tasks.filter((task) => {
        // The current node cannot be used as the prev node
        if (task.code === this.code) return false
        if (this.preTasks.includes(task.code)) return true
        // The number of edges start with CONDITIONS task cannot be greater than 2
        if (task.taskType === 'CONDITIONS') {
          return edges.filter((e) => e.sourceId === task.code).length < 2
        }
        return true
      })
    },
    computed: {
      ...mapState('dag', ['tasks'])
    },
    methods: {
      getDagCanvasRef () {
        if (this.canvasRef) {
          return this.canvasRef
        } else {
          const canvas = findComponentDownward(this.dagChart, 'dag-canvas')
          this.canvasRef = canvas
          return canvas
        }
      },
      setPreNodes () {
        const canvas = this.getDagCanvasRef()
        canvas.setPreNodes(this.code, this.preTasks, true)
      }
    },
    components: { mListBox }
  }
</script>
