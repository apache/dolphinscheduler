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
  <div class="tooltips-model">
    <x-poptip
            trigger="click"
            placement="bottom-start">
      <pre :id="'result-' + id" class="result"></pre>
      <em class="iconfont" slot="reference">
        <slot name="reference"></slot>
      </em>
    </x-poptip>
  </div>
</template>
<script>
  import { syntaxHighlight } from '@/module/util/util'

  export default {
    name: 'tooltips',
    data () {
      return {}
    },
    props: {
      JSON: String,
      id: Number
    },
    mounted () {
      $(`#result-${this.id}`).html(syntaxHighlight(this.JSON))
    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .tooltips-model {
    .result {
      border: 0;
      padding: 5px;
      background: #fff;
      font-size: 12px;
    }
    .string { color: green; }
    .number { color: darkorange; }
    .boolean { color: blue; }
    .null { color: magenta; }
    .key { color: red; }
  }
</style>
