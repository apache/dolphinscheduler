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
  <div class="dependence-model">
    <m-list-box>
      <div slot="text">{{$t('Add dependency')}}</div>
      <div slot="content">
        <div class="dep-opt">
          <a href="javascript:"
             @click="!isDetails && _addDep()"
             class="add-dep">
            <em v-if="!isLoading" class="ans-icon-increase" :class="_isDetails" data-toggle="tooltip" :title="$t('Add')"></em>
            <em v-if="isLoading" class="ans-icon-spinner2 as as-spin" data-toggle="tooltip" :title="$t('Add')"></em>
          </a>
        </div>
        <div class="dep-box">
          <span
            class="dep-relation"
            @click="!isDetails && _setGlobalRelation()"
            v-if="dependTaskList.length">
            {{relation === 'AND' ? $t('and') : $t('or')}}
          </span>
          <div class="dep-list" v-for="(el,$index) in dependTaskList" :key='$index'>
            <span class="dep-line-pie"
                  v-if="el.dependItemList.length"
                  @click="!isDetails && _setRelation($index)">
              {{el.relation === 'AND' ? $t('and') : $t('or')}}
            </span>
            <em class="ans-icon-trash dep-delete"
               data-toggle="tooltip"
               data-container="body"
               :class="_isDetails"
               @click="!isDetails && _deleteDep($index)"
               :title="$t('delete')" >
            </em>
            <m-depend-item-list
              :dependTaskList='dependTaskList'
              v-model="el.dependItemList"
              @on-delete-all="_onDeleteAll"
              @getDependTaskList="getDependTaskList"
              :index="$index">
            </m-depend-item-list>
          </div>
        </div>
      </div>
    </m-list-box>
  </div>
</template>
<script>
  import _ from 'lodash'
  import mListBox from './_source/listBox'
  import mDependItemList from './_source/dependItemList'
  import disabledState from '@/module/mixin/disabledState'

  export default {
    name: 'dependence',
    data () {
      return {
        relation: 'AND',
        dependTaskList: [],
        isLoading: false
      }
    },
    mixins: [disabledState],
    props: {
      backfillItem: Object
    },
    methods: {
      _addDep () {
        if (!this.isLoading) {
          this.isLoading = true
          this.dependTaskList.push({
            dependItemList: [],
            relation: 'AND'
          })
        }
      },
      _deleteDep (i) {
        // remove index dependent
        this.dependTaskList.splice(i, 1)

        // remove tootip
        $('body').find('.tooltip.fade.top.in').remove()
      },
      _onDeleteAll (i) {
        this.dependTaskList.map((item,i)=>{
          if(item.dependItemList.length === 0){
            this.dependTaskList.splice(i,1)
          }
        })
        // this._deleteDep(i)
      },
      _setGlobalRelation () {
        this.relation = this.relation === 'AND' ? 'OR' : 'AND'
      },
      getDependTaskList(i){
        // console.log('getDependTaskList',i)
      },
      _setRelation (i) {
        this.dependTaskList[i].relation = this.dependTaskList[i].relation === 'AND' ? 'OR' : 'AND'
      },
      _verification () {
        this.$emit('on-dependent', {
          relation: this.relation,
          dependTaskList: _.map(this.dependTaskList, v => {
            return {
              relation: v.relation,
              dependItemList: _.map(v.dependItemList, v1 => _.omit(v1, ['depTasksList', 'state', 'dateValueList']))
            }
          })
        })
        return true
      }
    },
    watch: {
      dependTaskList (e) {
        setTimeout(() => {
          this.isLoading = false
        }, 600)
      },
      cacheDependence (val) {
        this.$emit('on-cache-dependent', val)
      }
    },
    beforeCreate () {
    },
    created () {
      let o = this.backfillItem
      let dependentResult = $(`#${o.id}`).data('dependent-result') || {}
      // Does not represent an empty object backfill
      if (!_.isEmpty(o)) {
        this.relation = _.cloneDeep(o.dependence.relation) || 'AND'
        this.dependTaskList = _.cloneDeep(o.dependence.dependTaskList) || []
        let defaultState = this.isDetails ? 'WAITING' : ''
        // Process instance return status display matches by key
        _.map(this.dependTaskList, v => _.map(v.dependItemList, v1 => v1.state = dependentResult[`${v1.definitionId}-${v1.depTasks}-${v1.cycle}-${v1.dateValue}`] || defaultState))
      }
    },
    mounted () {
    },
    destroyed () {
    },
    computed: {
      cacheDependence () {
        return {
          relation: this.relation,
          dependTaskList: _.map(this.dependTaskList, v => {
            return {
              relation: v.relation,
              dependItemList: _.map(v.dependItemList, v1 => _.omit(v1, ['depTasksList', 'state', 'dateValueList']))
            }
          })
        }
      }
    },
    components: { mListBox, mDependItemList }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .dependence-model {
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
    .dep-list {
      margin-bottom: 16px;
      position: relative;
      border-left: 1px solid #eee;
      padding-left: 16px;
      margin-left: -16px;
      transition: all 0.2s ease-out;
      padding-bottom: 20px;
      &:hover{
        border-left: 1px solid #0097e0;
        transition: all 0.2s ease-out;
        .dep-line-pie {
          transition: all 0.2s ease-out;
          border: 1px solid #0097e0;
          background: #0097e0;
          color: #fff;
        }
      }
      .dep-line-pie {
        transition: all 0.2s ease-out;
        position: absolute;
        width: 20px;
        height: 20px;
        border: 1px solid #e2e2e2;
        text-align: center;
        top: 50%;
        margin-top: -20px;
        z-index: 1;
        left: -10px;
        border-radius: 10px;
        background: #fff;
        font-size: 12px;
        cursor: pointer;
        &::selection {
          background:transparent;
        }
        &::-moz-selection {
          background:transparent;
        }
        &::-webkit-selection {
          background:transparent;
        }
      }
      .dep-delete {
        position: absolute;
        bottom: -6px;
        left: 14px;
        font-size: 18px;
        color: #ff0000;
        cursor: pointer;
      }
    }
    .dep-box {
      border-left: 4px solid #eee;
      margin-left: -46px;
      padding-left: 42px;
      position: relative;
      .dep-relation {
        position: absolute;
        width: 20px;
        height: 20px;
        border: 1px solid #e2e2e2;
        text-align: center;
        top: 50%;
        margin-top: -10px;
        z-index: 1;
        left: -12px;
        border-radius: 10px;
        background: #fff;
        font-size: 12px;
        cursor: pointer;
        &::selection {
          background:transparent;
        }
        &::-moz-selection {
          background:transparent;
        }
        &::-webkit-selection {
          background:transparent;
        }
      }
    }
  }
</style>
