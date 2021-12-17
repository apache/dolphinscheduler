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
    class="dag-node-tips"
    v-show="visible"
    :style="{
      left: `${left}px`,
      top: `${top}px`,
    }"
  >
    <span class="nodeTips">{{ message }}</span>
  </div>
</template>

<script>

  export default {
    name: 'dag-node-tooltip',
    data () {
      return {
        visible: false,
        left: 0,
        top: 0,
        message: ''
      }
    },
    computed: {},
    mounted () {
      document.addEventListener('click', (e) => {
        this.hide()
      })
      document.addEventListener('mouseout', (e) => {
        this.hide()
      })
    },
    methods: {
      show (x, y, message) {
        this.visible = true
        // 12px is aligned to the left side of the node
        this.left = x > 12 ? x - 12 : x
        // 50px is reserved for the node status icon
        this.top = y > 50 ? y - 50 : y
        this.message = message
      },
      hide () {
        this.visible = false
      }
    }
  }
</script>

<style lang="scss" scoped>
@import "./nodeTooltip";
</style>
