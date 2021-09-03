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
    class="dag-context-menu"
    v-show="visible"
    :style="{
      left: `${left}px`,
      top: `${top}px`,
    }"
  >
    <div
      class="menu-item"
      :class="startAvailable ? '' : 'disable'"
      @click="onStart(!startAvailable)"
    >
      {{ $t("Start") }}
    </div>
    <div
      class="menu-item"
      :class="readOnly ? 'disable' : ''"
      @click="onEdit(readOnly)"
    >
      {{ $t("Edit") }}
    </div>
    <div
      class="menu-item"
      :class="readOnly ? 'disable' : ''"
      @click="onCopy(readOnly)"
    >
      {{ $t("Copy") }}
    </div>
    <div
      class="menu-item"
      :class="readOnly ? 'disable' : ''"
      @click="onDelete(readOnly)"
    >
      {{ $t("Delete") }}
    </div>
  </div>
</template>

<script>
  import { mapState } from 'vuex'
  import { findComponentDownward } from '@/module/util/'

  export default {
    name: 'dag-context-menu',
    inject: ['dagChart'],
    data () {
      return {
        visible: false,
        left: 0,
        top: 0,
        canvasRef: null,
        currentTask: {
          id: 0,
          name: '',
          type: ''
        }
      }
    },
    computed: {
      ...mapState('dag', ['isDetails', 'releaseState']),
      startAvailable () {
        return (
          this.$route.name === 'projects-definition-details' &&
          this.releaseState !== 'NOT_RELEASE'
        )
      },
      readOnly () {
        return this.isDetails
      }
    },
    mounted () {
      document.addEventListener('click', (e) => {
        this.hide()
      })
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
      setCurrentTask (task) {
        this.currentTask = { ...this.currentTask, ...task }
      },
      onStart (isReadOnly) {
        if (isReadOnly) return
        this.dagChart.startRunning(this.currentTask.name)
      },
      onEdit (isReadOnly) {
        if (isReadOnly) return
        this.dagChart.openFormModel(this.currentTask.id, this.currentTask.type)
      },
      onCopy (isReadOnly) {
        if (isReadOnly) return
        console.log(1)
      },
      onDelete (isReadOnly) {
        if (isReadOnly) return
        const canvas = this.getDagCanvasRef()
        canvas.removeNode(this.currentTask.id + '')
      },
      show (x = 0, y = 0) {
        const canvas = this.getDagCanvasRef()
        canvas.lockScroller()
        this.visible = true
        this.left = x + 10
        this.top = y + 10
      },
      hide () {
        const canvas = this.getDagCanvasRef()
        canvas.unlockScroller()
        this.visible = false
      }
    }
  }
</script>

<style lang="scss" scoped>
@import "./contextMenu";
</style>
