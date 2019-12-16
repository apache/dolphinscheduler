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
  <div class="clearfix list">
    <div class="text-box">
      <span><slot name="text"></slot></span>
    </div>
    <div class="cont-box">
      <div class="label-box">
        <slot name="content"></slot>
      </div>
    </div>
  </div>
</template>
<script>
  export default {
    name: 'list-box'
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .cont-box {
    .label-box {
      .ans-radio-group {
        margin-top: 7px;
      }
    }
  }

  .v-checkbox-wrapper {
    &.v-checkbox-wrapper-disabled {
      color: #999 ;
      .v-checkbox {
        .v-checkbox-inner{
          border-color:#dddee1;
          background: #f7f7f7;
          color: #bbbec4 ;
          &:after{
            border: 2px solid #ddd;
            border-top: 0;
            border-left: 0;
          }
        }
      }
    }
  }

</style>
