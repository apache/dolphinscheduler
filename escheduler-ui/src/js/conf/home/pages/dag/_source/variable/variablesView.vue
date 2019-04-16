<template>
  <div class="variable-model">
      <div class="list">
        <div class="name"><i class="fa fa-code"></i><b style="padding-top: 3px;display: inline-block">{{$t('Global parameters')}}</b></div>
        <div class="var-cont">
          <x-button size="xsmall" type="ghost" v-for="(item,$index) in list.globalParams"  @click="_copy('gbudp-' + $index)" :data-clipboard-text="item.prop + ' = ' +item.value" :class="'gbudp-' + $index"><b style="color: #2A455B;">{{item.prop}}</b> = {{item.value}}</x-button>
        </div>
      </div>
      <div class="list" style="height: 30px;">
        <div class="name"><i class="fa fa-code"></i><b style="padding-top: 3px;display: inline-block">{{$t('Local parameters')}}</b></div>
        <div class="var-cont">
          &nbsp;
        </div>
      </div>
      <div class="list list-t" v-for="(item,key,$index) in list.localParams">
        <div class="task-name">Task({{$index}})：{{key}}</div>
        <div class="var-cont" v-if="item.length">
          <template v-for="(el,index) in item">
            <x-button size="xsmall" type="ghost" @click="_copy('copy-part-' + index)" :data-clipboard-text="_rtClipboard(el)" :class="'copy-part-' + index">
              <span v-for="(e,k,i) in el"><b style="color: #2A455B;">{{k}}</b> = {{e}} </span>
            </x-button>
          </template>
        </div>
      </div>
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
      _rtClipboard (el) {
        let arr = []
        Object.keys(el).forEach((key) => {
          arr.push(`${key}=${el[key]}`)
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
