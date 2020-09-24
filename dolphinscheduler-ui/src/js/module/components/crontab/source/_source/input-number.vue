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
  <div class="input-number-model">
    <x-button type="ghost" @click="onReduce()" :disabled="(value < (min + 1))"><span class="bt-text">-</span></x-button>
    <x-input v-model="value" placeholder=" " @on-blur="onBlur"></x-input>
    <x-button type="ghost" @click="onIncrease()" :disabled="(value > (max - 1))"><span class="bt-text">+</span></x-button>
  </div>
</template>
<script>
  export default {
    name: 'input-number',
    data () {
      return {
        value: 1,
        isIncrease: false,
        isReduce: false
      }
    },
    props: {
      min: {
        type: Number,
        default: 0
      },
      max: {
        type: Number,
        default: 10
      },
      propsValue: Number
    },
    methods: {
      onBlur () {
        let $reg = /^\+?[1-9][0-9]*$/　　// eslint-disable-line
        let $val = this.value
        // if (parseInt($val) >= this.min || parseInt($val) <= this.max) {
        //   return
        // }
        // Verify integer
        if (!$reg.test($val)) {
          this.value = this.min
        }
        // Maximum value
        if (this.value > this.max) {
          this.value = this.max
        }
        // minimum value
        if (this.min > this.value) {
          this.value = this.min
        }
        this.$emit('on-number', this.value)
      },
      onIncrease () {
        this.value = parseInt(this.value) + 1
        this.$emit('on-number', this.value)
      },
      onReduce () {
        this.value = parseInt(this.value) - 1
        this.$emit('on-number', this.value)
      }
    },
    watch: {
    },
    beforeCreate () {
    },
    created () {
      this.value = this.propsValue ? this.propsValue : this.min
    },
    beforeMount () {
    },
    mounted () {
    },
    beforeUpdate () {
    },
    updated () {
    },
    beforeDestroy () {
    },
    destroyed () {
    },
    computed: {},
    components: {}
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .input-number-model {
    display: inline-block;
    button{
      background: #f5f7fa;
      padding: 8px 10px;
      position: relative;
      .bt-text {
        font-size: 18px;
        color: #888;
      }
    }
    .ans-input {
      width: 80px;
      margin:0 -2px 0 -1px;
      input {
        text-align: center;
      }
    }
    button,input{
      vertical-align: middle;
    }
  }
</style>
