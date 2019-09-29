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
