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
  <div class="popup-model">
    <div class="top-p">
      <span>{{nameText}}</span>
    </div>
    <div class="content-p">
      <slot name="content"></slot>
    </div>
    <div class="bottom-p">
      <x-button type="text" shape="circle" @click="close()" :disabled="disabled"> {{$t('Cancel')}} </x-button>
      <x-button type="primary" shape="circle" :loading="spinnerLoading" @click="ok()" :disabled="disabled || apDisabled">{{spinnerLoading ? 'Loading...' : okText}} </x-button>
    </div>
  </div>
</template>
<script>
  import i18n from '@/module/i18n'
  export default {
    name: 'popup',
    data () {
      return {
        spinnerLoading: false,
        apDisabled: false
      }
    },
    props: {
      nameText: {
        type: String,
        default: `${i18n.$t('Create')}`
      },
      okText: {
        type: String,
        default: `${i18n.$t('Confirm')}`
      },
      disabled: {
        type: Boolean,
        default: false
      },
      asynLoading: {
        type: Boolean,
        default: false
      }
    },
    methods: {
      close () {
        this.$emit('close')
        this.$modal.destroy()
      },
      ok () {
        if (this.asynLoading) {
          this.spinnerLoading = true
          this.$emit('ok', () => {
            this.spinnerLoading = false
          })
        } else {
          this.$emit('ok')
        }
      }
    },
    components: { }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .popup-model {
    background: #fff;
    border-radius: 3px;

    .top-p {
      height: 70px;
      line-height: 70px;
      border-radius: 3px 3px 0 0;
      padding: 0 20px;
      >span {
        font-size: 20px;
      }
    }
    .bottom-p {
      text-align: right;
      height: 72px;
      line-height: 72px;
      border-radius:  0 0 3px 3px;
      padding: 0 20px;
    }
    .content-p {
      min-width: 520px;
      min-height: 100px;
    }
  }
</style>
