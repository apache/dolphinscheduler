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
         v-for="(item,$index) in localParamsList"
         :key="item.id"
         @click="_getIndex($index)">
      <el-input
              :disabled="isDetails"
              type="text"
              size="small"
              v-model="localParamsList[$index].prop"
              :placeholder="$t('prop(required)')"
              maxlength="256"
              @blur="_verifProp()"
              :style="inputStyle">
      </el-input>
      <template v-if="hide">
        <el-select
                style="width: 80px;"
                size="small"
                @change="_handleDirectChanged"
                v-model="localParamsList[$index].direct"
                :disabled="isDetails">
          <el-option
                  v-for="city in directList"
                  :key="city.code"
                  :value="city.code"
                  :label="city.code">
          </el-option>
        </el-select>
        <el-select
                style="width: 118px;"
                size="small"
                @change="_handleTypeChanged"
                v-model="localParamsList[$index].type"
                :disabled="isDetails">
          <el-option
                  v-for="city in typeList"
                  :key="city.code"
                  :value="city.code"
                  :label="city.code">
          </el-option>
        </el-select>
      </template>
      <el-input
              :disabled="isDetails && !isStartProcess"
              type="text"
              size="small"
              v-model="localParamsList[$index].value"
              :placeholder="$t('value(optional)')"
              maxlength="256"
              @blur="_handleValue()"
              :style="inputStyle">
      </el-input>
      <span class="lt-add" v-show="!isStartProcess">
        <a href="javascript:" style="color:red;" @click="!isDetails && _removeUdp($index)" >
          <em class="el-icon-delete" :class="_isDetails" data-toggle="tooltip" :title="$t('delete')" ></em>
        </a>
      </span>
      <span class="add" v-if="$index === (localParamsList.length - 1)" v-show="!isStartProcess">
        <a href="javascript:" @click="!isDetails && _addUdp()" >
          <em class="el-icon-circle-plus-outline" :class="_isDetails" data-toggle="tooltip" :title="$t('Add')"></em>
        </a>
      </span>
    </div>
    <span class="add-dp" v-if="!localParamsList.length" v-show="!isStartProcess">
      <a href="javascript:" @click="!isDetails && _addUdp()" >
        <em class="iconfont el-icon-circle-plus-outline" :class="_isDetails" data-toggle="tooltip" :title="$t('Add')"></em>
      </a>
    </span>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import { directList, typeList } from './commcon'
  import disabledState from '@/module/mixin/disabledState'
  export default {
    name: 'user-def-params',
    data () {
      return {
        // Direct data Custom parameter type support IN
        directList: directList,
        // Type data Custom parameter type support OUT
        typeList: typeList,
        // Increased data
        localParamsList: [],
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
      isStartProcess: {
        type: Boolean,
        default: false
      }
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
      /**
       * add
       */
      _addUdp () {
        this.localParamsList.push({
          prop: '',
          direct: 'IN',
          type: 'VARCHAR',
          value: ''
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
          arr.push(v.prop)
          if (!v.prop) {
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
        this.$emit('on-local-params', _.cloneDeep(this.localParamsList))
        return true
      }
    },
    watch: {
      // Monitor data changes
      udpList () {
        this.localParamsList = this.udpList
      }
    },
    created () {
      this.localParamsList = this.udpList
    },
    computed: {
      inputStyle () {
        return `width:${this.hide ? 160 : 252}px`
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
        line-height: 32px;
        a {
          .iconfont, [class^="el-icon"] {
            font-size: 17px;
            vertical-align: middle;
            display: inline-block;
            margin-top: 0;
          }
        }
      }
    }
    .add {
      line-height: 32px;
      a {
        color: #000;
        .iconfont, [class^="el-icon"] {
          font-size: 18px;
          vertical-align: middle;
          display: inline-block;
          margin-top: 0;
        }
      }
    }
    .add-dp {
      a {
        color: #0097e0;
        .iconfont, [class^="el-icon"] {
          font-size: 18px;
          vertical-align: middle;
          display: inline-block;
          margin-top: 2px;
        }
      }
    }
  }
</style>
