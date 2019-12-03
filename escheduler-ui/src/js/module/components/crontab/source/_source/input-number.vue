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
        // 验证整数
        if (!$reg.test($val)) {
          this.value = this.min
        }
        // 最大值
        if (this.value > this.max) {
          this.value = this.max
        }
        // 最小值
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
      width: 60px;
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
