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
  <div class="no-data-model" :style="{height:height + 'px'}">
    <div class="no-data-box">
      <div class="img">
        <img src="./images/errorTip.png" alt="">
      </div>
      <div class="text">{{msg || $t('No data')}}</div>
    </div>
  </div>
</template>
<script>
  export default {
    name: 'no-data',
    props: {
      msg: String,
      height: Number
    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .no-data-model {
    position: relative;
    width: calc(100%);
    height: calc(100vh - 200px);
    .no-data-box {
      width: 210px;
      height: 210px;
      position: absolute;
      left: 50%;
      top: 50%;
      margin-left: -105px;
      margin-top: -105px;
      text-align: center;
      .text {
        padding-top: 10px;
        color: #666;
      }
    }
  }
</style>
