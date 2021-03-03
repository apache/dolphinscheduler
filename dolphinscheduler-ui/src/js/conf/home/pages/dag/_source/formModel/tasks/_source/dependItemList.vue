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
  <div class="dep-list-model">
    <div v-for="(el,$index) in dependItemList" :key='$index'>
      <el-select filterable :disabled="isDetails" style="width: 450px" v-model="el.projectId" @change="v => _onChangeProjectId(v, $index)" size="small">
        <el-option v-for="item in projectList" :key="item.value" :value="item.value" :label="item.label"></el-option>
      </el-select>
      <el-select filterable :disabled="isDetails" style="width: 450px" v-model="el.definitionId" @change="v => _onChangeDefinitionId(v, $index)" size="small">
        <el-option v-for="item in el.definitionList" :key="item.value" :value="item.value" :label="item.label"></el-option>
      </el-select>
      <el-select filterable :disabled="isDetails" style="width: 450px" v-model="el.depTasks" size="small">
        <el-option v-for="item in el.depTasksList || []" :key="item" :value="item" :label="item"></el-option>
      </el-select>
      <el-select v-model="el.cycle" :disabled="isDetails" @change="v => _onChangeCycle(v, $index)" size="small">
        <el-option v-for="item in cycleList" :key="item.value" :value="item.value" :label="item.label"></el-option>
      </el-select>
      <el-select v-model="el.dateValue" :disabled="isDetails" size="small">
        <el-option v-for="item in el.dateValueList || []" :key="item.value" :value="item.value" :label="item.label"></el-option>
      </el-select>
      <template v-if="isInstance">
        <span class="instance-state">
          <em class="iconfont el-icon-success" :class="'icon-' + el.state" v-if="el.state === 'SUCCESS'" data-toggle="tooltip" data-container="body" :title="$t('success')"></em>
          <em class="iconfont el-icon-timer" :class="'icon-' + el.state" v-if="el.state === 'WAITING'" data-toggle="tooltip" data-container="body" :title="$t('waiting')"></em>
          <em class="iconfont el-icon-error" :class="'icon-' + el.state" v-if="el.state === 'FAILED'" data-toggle="tooltip" data-container="body" :title="$t('failed')"></em>
        </span>
      </template>
      <span class="operation">
        <a href="javascript:" class="delete" @click="!isDetails && _remove($index)">
          <em class="el-icon-delete" :class="_isDetails" data-toggle="tooltip" data-container="body" :title="$t('delete')" ></em>
        </a>
        <a href="javascript:" class="add" @click="!isDetails && _add()" v-if="$index === (dependItemList.length - 1)">
          <em class="iconfont el-icon-circle-plus-outline" :class="_isDetails" data-toggle="tooltip" data-container="body" :title="$t('Add')"></em>
        </a>
      </span>
    </div>
  </div>
</template>

