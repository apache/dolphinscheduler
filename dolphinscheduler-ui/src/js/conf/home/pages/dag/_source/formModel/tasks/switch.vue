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
    <m-list-box>
      <div slot="text">{{$t('condition')}}</div>
      <div slot="content">
        <div class="dep-opt">
          <a href="javascript:"
             @click="!isDetails && _addDep()"
             class="add-dep">
            <em v-if="!isLoading" class="el-icon-circle-plus-outline" :class="_isDetails" data-toggle="tooltip" :title="$t('Add')"></em>
            <em v-if="isLoading" class="el-icon-loading as as-spin" data-toggle="tooltip" :title="$t('Add')"></em>
          </a>
        </div>
        <div class="dep-list-model">
          <div v-for="(el,index) in dependItemList" :key='index' class="switch-list">
            <label style="display:block">
              <textarea :id="`code-switch-mirror${index}`" :name="`code-switch-mirror${index}`" style="opacity: 0;">
              </textarea>
            </label>
            <span class="text-b" style="padding-left: 0">{{$t('Branch flow')}}</span>
            <el-select style="width: 157px;" size="small" v-model="el.nextNode" clearable :disabled="isDetails">
              <el-option v-for="item in postTasks" :key="item.code" :value="item.name" :label="item.name"></el-option>
            </el-select>
            <span class="operation">
              <a href="javascript:" class="delete" @click="!isDetails && _removeDep(index)" v-if="index === (dependItemList.length - 1)">
                <em class="iconfont el-icon-delete" :class="_isDetails" data-toggle="tooltip" data-container="body" :title="$t('Delete')" ></em>
              </a>
              <a href="javascript:" class="add" @click="!isDetails && _addDep()" v-if="index === (dependItemList.length - 1)">
                <em class="iconfont el-icon-circle-plus-outline" :class="_isDetails" data-toggle="tooltip" data-container="body" :title="$t('Add')"></em>
              </a>
            </span>
          </div>
        </div>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Branch flow')}}</div>
      <div slot="content">
        <el-select style="width: 157px;" size="small" v-model="nextNode" clearable :disabled="isDetails">
          <el-option v-for="item in postTasks" :key="item.code" :value="item.name" :label="item.name"></el-option>
        </el-select>
      </div>
    </m-list-box>
  </div>
</template>
<script>
  import _ from 'lodash'
  import mListBox from './_source/listBox'
  import disabledState from '@/module/mixin/disabledState'
  import codemirror from '@/conf/home/pages/resource/pages/file/pages/_source/codemirror'

  let editArray = []
  export default {
    name: 'dependence',
    data () {
      return {
        nextNode: '',
        relation: 'AND',
        dependItemList: [],
        editArray: [],
        isLoading: false,
        oldList: []
      }
    },
    mixins: [disabledState],
    props: {
      nodeData: Object,
      backfillItem: Object,
      postTasks: Array
    },
    methods: {
      editList (index) {
        // editor
        const self = this
        const editor = codemirror(`code-switch-mirror${index}`, {
          mode: 'shell',
          readOnly: this.isDetails
        }, this)
        editor.on('change', function () {
          const outputList = _.cloneDeep(self.dependItemList)
          outputList[index].condition = editor.getValue()
          self.dependItemList = outputList
        })

        this.keypress = () => {
          if (!editor.getOption('readOnly')) {
            editor.showHint({
              completeSingle: false
            })
          }
        }
        editor.on('keypress', this.keypress)
        editor.setValue(this.dependItemList[index].condition || '')
        editor.setSize('580px', '60px')
        editArray.push(editor)
        this.oldList = _.cloneDeep(this.dependItemList)
        return editArray
      },
      _addDep () {
        if (!this.isLoading) {
          this.isLoading = true

          this.dependItemList.push({
            condition: '',
            nextNode: ''
          })
          let dependItemListLen = this.dependItemList.length
          if (dependItemListLen > 0) {
            setTimeout(() => {
              this.editList(dependItemListLen - 1)
              this.isLoading = false
            }, 200)
          }
          this._removeTip()
        }
      },
      _removeDep (i) {
        this.dependItemList.splice(i, 1)
        this._removeTip()
      },
      _removeTip () {
        $('body').find('.tooltip.fade.top.in').remove()
      },
      _verification () {
        let flag = this.dependItemList.some((item) => {
          return !item.condition
        })
        if (flag) {
          this.$message.warning(`${this.$t('The condition content cannot be empty')}`)
          return false
        }
        let params = {
          dependTaskList: this.dependItemList || [],
          nextNode: this.nextNode || ''
        }
        this.$emit('on-switch-result', params)
        return true
      }
    },
    watch: {},
    beforeCreate () {
    },
    created () {
      const o = this.backfillItem
      if (!_.isEmpty(o)) {
        let switchResult = o.switchResult || {}
        this.dependItemList = _.cloneDeep(switchResult.dependTaskList) || []
        this.nextNode = _.cloneDeep(switchResult.nextNode) || ''
      }
    },
    mounted () {
      if (this.dependItemList && this.dependItemList.length > 0) {
        setTimeout(() => {
          this.dependItemList.forEach((item, index) => {
            this.editList(index)
          })
        })
      }
    },
    destroyed () {
    },
    computed: {
    },
    components: { mListBox }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .conditions-model {
    margin-top: -10px;
    .dep-opt {
      margin-bottom: 10px;
      padding-top: 3px;
      line-height: 24px;
      .add-dep {
        color: #0097e0;
        margin-right: 10px;
        i {
          font-size: 18px;
          vertical-align: middle;
        }
      }
    }
    .dep-list-model{
      position: relative;
      min-height: 0px;
      .switch-list {
        margin-bottom: 6px;
        .operation {
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
    }
  }
</style>
