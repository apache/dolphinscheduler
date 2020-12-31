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
  <div class="form-model-wrapper" v-clickoutside="_handleClose">
    <div class="title-box">
      <span class="name">{{$t('Current connection settings')}}</span>
    </div>
    <div class="content-box">
      <div class="form-model">
        <!-- Node name -->
        <div class="clearfix list">
          <div class="text-box"><span>{{$t('Connection name')}}</span></div>
          <div class="cont-box">
            <label class="label-box">
              <el-input
                type="text"
                size="small"
                v-model="labelName"
                :disabled="isDetails"
                :placeholder="$t('Please enter name')"
                maxlength="100">
              </el-input>
            </label>
          </div>
        </div>
      </div>
    </div>
    <div class="bottom-box">
      <div class="submit" style="background: #fff;">
        <el-button type="text" size="small" @click="cancel()"> {{$t('Cancel')}} </el-button>
        <el-button type="primary" size="small" round :loading="spinnerLoading" @click="ok()" :disabled="isDetails">{{spinnerLoading ? 'Loading...' : $t('Confirm add')}} </el-button>
      </div>
    </div>
  </div>
</template>
<script>
  import disabledState from '@/module/mixin/disabledState'

  export default {
    name: 'form-line-model',
    data () {
      return {
        // loading
        spinnerLoading: false,
        // node name
        labelName: ''
      }
    },
    mixins: [disabledState],
    props: {
      lineData: Object
    },
    methods: {
      cancel () {
        this.$emit('cancel', {
          fromThis: this
        })
      },
      ok () {
        if ($(`#${this.lineData.id}`).prev().attr('class') === 'jtk-overlay') {
          $(`#${this.lineData.id}`).prev().empty()
        }
        $(`#${this.lineData.id}`).text(this.labelName)
        this.$emit('addLineInfo', {
          item: {
            labelName: this.labelName,
            sourceId: this.lineData.sourceId,
            targetId: this.lineData.targetId
          },
          fromThis: this
        })
      }
    },
    watch: {

    },
    created () {
      if ($(`#${this.lineData.id}`).prev().attr('class').indexOf('jtk-overlay') !== -1) {
        this.labelName = $(`#${this.lineData.id}`).prev().text()
      } else {
        this.labelName = $(`#${this.lineData.id}`).text()
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
