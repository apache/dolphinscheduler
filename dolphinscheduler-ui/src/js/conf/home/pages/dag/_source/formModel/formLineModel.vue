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
  <div class="form-model-model" v-clickoutside="_handleClose">
    <div class="title-box">
      <span class="name">{{$t('Current connection settings')}}</span>
    </div>
    <div class="content-box">
      <div class="from-model">
        <!-- Node name -->
        <div class="clearfix list">
          <div class="text-box"><span>{{$t('Connection name')}}</span></div>
          <div class="cont-box">
            <label class="label-box">
              <x-input
                type="text"
                v-model="labelName"
                :disabled="isDetails"
                :placeholder="$t('Please enter name')"
                maxlength="100"
                autocomplete="off">
              </x-input>
            </label>
          </div>
        </div>
      </div>
    </div>
    <div class="bottom-box">
      <div class="submit" style="background: #fff;">
        <x-button type="text" @click="cancel()"> {{$t('Cancel')}} </x-button>
        <x-button type="primary" shape="circle" :loading="spinnerLoading" @click="ok()" :disabled="isDetails">{{spinnerLoading ? 'Loading...' : $t('Confirm add')}} </x-button>
      </div>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import i18n from '@/module/i18n'
  import JSP from './../plugIn/jsPlumbHandle'
  import disabledState from '@/module/mixin/disabledState'

  export default {
    name: 'form-line-model',
    data () {
      return {
          // loading
        spinnerLoading: false,
        // node name
        labelName: '',
      }
    },
    mixins: [disabledState],
    props: {
      id: String,
      sourceId: String,
      targetId: String
    },
    methods: {
        cancel() {
            this.$emit('cancel', {
                fromThis: this
            })
        },
        ok() {
          if($(`#${this.id}`).prev().attr('class')==='jtk-overlay') {
            $(`#${this.id}`).prev().empty()
          }
          $(`#${this.id}`).text(this.labelName)
            this.$emit('addLineInfo', {
              item: {
                labelName: this.labelName,
                sourceId: this.sourceId,
                targetId: this.targetId
              },
              fromThis: this
            })
        }
    }, 
    watch: {
      
    },
    created () {
      if($(`#${this.id}`).prev().attr('class').indexOf('jtk-overlay')!==-1) {
        this.labelName = $(`#${this.id}`).prev().text()
      } else {
        this.labelName = $(`#${this.id}`).text()
      }
    },
    mounted () {
      
    },
    updated () {
    },
    beforeDestroy () {
    },
    destroyed () {
    },
    computed: {
      
    },
    components: {}
  }
</script>

<style lang="scss" rel="stylesheet/scss">

</style>
