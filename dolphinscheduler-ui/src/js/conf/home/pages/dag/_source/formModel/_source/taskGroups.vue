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
  <el-select
          :disabled="isDetails"
          @change="_onChange"
          v-model="selectedValue"
          size="small"
          clearable
          style="width: 180px">
    <el-option
            v-for="item in taskGroupList"
            :key="item.id"
            :value="item.id"
            :label="item.name">
    </el-option>
  </el-select>
</template>
<script>
  import disabledState from '@/module/mixin/disabledState'
  export default {
    name: 'form-task-group',
    data () {
      return {
        selectedValue: this.value,
        taskGroupList: []
      }
    },
    mixins: [disabledState],
    props: {
      projectCode: {
        type: Number
      },
      value: {
        type: Number
      }
    },
    model: {
      prop: 'value',
      event: 'taskGroupIdEvent'
    },
    methods: {
      _onChange (o) {
        this.$emit('taskGroupIdEvent', o)
      }
    },
    watch: {
      value (val) {
        this.selectedValue = val
      }
    },
    created () {
      let stateTaskGroupList = this.store.state.resource.taskGroupListAll || []
      if (stateTaskGroupList.length) {
        this.taskGroupList = stateTaskGroupList
      } else {
        let params = {
          pageNo: 1,
          pageSize: 2147483647,
          projectCode: this.projectCode
        }
        this.store.dispatch('resource/getTaskGroupListPagingByProjectCode', params).then(res => {
          this.$nextTick(() => {
            if (res.totalList) {
              this.taskGroupList = res.totalList
            }
          })
        })
      }
    }
  }
</script>
