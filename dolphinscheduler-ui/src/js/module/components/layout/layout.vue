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
  <div class="main-layout-model">
    <div class="m-top">
      <slot name="top"></slot>
    </div>
    <div class="m-bottom">
      <slot name="bottom"></slot>
    </div>
  </div>
</template>
<script>
  export default {
    name: 'main-layout'
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .main-layout-model {
    position: relative;
    z-index: 0;
    .m-top {
      width: 100%;
      position: fixed;
      left: 0;
      top: 0;
      height: 60px;
      z-index: 1;
    }
    .m-bottom {
      position: absolute;
      top: 60px;
      left: 0px;
      width: 100%;
      z-index: 0;
      min-height: calc(100% - 60px);
      
    }
  }
</style>
