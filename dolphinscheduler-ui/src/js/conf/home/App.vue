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
  <m-layout>
    <m-nav slot="top"></m-nav>
    <router-view slot="bottom" v-if="isRenderRouterView"></router-view>
  </m-layout>
</template>

<script>
  import visibility from '@/module/visibility'
  import mLayout from '@/module/components/layout/layout'
  import mNav from '@/module/components/nav/nav'
  export default {
    name: 'app',
    data () {
      return {
        isRenderRouterView: true
      }
    },
    methods: {
      reload () {
        this.isRenderRouterView = false
        this.$nextTick(() => {
          this.isRenderRouterView = true
        })
      }
    },
    mounted () {
      visibility.change((evt, hidden) => {
        if (hidden === false && this.$route.meta.refresh_in_switched_tab) {
          this.reload()
        }
      })
    },
    components: { mLayout, mNav }
  }
</script>
