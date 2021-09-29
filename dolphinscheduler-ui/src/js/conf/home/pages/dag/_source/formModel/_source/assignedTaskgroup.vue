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
          style="width: 180px">
    <el-option
            v-for="item in taskgroupList"
            :key="item.code"
            :value="item.id"
            :label="item.name">
    </el-option>
  </el-select>
</template>
<script>
  import disabledState from '@/module/mixin/disabledState'
  import { mapActions, mapMutations } from 'vuex'
  export default {
    name: 'form-assigned-taskgroup',
    data () {
      return {
        selectedValue: this.value,

        taskgroupList: [],
        searchParams: {
                  status:1,
                  pageSize: 10,
                  pageNo: 1
                },
      }
    },
    mixins: [disabledState],
    props: {
      value: {
        type: String
      },

    },
    model: {
      prop: 'value',
      event: 'taskgroupEvent'
    },
    methods: {
     ...mapActions('security', ['listAllTaskGroupByStatus']),
      _onChange (o) {
        this.$emit('taskgroupEvent', o)
      },

      _initTaskgroupOptions () {
        this.taskgroupOptions = []
        this.selectedValue = ''


        if (this.taskgroupList.length > 0) {

          if (this.taskgroupList.length === 1 && this.selectedValue === '') {
            this.selectedValue = this.taskgroupList[0].id
          }
          console.log("33333",this.selectedValue)
          this.$emit('taskgroupEvent', this.selectedValue)
        } else {
          this.selectedValue = ''
          this.$emit('taskgroupEvent', this.selectedValue)
        }
      }
    },
    watch: {
      value: function (val) {
      },

    },
    created () {
          let taskgroupList = this.store.state.security.taskgroupListAll || []

          if (taskgroupList.length && taskgroupList.length > 0) {
            this.taskgroupList = taskgroupList

          } else {
            this.listAllTaskGroupByStatus(this.searchParams).then(res => {
              this.taskgroupList = res.data.totalList;
              this.selectedValue=res.data.totalList[0].id;
              this.$emit('taskgroupEvent', this.selectedValue)

              }).catch(e => {
                this.isLoading = false
              })

          }
        }
  }
</script>
