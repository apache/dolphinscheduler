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
          clearable
          @change="_onChange"
          v-model="selectedValue"
          size="small"
          style="width: 100%">
    <el-option
            v-for="item in alarmGroupsList"
            :key="item.id"
            :value="item.id"
            :label="item.groupName">
    </el-option>
  </el-select>
</template>
<script>
  import disabledState from '@/module/mixin/disabledState'
  export default {
    name: 'form-warning-group',
    data () {
      return {
        selectedValue: this.value,
        workerGroupsList: []
      }
    },
    mixins: [disabledState],
    props: {
      value: {
        type: Number,
        default: null
      }
    },
    model: {
      prop: 'value',
      event: 'warningGroupsEvent'
    },
    methods: {
      _onChange (o) {
        this.$emit('warningGroupsEvent', o)
      }
    },
    watch: {
      value (val) {
        this.selectedValue = val
      }
    },
    created () {
      let stateAlarmGroupsList = this.store.state.security.alarmGroupsListAll || []
      if (stateAlarmGroupsList.length) {
        this.alarmGroupsList = stateAlarmGroupsList
      } else {
        this.store.dispatch('security/getAlarmGroupsAll').then(res => {
          this.$nextTick(() => {
            this.alarmGroupsList = res
          })
        })
      }
    }
  }
</script>
