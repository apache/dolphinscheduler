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
  <div class="variable-model">
    <template v-if="list">
      <div class="list">
        <div class="name"><em class="fa ans-icon-code"></em><strong style="padding-top: 3px;display: inline-block">{{$t('Global parameters')}}</strong></div>
        <div class="var-cont">
          <template v-for="(item,$index) in list.globalParams">
            <x-button
                    size="xsmall"
                    type="ghost"
                    @click="_copy('gbudp-' + $index)"
                    :key="$index"
                    :data-clipboard-text="item.prop + ' = ' +item.value"
                    :class="'gbudp-' + $index">
              <strong style="color: #2A455B;">{{item.prop}}</strong> = {{item.value}}
            </x-button>
          </template>
        </div>
      </div>
      <div class="list" style="height: 30px;">
        <div class="name"><em class="fa ans-icon-code"></em><strong style="padding-top: 3px;display: inline-block">{{$t('Local parameters')}}</strong></div>
        <div class="var-cont">
          &nbsp;
        </div>
      </div>
      <div class="list list-t" v-for="(item,key,$index) in list.localParams" :key="$index">
        <div class="task-name">Task({{$index}})：{{key}}</div>
        <div class="var-cont" v-if="item.localParamsList.length">
          <template v-for="(el,index) in item.localParamsList">
            <x-button size="xsmall" type="ghost" :key="index" @click="_copy('copy-part-' + index)" :data-clipboard-text="_rtClipboard(el,item.taskType)" :class="'copy-part-' + index">
              <span v-for="(e,k,i) in el" :key="i">
                <template v-if="item.taskType === 'SQL' || item.taskType === 'PROCEDURE'">
                  <template v-if="(k !== 'direct' && k !== 'type')">
                    <strong style="color: #2A455B;">{{k}}</strong> = {{e}}
                  </template>
                </template>
                <template v-else>
                  <strong style="color: #2A455B;">{{k}}</strong> = {{e}}
                </template>
              </span>
            </x-button>
          </template>
        </div>
      </div>
    </template>
  </div>
</template>
<script>
  import i18n from '@/module/i18n'
  import { mapActions } from 'vuex'
  import Clipboard from 'clipboard'

  export default {
    name: 'variables-view',
    data () {
      return {
        list: {}
      }
    },
    props: {},
    methods: {
      ...mapActions('dag', ['getViewvariables']),
      /**
       * Get variable data
       */
      _getViewvariables () {
        this.getViewvariables({
          processInstanceId: this.$route.params.id
        }).then(res => {
          this.list = res.data
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      /**
       * Click to copy
       */
      _copy (className) {
        let clipboard = new Clipboard(`.${className}`)
        clipboard.on('success', e => {
          this.$message.success(`${i18n.$t('Copy success')}`)
          // Free memory
          clipboard.destroy()
        })
        clipboard.on('error', e => {
          // Copy is not supported
          this.$message.warning(`${i18n.$t('The browser does not support automatic copying')}`)
          // Free memory
          clipboard.destroy()
        })
      },
      /**
       * Copyed text processing
       */
      _rtClipboard (el, taskType) {
        let arr = []
        Object.keys(el).forEach((key) => {
          if (taskType === 'SQL' || taskType === 'PROCEDURE') {
            if (key !== 'direct' && key !== 'type') {
              arr.push(`${key}=${el[key]}`)
            }
          } else {
            arr.push(`${key}=${el[key]}`)
          }
        })
        return arr.join(' ')
      }
    },
    watch: {},
    created () {
      this._getViewvariables()
    },
    mounted () {
    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .variable-model {
    padding: 10px;
    padding-bottom: 5px;
    .list {
      position: relative;
      min-height: 30px;
      .var-cont {
        padding-left: 19px;
        >button {
          margin-bottom: 6px;
          margin-right: 6px;
        }
      }
      .name {
        padding-bottom: 10px;
        font-size: 16px;
        >.fa {
          font-size: 16px;
          color: #0097e0;
          margin-right: 4px;
          vertical-align: middle;
          font-weight: bold;
        }
        >b {
          vertical-align: middle;
        }
      }
      >span{
        height: 28px;
        line-height: 28px;
        padding: 0 8px;
        background: #2d8cf0;
        display: inline-block;
        margin-bottom: 8px;
        color: #fff;
      }
    }
    .list-t {
      padding-left: 0px;
      margin-bottom: 10px;
      .task-name {
        padding-left: 19px;
        padding-bottom: 8px;
        font-size: 12px;
        font-weight: bold;
        color: #0097e0;
      }
    }
  }
</style>
