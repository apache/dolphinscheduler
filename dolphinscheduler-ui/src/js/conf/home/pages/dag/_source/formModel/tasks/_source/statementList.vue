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
  <div class="statement-list-model">
    <div class="select-listpp"
         v-for="(item,$index) in localStatementList"
         :key="item.id"
         @click="_getIndex($index)">
      <x-input
        :disabled="isDetails"
        type="textarea"
        resize="none"
        :autosize="{minRows:1}"
        :placeholder="$t('Please enter a non-query SQL statement')"
        v-model="localStatementList[$index]"
        @on-blur="_verifProp()"
        style="width: 525px;">
      </x-input>
      <span class="lt-add">
        <a href="javascript:" style="color:red;" @click="!isDetails && _removeStatement($index)" >
          <em class="ans-icon-trash" :class="_isDetails" data-toggle="tooltip" :title="$t('delete')" ></em>
        </a>
      </span>
      <span class="add" v-if="$index === (localStatementList.length - 1)">
        <a href="javascript:" @click="!isDetails && _addStatement()" >
          <em class="iconfont ans-icon-increase" :class="_isDetails" data-toggle="tooltip" :title="$t('Add')"></em>
        </a>
      </span>
    </div>
    <span class="add" v-if="!localStatementList.length">
      <a href="javascript:" @click="!isDetails && _addStatement()" >
        <em class="iconfont ans-icon-increase" :class="_isDetails" data-toggle="tooltip" :title="$t('Add')"></em>
      </a>
    </span>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import disabledState from '@/module/mixin/disabledState'
  export default {
    name: 'user-def-statements',
    data () {
      return {
        // Increased data
        localStatementList: [],
        // Current execution index
        localStatementIndex: null
      }
    },
    mixins: [disabledState],
    props: {
      statementList: Array
    },
    methods: {
      /**
       * Current index
       */
      _getIndex (index) {
        this.localStatementIndex = index
      },
      /**
       * delete item
       */
      _removeStatement (index) {
        this.localStatementList.splice(index, 1)
        this._verifProp('value')
      },
      /**
       * add
       */
      _addStatement () {
        this.localStatementList.push('')
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
        _.map(this.localStatementList, v => {
          arr.push(v)
          if (!v) {
            flag = false
          }
        })
        if (!flag) {
          if (!type) {
            this.$message.warning(`${i18n.$t('Statement cannot be empty')}`)
          }
          return false
        }

        this.$emit('on-statement-list', _.cloneDeep(this.localStatementList))
        return true
      }
    },
    watch: {
      // Monitor data changes
      statementList () {
        this.localStatementList = this.statementList
      }
    },
    created () {
      this.localStatementList = this.statementList
    },
    mounted () {
    },
    components: { }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .statement-list-model {
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
        .iconfont {
          font-size: 16px;
          vertical-align: middle;
          display: inline-block;
          margin-top: -5px;
        }
      }
    }
  }
</style>
