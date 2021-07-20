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
  <div class="user-def-columns-model">
    <div class="select-listpp"
         v-for="(item,$index) in localParamsList"
         :key="item.id"
         @click="_getIndex($index)">
      <span>field:</span>
      <x-input
        readonly
        :disabled="isDetails"
        type="text"
        :value="$index"
        placeholder=""
        style="width: 80px">
      </x-input>
      <span>column:</span>
      <x-input
        :disabled="isDetails"
        type="text"
        v-model="localParamsList[$index].name"
        maxlength="256"
        @on-blur="_verifProp()"
        placeholder=""
        :style="inputStyle">
      </x-input>
      <span>type:</span>
      <x-input
        :disabled="isDetails"
        type="text"
        v-model="localParamsList[$index].type"
        @on-blur="_verifProp()"
        placeholder=""
        style="width: 120px">
      </x-input>
      <span class="lt-add">
        <a href="javascript:" style="color:red;" @click="!isDetails && _removeUdp($index)" >
          <em class="ans-icon-trash" :class="_isDetails" data-toggle="tooltip" :title="$t('delete')" ></em>
        </a>
      </span>
      <span class="add" v-if="$index === (localParamsList.length - 1)">
        <a href="javascript:" @click="!isDetails && _addUdp()" >
          <em class="iconfont ans-icon-increase" :class="_isDetails" data-toggle="tooltip" :title="$t('Add')"></em>
        </a>
      </span>
    </div>
    <span class="add-dp" v-if="!localParamsList.length">
      <a href="javascript:" @click="!isDetails && _addColumns()" >
        <em class="iconfont ans-icon-increase" :class="_isDetails" data-toggle="tooltip" :title="$t('Add')"></em>
      </a>
    </span>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import { columnTypeList } from './commcon'
  import disabledState from '@/module/mixin/disabledState'
  export default {
    name: 'user-def-columns',
    data () {
      return {
        // Type data Custom parameter type support OUT
        columnTypeList: columnTypeList,
        // Increased data
        localParamsList: [],
        id: '',
        table: '',
        // Current execution index
        localParamsIndex: null
      }
    },
    mixins: [disabledState],
    props: {
      udpList: Array,
      // hide direct/type
      hide: {
        type: Boolean,
        default: true
      },
      dataSource: String,
      dataTable: String,
    },
    methods: {
      /**
       * Current index
       */
      _getIndex (index) {
        this.localParamsIndex = index
      },
      /**
       * handle direct
       */
      _handleDirectChanged () {
        this._verifProp('value')
      },
      /**
       * handle type
       */
      _handleTypeChanged () {
        this._verifProp('value')
      },
      /**
       * delete item
       */
      _removeUdp (index) {
        this.localParamsList.splice(index, 1)
        this._verifProp('value')
      },
      _verification(){
          if(!this.id){
            this.$message.warning(`${i18n.$t('Please choose a datasource(required)')}`)
            return false
          }
          if(!this.table){
            this.$message.warning(`${i18n.$t('Please enter a table(required)')}`)
            return false
          }
          return true
      },
      _rtParam(){
        return {
          id: this.id,
          table: this.table
        }
      },
      /**
       * get columns
       */
      _addColumns(){
          if(this._verification()){
            let param = this._rtParam()
              //请求接口
            this.store.dispatch('datasource/getDatasourceColumns',param).then(res=>{
              for (const item of res) {
                this.localParamsList.push({
                  name: item['name'],
                  type: item['type'],
                })
              }
            }).catch(e=>{
              this.$message.error(e.msg || '')
            })
          }
      },
      /**
       * add
       */
      _addUdp () {
        this.localParamsList.push({
          name: '',
          type: ''
        })
      },
      /**
       * blur verification
       */
      _handleValue () {
        this._verifProp('value')
      },
      /**
       * Verify that the value exists or is empty
       */
      _verifProp (type) {
        let arr = []
        let flag = true
        _.map(this.localParamsList, v => {
          arr.push(v.name)
          if (!v.name) {
            flag = false
          }
        })
        if (!flag) {
          if (!type) {
            this.$message.warning(`${i18n.$t('prop is empty')}`)
          }
          return false
        }
        let newArr = _.cloneDeep(_.uniqWith(arr, _.isEqual))
        if (newArr.length !== arr.length) {
          if (!type) {
            this.$message.warning(`${i18n.$t('prop is repeat')}`)
          }
          return false
        }
        this.$emit('on-local-columns', _.cloneDeep(this.localParamsList))
        return true
      }
    },
    watch: {
      // Monitor data changes
      udpList () {
        this.localParamsList = this.udpList
      },
      dataSource(){
        this.id = this.dataSource
      },
      dataTable(){
        this.table = this.dataTable
      }

    },
    created () {
      this.localParamsList = this.udpList
      this.id = this.dataSource
      this.table = this.dataTable
    },
    computed: {
      inputStyle () {
        return `width:${this.hide ? 160 : 262}px`
      }
    },
    mounted () {
    },
    components: { }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .user-def-columns-model {
    .select-listpp {
      margin-bottom: 6px;
      .lt-add {
        padding-left: 4px;
        a {
          .iconfont {
            font-size: 18px;
            vertical-align: middle;
            margin-bottom: -2px;
            display: inline-block;
          }
        }
      }
    }
    .add {
      a {
        color: #000;
        .iconfont {
          font-size: 16px;
          vertical-align: middle;
          display: inline-block;
          margin-top: -5px;
        }
      }
    }
    .add-dp{
      a {
        color: #0097e0;
        .iconfont {
          font-size: 18px;
          vertical-align: middle;
          display: inline-block;
          margin-top: 2px;
        }
      }
    }
  }
</style>
