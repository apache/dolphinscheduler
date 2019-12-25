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
  <div class="progress-bar-model">
    <div class="progress-bar-box">
      <div class="bar" :style="{width:value + '%',height:'4px'}">&nbsp;</div>
    </div>
    <template v-if="textPlacement === 'bottom'">
      <div class="textp text-1">
        {{$t('Uploading...')}} <span>({{value}}%)</span>
      </div>
    </template>
    <template v-if="textPlacement === 'left-right'">
      <div class="textp text-2-1">{{$t('Uploading...')}}</div>
      <div class="textp text-2-2"><span>({{value}}%)</span></div>
    </template>
  </div>
</template>
<script>
  export default {
    name: 'progress-bar',
    data () {
      return {}
    },
    props: {
      value: Number,
      textPlacement: String
    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .progress-bar-model {
    position: relative;
    margin-right: 46px;
    .progress-bar-box{
      width: 100%;
      height: 4px;
      background: #f6faff;
      border-radius: 4px;
      .bar {
        background: #2d8cf0;
        border-radius: 3px;
      }
    }
    .textp {
      font-size: 12px;
      color: #fff;
      display: inline-block;
      span {
        color: #2d8cf0;
        font-weight: bold;
      }
    }
    .text-1 {
      padding-top: 6px;
    }
    .text-2-1 {
      position: absolute;
      left: -70px;
      top: -7px;
    }
    .text-2-2 {
      position: absolute;
      right: -42px;
      top: -7px;
    }
  }
</style>
