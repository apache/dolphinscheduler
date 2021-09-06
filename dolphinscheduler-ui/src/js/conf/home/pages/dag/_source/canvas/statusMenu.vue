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
  <div
    class="dag-status-menu"
    v-show="visible"
    :style="{
      left: `${left}px`,
      top: `${top}px`,
    }"
  >
  </div>
</template>

<script>
  import { mapState } from 'vuex'

  export default {
    name: 'dag-status-menu',
    inject: ['dagChart', 'dagCanvas'],
    data () {
      return {
        visible: false,
        left: 0,
        top: 0,
        currentTask: {
          code: 0,
          name: '',
          type: ''
        }
      }
    },
    computed: {
      ...mapState('dag', ['tasks'])
    },
    methods: {
      setCurrentTask (task) {
        this.currentTask = { ...this.currentTask, ...task }
      },
      show (x = 0, y = 0) {
        this.dagCanvas.lockScroller()
        this.visible = true
        this.left = x
        this.top = y
      },
      hide () {
        this.dagCanvas.unlockScroller()
        this.visible = false
      }
    }
  }
</script>

<style lang="scss" scoped>
@import "./contextMenu";
</style>
