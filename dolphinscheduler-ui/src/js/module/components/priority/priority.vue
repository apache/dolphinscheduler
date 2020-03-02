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
  <div class="priority-model">
    <x-select @on-change="_onChange" v-model="value" style="width: 130px;" :disabled="isDetails">
      <div slot="trigger" slot-scope="{ selectedModel }" class="ans-input" >
        <div class="input-element" :class="isDetails?'disabled' : ''">
          <span v-html="_rtUnicode(selectedModel ? selectedModel.label : 'MEDIUM')"></span>
          <span class="label-p">{{selectedModel ? selectedModel.label : 'MEDIUM'}}</span>
          <em class="ans-icon-arrow-down" ></em>
        </div>
      </div>
      <x-option
              v-for="item in priorityList"
              :key="item.code"
              :value="item.code"
              :label="item.code">
        <li class="ans-option ans-option-listp">
          <span class="default-option-class">
            <em :class="item.unicode" :style="{color:item.color}"></em>
            {{item.code}}
          </span>
        </li>
      </x-option>
    </x-select>
  </div>
</template>
<script>
  import _ from 'lodash'
  import disabledState from '@/module/mixin/disabledState'

  export default {
    name: 'priority',
    data () {
      return {
        priorityList: [
          {
            code: 'HIGHEST',
            unicode: 'ans-icon-line-arrow-up',
            color: '#ff0000'
          },
          {
            code: 'HIGH',
            unicode: 'ans-icon-line-arrow-up',
            color: '#ff0000'
          },
          {
            code: 'MEDIUM',
            unicode: 'ans-icon-line-arrow-up',
            color: '#EA7D24'
          },
          {
            code: 'LOW',
            unicode: 'ans-icon-line-arrow-down',
            color: '#2A8734'
          },
          {
            code: 'LOWEST',
            unicode: 'ans-icon-line-arrow-down',
            color: '#2A8734'
          }
        ]
      }
    },
    props: {
      value: {
        type: String,
        default: 'MEDIUM'
      }
    },
    mixins: [disabledState],
    model: {
      prop: 'value',
      event: 'priorityEvent'
    },
    methods: {
      _rtUnicode (value) {
        let o = _.find(this.priorityList, ['code', value])
        return `<em class="${o.unicode}" style="color:${o.color}"></em>`
      },
      _onChange (o) {
        this.value = o.value
        this.$emit('priorityEvent', o.value)
      }
    },
    created () {
    },
    mounted () {
    },
    components: {}
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .priority-model {
    display: inline-block;
    .ans-option-listp {
      >span {
        font-weight: normal;
        >.iconfont {
          padding-right: 2px;
        }
      }
    }
    .ans-input {
      .input-element {
        cursor: pointer;
        height: 32px;
        line-height: 30px;
        position: relative;
        font-weight: normal;
        .ans-icon-arrow-down {
          position: absolute;
          right: 8px;
          top: 0;
          font-size: 12px;
          color: #888;
          cursor: pointer;
        }
        span {
          vertical-align: middle;
          &.label-p {
            margin-top: -4px;
            display: inline-block;
          }
        }
      }
    }
  }
</style>
