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
  <div class="user-def-params-model">
    <div class="select-listpp"
         v-for="(item,$index) in httpParamsList"
         :key="item.id"
         @click="_getIndex($index)">
      <x-input
        :disabled="isDetails"
        type="text"
        v-model="httpParamsList[$index].prop"
        :placeholder="$t('prop(required)')"
        @on-blur="_verifProp()"
        :style="inputStyle">
      </x-input>
      <x-select
        @change="_handlePositionChanged"
        v-model="httpParamsList[$index].httpParametersType"
        :placeholder="$t('Http Parameters Position')"
        :disabled="isDetails"
        :style="inputStyle"
      >
        <x-option
          v-for="position in positionList"
          :key="position.code"
          :value="position.id"
          :label="position.code">
        </x-option>
      </x-select>
      <x-input
        :disabled="isDetails"
        type="text"
        v-model="httpParamsList[$index].value"
        :placeholder="$t('value(required)')"
        @on-blur="_handleValue()"
        :style="inputStyle">
      </x-input>
      <span class="lt-add">
        <a href="javascript:" style="color:red;" @click="!isDetails && _removeUdp($index)" >
          <em class="ans-icon-trash" :class="_isDetails" data-toggle="tooltip" :title="$t('delete')" ></em>
        </a>
      </span>
      <span class="add" v-if="$index === (httpParamsList.length - 1)">
        <a href="javascript:" @click="!isDetails && _addUdp()" >
          <em class="ans-icon-increase" :class="_isDetails" data-toggle="tooltip" :title="$t('Add')"></em>
        </a>
      </span>
    </div>
    <span class="add-dp" v-if="!httpParamsList.length">
      <a href="javascript:" @click="!isDetails && _addUdp()" >
        <em class="iconfont ans-icon-increase" :class="_isDetails" data-toggle="tooltip" :title="$t('Add')"></em>
      </a>
    </span>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import { positionList } from './commcon'
  import disabledState from '@/module/mixin/disabledState'
  export default {
    name: 'http-params',
    data () {
      return {
        // Increased data
        httpParamsList: [],
        // Current execution index
        httpParamsIndex: null,
        // 参数位置的下拉框
        positionList:positionList
      }
    },
    mixins: [disabledState],
    props: {
      udpList: Array,
      // hide direct/type
      hide: {
        type: Boolean,
        default: true
      }
    },
    methods: {
      /**
       * Current index
       */
      _getIndex (index) {
        this.httpParamsIndex = index
      },
      /**
       * 获取参数位置
       */
      _handlePositionChanged () {
        this._verifProp('value')
      },
      /**
       * delete item
       */
      _removeUdp (index) {
        this.httpParamsList.splice(index, 1)
        this._verifProp('value')
      },
      /**
       * add
       */
      _addUdp () {
        this.httpParamsList.push({
          prop: '',
          httpParametersType: 'PARAMETER',
          value: ''
        })
      },
      /**
       * blur verification
       */
      _handleValue () {
        this._verifValue('value')
      },
      /**
       * Verify that the value exists or is empty
       */
      _verifProp (type) {
        let arr = []
        let flag = true
        _.map(this.httpParamsList, v => {
          arr.push(v.prop)
          if (!v.prop) {
            flag = false
          }
          if(v.value === ''){
            this.$message.warning(`${i18n.$t('value is empty')}`)
            return false
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
        this.$emit('on-http-params', _.cloneDeep(this.httpParamsList))
        return true
      },
      _verifValue (type) {
        let arr = []
        let flag = true
        _.map(this.httpParamsList, v => {
          arr.push(v.value)
          if (!v.value) {
            flag = false
          }
        })
        if (!flag) {
            this.$message.warning(`${i18n.$t('value is empty')}`)
          return false
        }
        this.$emit('on-http-params', _.cloneDeep(this.httpParamsList))
        return true
      }
    },
    watch: {
      // Monitor data changes
      udpList () {
        this.httpParamsList = this.udpList
      }
    },
    created () {
      this.httpParamsList = this.udpList
    },
    computed: {
      inputStyle () {
        return "width:30%"
      }
    },
    mounted () {
    },
    components: { }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .user-def-params-model {
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

