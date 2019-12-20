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
  <div class="conditions-model">
    <div class="left">
      <slot name="button-group"></slot>
    </div>
    <div class="right">
      <div class="from-box">
        <slot name="search-group" v-if="isShow"></slot>
        <template v-if="!isShow">
          <div class="list">
            <x-button type="ghost" size="small" @click="_ckQuery" icon="ans-icon-search"></x-button>
          </div>
          <div class="list">
            <x-input v-model="searchVal"
                     @on-enterkey="_ckQuery"
                     size="small"
                     :placeholder="$t('Please enter keyword')"
                     type="text"
                     style="width:180px;">
            </x-input>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  export default {
    name: 'conditions',
    data () {
      return {
        // search value
        searchVal: ''
      }
    },
    props: {
      operation: Array
    },
    methods: {
      /**
       * emit Query parameter
       */
      _ckQuery () {
        this.$emit('on-conditions', {
          searchVal: _.trim(this.searchVal)
        })
      }
    },
    computed: {
      // Whether the slot comes in
      isShow () {
        return this.$slots['search-group']
      }
    },
    created () {
      // Routing parameter merging
      if (!_.isEmpty(this.$route.query)) {
        this.searchVal = this.$route.query.searchVal || ''
      }
    },
    components: {}
  }
</script>
