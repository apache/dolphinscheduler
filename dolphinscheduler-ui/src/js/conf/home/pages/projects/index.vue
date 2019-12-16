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
  <div class="main-layout-box" :class="!isProjectsList ? '' : 'no'">
    <m-secondary-menu :type="'projects'" v-if="!isProjectsList"></m-secondary-menu>
    <router-view></router-view>
  </div>
</template>
<script>
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  export default {
    name: 'projects-index',
    data () {
      return {
        isProjectsList: this.$router.history.current.name === 'projects-list'
      }
    },
    watch: {
      '$route' ({ name }) {
        this.isProjectsList = name === 'projects-list'
      }
    },
    components: { mSecondaryMenu }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .toolbar-color-sp,.state-tasks-color-sp {
    >a {
      display: inline-block;
      margin-right: 10px;
      cursor:default;
      &:hover {
        span {
          color: #333;
        }
      }
      >i {
        border-radius: 10px;
        display: inline-block;
        vertical-align: middle;
        font-size: 14px;
      }
      span {
        vertical-align: middle;
        font-size: 12px;
      }
    }
  }
  .toolbar-color-sp {
    >a {
      >i {
        font-size: 15px;
      }
    }
  }
</style>
