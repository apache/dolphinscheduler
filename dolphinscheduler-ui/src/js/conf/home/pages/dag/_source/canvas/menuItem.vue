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
  <div class="menu-item" :class="disabled ? 'disabled' : ''" @click="onClick">
    <slot></slot>
  </div>
</template>

<script>
  export default {
    name: 'dag-context-menu-item',
    props: {
      disabled: {
        type: Boolean,
        default: false
      }
    },
    data () {
      return {}
    },
    methods: {
      onClick (e) {
        if (this.disabled) return
        this.$emit('on-click', e)
      }
    }
  }
</script>
