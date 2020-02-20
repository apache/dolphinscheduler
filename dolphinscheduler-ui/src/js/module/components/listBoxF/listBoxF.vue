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
  <div class="clearfix list-box-f">
    <div class="text">
      <slot name="name"></slot>
    </div>
    <div class="cont">
      <slot name="content"></slot>
    </div>
  </div>
</template>
<script>
  export default {
    name: 'list-box-f'
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .list-box-f {
    margin-bottom: 20px;
    &:last-child {
      margin-bottom: 0;
    }
    .text {
      width: 144px;
      float: left;
      text-align: right;
      line-height: 32px;
      padding-right: 8px;
      >strong {
        color: #ff0000;
        padding-right: 4px;
      }
    }
    .cont {
      width: calc(100% - 162px);
      float: left;
      .textarea-content {
        .v-input {
          textarea {
            min-height: 400px !important;
            font-size: 12px;
          }
        }
      }
      >.v-radio-group,>.ans-radio-group {
        padding-top: 7px;
      }
      >.v-input {
        textarea {
          height: 70px;
        }
      }
      .v-radio-group-item {
        font-weight: normal;
        margin-top: 1px;
      }
    }
  }
</style>
