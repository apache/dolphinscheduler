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
      <div class="form-box">
        <slot name="search-group" v-if="isShow"></slot>
        <template v-if="!isShow">
          <div class="list">
            <el-button
              size="mini"
              @click="_ckQuery"
              icon="el-icon-search"
            ></el-button>
          </div>
          <div class="list">
            <el-input
              v-model="searchVal"
              @keyup.enter="_ckQuery"
              size="mini"
              :placeholder="$t('Please enter keyword')"
              type="text"
              style="width: 180px"
              clearable
            >
            </el-input>
          </div>
          <div class="list" v-if="taskTypeShow">
            <el-select
              size="mini"
              style="width: 140px"
              :placeholder="$t('type')"
              :value="taskType"
              @change="_onChangeTaskType"
              clearable
            >
              <el-option
                v-for="(task, index) in taskTypeList"
                :key="index"
                :value="task.desc"
                :label="index"
              >
              </el-option>
            </el-select>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  /**
   * taskType list
   */
  import { tasksType } from '@/conf/home/pages/dag/_source/config.js'
  export default {
    name: 'conditions',
    data () {
      return {
        // taskType list
        taskTypeList: tasksType,
        // search value
        searchVal: '',
        // taskType switch
        taskType: ''
      }
    },
    props: {
      taskTypeShow: Boolean,
      operation: Array
    },
    methods: {
      /**
       * switch taskType
       */
      _onChangeTaskType (val) {
        this.taskType = val
      },
      /**
       * emit Query parameter
       */
      _ckQuery () {
        this.$emit('on-conditions', {
          searchVal: _.trim(this.searchVal),
          taskType: this.taskType
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
        this.taskType = this.$route.query.taskType || ''
      }
    },
    components: {}
  }
</script>
