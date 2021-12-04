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
  <list-box>
    <div slot="text">{{ $t("Reference from") }}</div>
    <div slot="content" class="copy-from" ref="copyFrom">
      <div class="copy-from-content">
        <el-input
          class="copy-from-input"
          v-model="searchVal"
          :placeholder="getTaskName(value) || $t('Please choose')"
          @focus="inputFocus"
          @input="searchValChange"
          size="small"
          :suffix-icon="
            dropdownVisible ? 'el-icon-arrow-up' : 'el-icon-arrow-down'
          "
        ></el-input>
        <div class="copy-from-dropdown" v-show="dropdownVisible">
          <div class="scroll-box">
            <ul v-infinite-scroll="load">
              <li
                v-for="taskDefinition in taskDefinitions"
                :key="taskDefinition.code"
                class="dropdown-item"
                @click="itemClick(taskDefinition)"
              >
                {{ taskDefinition.name }}
              </li>
            </ul>
            <p class="dropdown-msg" v-if="loading">{{ $t("Loading...") }}</p>
            <p class="dropdown-msg" v-if="noMore">{{ $t("No more...") }}</p>
          </div>
        </div>
      </div>
    </div>
  </list-box>
</template>

<script>
  import ListBox from '../tasks/_source/listBox'
  import { mapActions } from 'vuex'

  export default {
    name: 'copy-from-task',
    props: {
      taskType: String
    },
    inject: ['formModel'],
    data () {
      return {
        pageNo: 1,
        pageSize: 10,
        searchVal: '',
        value: '',
        loading: false,
        noMore: false,
        taskDefinitions: [],
        dropdownVisible: false
      }
    },
    mounted () {
      document.addEventListener('click', this.outsideClick)
      this.load()
    },
    destroyed () {
      document.removeEventListener('click', this.outsideClick)
    },
    methods: {
      ...mapActions('dag', ['getTaskDefinitionList']),
      outsideClick (e) {
        const elem = this.$refs.copyFrom
        if (!elem.contains(e.target) && this.dropdownVisible) {
          this.dropdownVisible = false
        }
      },
      searchValChange (val) {
        this.load(true)
      },
      load (override) {
        if (this.loading) return
        if (override) {
          this.noMore = false
          this.pageNo = 1
        }
        if (this.noMore) return
        this.loading = true
        this.getTaskDefinitionsList({
          pageNo: this.pageNo,
          pageSize: this.pageSize,
          searchVal: this.searchVal,
          taskType: this.taskType
        }).then((res) => {
          this.taskDefinitions = override ? res.totalList : this.taskDefinitions.concat(res.totalList)
          this.pageNo = res.currentPage + 1
          this.noMore = this.taskDefinitions.length >= res.total
          this.loading = false
        })
      },
      itemClick (taskDefinition) {
        this.value = taskDefinition.code
        this.searchVal = taskDefinition.name
        this.dropdownVisible = false

        if (this.formModel) {
          const backfillItem = this.formModel.taskToBackfillItem(taskDefinition)
          this.formModel.backfillRefresh = false
          this.$nextTick(() => {
            this.formModel.backfillItem = backfillItem
            this.formModel.backfill(backfillItem)
            this.formModel.backfillRefresh = true
          })
        }
      },
      inputFocus () {
        this.searchVal = ''
        this.dropdownVisible = true
      },
      inputBlur () {
        this.dropdownVisible = false
      },
      getTaskName (code) {
        const taskDefinition = this.taskDefinitions.find(
          (taskDefinition) => taskDefinition.code === code
        )
        return taskDefinition ? taskDefinition.name : ''
      }
    },
    components: {
      ListBox
    }
  }
</script>

<style lang="scss" scoped>
.copy-from {
  position: relative;
  &-content {
    width: 100%;
  }
  &-input {
    width: 100%;
  }
  &-dropdown {
    width: 100%;
    position: absolute;
    padding: 6px 0;
    top: 42px;
    left: 0;
    z-index: 10;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    background-color: #fff;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

    .scroll-box {
      width: 100%;
      max-height: 200px;
      overflow: auto;
    }

    .dropdown-item {
      font-size: 14px;
      padding: 0 20px;
      position: relative;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      color: #606266;
      height: 34px;
      line-height: 34px;
      box-sizing: border-box;
      cursor: pointer;
      &:hover {
        background-color: #f5f7fa;
      }
      &.selected {
        color: #409eff;
        font-weight: 700;
      }
    }

    &:before,
    &:after {
      content: "";
      position: absolute;
      display: block;
      width: 0;
      height: 0;
      top: -6px;
      left: 35px;
      border-width: 6px;
      border-top-width: 0;
      border-color: transparent;
      border-bottom-color: #fff;
      border-style: solid;
      z-index: 10;
    }

    &:before {
      top: -8px;
      left: 33px;
      border-width: 8px;
      border-top-width: 0;
      border-bottom-color: #ebeef5;
      z-index: 9;
    }

    .dropdown-msg {
      text-align: center;
      color: #666;
      font-size: 12px;
      line-height: 34px;
      margin: 0;
    }
  }
}
</style>
