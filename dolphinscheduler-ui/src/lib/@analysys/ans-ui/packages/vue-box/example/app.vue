<template>
  <div>
    <section class="demo-section">
      <h4>modal</h4>
      <div>
        <x-button type="primary" @click="handleModal">Modal Dialog</x-button>
        <x-button type="primary" @click="handleAbstract">Abstract Modal Dialog</x-button>
      </div>
    </section>
    <section class="demo-section">
      <h4>message</h4>
      <div>
        <x-button type="primary" @click="info">消息</x-button>
        <x-button type="success" @click="success">成功</x-button>
        <x-button type="warning" @click="warning">警告</x-button>
        <x-button type="error" @click="error">错误</x-button>
        <x-button type="primary" @click="loading">Get...</x-button>
        <x-button @click="loading">Loading</x-button>
      </div>
    </section>
    <section class="demo-section">
      <h4>notice</h4>
      <div>
        <x-button type="primary" @click="infoNotice">消息</x-button>
        <x-button type="success" @click="successNotice">成功</x-button>
        <x-button type="warning" @click="warningNotice">警告</x-button>
        <x-button type="error" @click="errorNotice">错误</x-button>
        <x-button type="primary" @click="loadingNotice">Get...</x-button>
      </div>
    </section>
  </div>
</template>

<script>
import Vue from 'vue'
import { xButton } from '../../vue-button/src'
import { xModal, xMessage, xNotice } from '../src'

Vue.$message = Vue.prototype.$message = xMessage
Vue.$modal = Vue.prototype.$modal = xModal
Vue.$notice = Vue.prototype.$notice = xNotice

const ModalTest = {
  data () {
    return {
      name: 1
    }
  },
  methods: {
    handleAbstract () {
      this.$modal.dialog({
        title: '你好',
        width: 200,
        escClose: true,
        closable: false,
        showMask: true,
        className: 'x-modal-custom',
        maskClosable: true,
        render (h) {
          return (
            <div class='customize-dialog'>This is a abstract modal dialog.</div>
          )
        }
      })
    },
    handleModal () {
      let self = this
      this.$modal.dialog({
        className: 'x-modal-custom',
        width: 350,
        closable: true,
        showMask: true,
        maskClosable: true,
        title: '你好',
        content: 'hello word' + (this.name++),
        ok: {
          show: true,
          className: 'x-btn-ok',
          handle (e) {
            self.$notice.success({
              title: 'Success',
              content: 'ok',
              duration: 2,
              onClose () {},
              closable: false
            })
            console.log('ok event handled.', e)
          }
        },
        cancel: {
          handle (e) {
            self.$notice.info({
              title: 'Canceled',
              content: 'cancel',
              duration: 2,
              onClose () {},
              closable: false
            })
            console.log('cancel event handled.', e)
          }
        }
      })
    }
  }
}

export default {
  mixins: [ ModalTest ],
  components: { xButton },
  data () {
    return {
      name: 1
    }
  },
  methods: {
    info () {
      this.$message.info({
        content: 'hello word' + (this.name++),
        duration: 2,
        onClose: function () {
        },
        closable: false
      })
    },
    infoNotice () {
      this.$notice.info({
        title: '标题',
        content: 'hello word' + (this.name++),
        duration: 0,
        onClose: function () {
        },
        closable: true
      })
    },
    success () {
      this.$message.success({
        content: 'hello word' + (this.name++),
        duration: 2,
        onClose: function () {
        },
        closable: false
      })
    },
    successNotice () {
      this.$notice.success({
        title: '成功',
        content: 'hello word' + (this.name++),
        duration: 2,
        onClose: function () {
        },
        closable: false
      })
    },
    error () {
      this.$message.error({
        content: 'hello wordhello wordhello word' + (this.name++),
        duration: 0,
        onClose: function () {
        },
        closable: true
      })
    },
    errorNotice () {
      this.$notice.error({
        title: '错误',
        content: 'hello wordhello wordhello word' + (this.name++),
        duration: 0,
        onClose: function () {
        },
        closable: true
      })
    },
    loading () {
      this.$message.loading({
        content: 'hello word' + (this.name++),
        duration: 2,
        onClose: function () {
        },
        closable: true
      })
    },
    loadingNotice () {
      this.$notice.loading({
        title: '加载中',
        content: 'hello word' + (this.name++),
        duration: 2,
        onClose: function () {
        },
        closable: true
      })
    },
    warning () {
      this.$message.warning({
        content: 'hello word' + (this.name++),
        duration: 2,
        onClose: function () {
        },
        closable: false
      })
    },
    warningNotice () {
      this.$notice.warning({
        title: '警告',
        content: 'hello word' + (this.name++),
        duration: 2,
        onClose: function () {
        },
        closable: false
      })
    }
  }
}
</script>

<style lang="scss">
.customize-dialog {
  padding: 10px;
  background: #ccc;
  border-radius: 4px;
}
</style>