<script>
  import _ from 'lodash'
  import { cycleList, dateValueList } from './commcon'
  import disabledState from '@/module/mixin/disabledState'
  export default {
    name: 'dep-list',
    data () {
      return {
        list: [],
        projectList: [],
        cycleList: cycleList,
        isInstance: false
      }
    },
    mixins: [disabledState],
    props: {
      dependItemList: Array,
      index: Number,
      dependTaskList: Array
    },
    model: {
      prop: 'dependItemList',
      event: 'dependItemListEvent'
    },
    methods: {
      /**
       * add task
       */
      _add () {
        // btn loading
        this.isLoading = true

        // add task list
        let projectId = this.projectList[0].value
        this._getProcessByProjectId(projectId).then(definitionList => {
          // dependItemList index
          let is = (value) => _.some(this.dependItemList, { definitionId: value })
          let noArr = _.filter(definitionList, v => !is(v.value))
          let value = noArr[0] && noArr[0].value || null
          let val = value || definitionList[0].value
          this._getDependItemList(val).then(depTasksList => {
            this.$nextTick(() => {
              this.$emit('dependItemListEvent', _.concat(this.dependItemList, this._rtNewParams(val, definitionList, depTasksList, projectId)))
            })
          })
        })
        // remove tooltip
        this._removeTip()
      },
      /**
       * remove task
       */
      _remove (i) {
        // eslint-disable-next-line
        this.dependTaskList[this.index].dependItemList.splice(i, 1)
        this._removeTip()
        if (!this.dependItemList.length || this.dependItemList.length === 0) {
          this.$emit('on-delete-all', {
            index: this.index
          })
        }
      },
      _getProjectList () {
        return new Promise((resolve, reject) => {
          this.projectList = _.map(_.cloneDeep(this.store.state.dag.projectListS), v => {
            return {
              value: v.id,
              label: v.name
            }
          })
          resolve()
        })
      },
      /**
       * get processlist
       */
      _getProcessList () {
        return new Promise((resolve, reject) => {
          let definitionList = _.map(_.cloneDeep(this.store.state.dag.processListS), v => {
            return {
              value: v.id,
              label: v.name
            }
          })
          resolve(definitionList)
        })
      },
      _getProcessByProjectId (id) {
        return new Promise((resolve, reject) => {
          this.store.dispatch('dag/getProcessByProjectId', { projectId: id }).then(res => {
            let definitionList = _.map(_.cloneDeep(res), v => {
              return {
                value: v.id,
                label: v.name
              }
            })
            resolve(definitionList)
          })
        })
      },
      /**
       * get dependItemList
       */
      _getDependItemList (ids, is = true) {
        return new Promise((resolve, reject) => {
          if (is) {
            this.store.dispatch('dag/getProcessTasksList', { processDefinitionId: ids }).then(res => {
              resolve(['ALL'].concat(_.map(res, v => v.name)))
            })
          } else {
            this.store.dispatch('dag/getTaskListDefIdAll', { processDefinitionIdList: ids }).then(res => {
              resolve(res)
            })
          }
        })
      },
      /**
       * change process get dependItemList
       */
      _onChangeProjectId (value, itemIndex) {
        this._getProcessByProjectId(value).then(definitionList => {
          /* this.$set(this.dependItemList, itemIndex, this._dlOldParams(value, definitionList, item)) */
          let definitionId = definitionList[0].value
          this._getDependItemList(definitionId).then(depTasksList => {
            let item = this.dependItemList[itemIndex]
            // init set depTasks All
            item.depTasks = 'ALL'
            // set dependItemList item data
            this.$set(this.dependItemList, itemIndex, this._cpOldParams(value, definitionId, definitionList, depTasksList, item))
          })
        })
      },
      _onChangeDefinitionId (value, itemIndex) {
        // get depItem list data
        this._getDependItemList(value).then(depTasksList => {
          let item = this.dependItemList[itemIndex]
          // init set depTasks All
          item.depTasks = 'ALL'
          // set dependItemList item data
          this.$set(this.dependItemList, itemIndex, this._rtOldParams(value, item.definitionList, depTasksList, item))
        })
      },
      _onChangeCycle (value, itemIndex) {
        let list = _.cloneDeep(dateValueList[value])
        this.$set(this.dependItemList[itemIndex], 'dateValue', list[0].value)
        this.$set(this.dependItemList[itemIndex], 'dateValueList', list)
      },
      _rtNewParams (value, definitionList, depTasksList, projectId) {
        return {
          projectId: projectId,
          definitionId: value,
          // dependItem need private definitionList
          definitionList: definitionList,
          depTasks: 'ALL',
          depTasksList: depTasksList,
          cycle: 'day',
          dateValue: 'today',
          dateValueList: _.cloneDeep(dateValueList.day),
          state: ''
        }
      },
      _rtOldParams (value, definitionList, depTasksList, item) {
        return {
          projectId: item.projectId,
          definitionId: value,
          // dependItem need private definitionList
          definitionList: definitionList,
          depTasks: item.depTasks || 'ALL',
          depTasksList: depTasksList,
          cycle: item.cycle,
          dateValue: item.dateValue,
          dateValueList: _.cloneDeep(dateValueList[item.cycle]),
          state: item.state
        }
      },

      _cpOldParams (value, definitionId, definitionList, depTasksList, item) {
        return {
          projectId: value,
          definitionList: definitionList,
          definitionId: definitionId,
          depTasks: item.depTasks || 'ALL',
          depTasksList: depTasksList,
          cycle: item.cycle,
          dateValue: item.dateValue,
          dateValueList: _.cloneDeep(dateValueList[item.cycle]),
          state: item.state
        }
      },
      /**
       * remove tip
       */
      _removeTip () {
        $('body').find('.tooltip.fade.top.in').remove()
      }
    },
    watch: {
    },
    beforeCreate () {
    },
    created () {
      // is type projects-instance-details
      this.isInstance = this.router.history.current.name === 'projects-instance-details'
      // get processlist
      this._getProjectList().then(() => {
        if (!this.dependItemList.length) {
          let projectId = this.projectList[0].value
          this._getProcessByProjectId(projectId).then(definitionList => {
            let value = definitionList[0].value
            this._getDependItemList(value).then(depTasksList => {
              this.$emit('dependItemListEvent', _.concat(this.dependItemList, this._rtNewParams(value, definitionList, depTasksList, projectId)))
            })
          })
        } else {
          // get definitionId ids
          let ids = _.map(this.dependItemList, v => v.definitionId).join(',')
          // get item list
          this._getDependItemList(ids, false).then(res => {
            _.map(this.dependItemList, (v, i) => {
              this._getProcessByProjectId(v.projectId).then(definitionList => {
                this.$set(this.dependItemList, i, this._rtOldParams(v.definitionId, definitionList, ['ALL'].concat(_.map(res[v.definitionId] || [], v => v.name)), v))
              })
            })
          })
        }
      })
    },
    mounted () {
    },
    components: {}
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .dep-list-model {
    position: relative;
    min-height: 32px;
    .list {
      margin-bottom: 6px;
      .operation {
        width: 80px;
        padding-left: 4px;
        a {
          i {
            font-size: 18px;
            vertical-align: middle;
          }
        }
        .delete {
          color: #ff0000;
        }
        .add {
          color: #0097e0;
        }
      }
    }
    .instance-state {
      display: inline-block;
      width: 24px;
      .iconfont {
        font-size: 20px;
        vertical-align: middle;
        cursor: pointer;
        margin-left: 6px;
        &.icon-SUCCESS {
          color: #33cc00;
        }
        &.icon-WAITING {
          color: #888888;
        }
        &.icon-FAILED {
          color: #F31322;
        }
      }
    }
  }
</style>
