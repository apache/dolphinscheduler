<template>
  <div class="user-def-params-model">
    <div class="select-listpp"
         v-for="(item,$index) in localParamsList"
         :key="item.id"
         @click="_getIndex($index)">
      <x-input
          :disabled="isDetails"
          type="text"
          v-model="localParamsList[$index].prop"
          :placeholder="$t('prop(required)')"
          @on-blur="_verifProp()"
          :style="inputStyle">
      </x-input>
      <x-select
                @change="_handlePositionChanged"
                v-model="localParamsList[$index].direct"
                :placeholder="$t('Http Parameters Position')"
                :disabled="isDetails"
                :style="inputStyle"
                >
          <x-option
                  v-for="position in positionList"
                  :key="position.code"
                  :value="position.id"
                  :label="position.code">
          </x-option>
      </x-select>
      <x-input
          :disabled="isDetails"
          type="text"
          v-model="localParamsList[$index].value"
          :placeholder="$t('value(optional)')"
          @on-blur="_verifProp()"
          :style="inputStyle">
      </x-input>
      <span class="lt-add">
        <a href="javascript:" style="color:red;" @click="!isDetails && _removeUdp($index)" >
          <i class="iconfont" :class="_isDetails" data-toggle="tooltip" :title="$t('delete')" >&#xe611;</i>
        </a>
      </span>
      <span class="add" v-if="$index === (localParamsList.length - 1)">
        <a href="javascript:" @click="!isDetails && _addUdp()" >
          <i class="iconfont" :class="_isDetails" data-toggle="tooltip" :title="$t('Add')">&#xe636;</i>
        </a>
      </span>
    </div>
    <span class="add-dp" v-if="!localParamsList.length">
      <a href="javascript:" @click="!isDetails && _addUdp()" >
        <i class="iconfont" :class="_isDetails" data-toggle="tooltip" :title="$t('Add')">&#xe636;</i>
      </a>
    </span>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import { positionList } from './commcon'
  import disabledState from '@/module/mixin/disabledState'
  export default {
    name: 'http-params',
    data () {
      return {
        // Increased data
        localParamsList: [],
        // Current execution index
        localParamsIndex: null,
        // 参数位置的下拉框
        positionList:positionList
      }
    },
    mixins: [disabledState],
    props: {
      udpList: Array,
      // hide direct/type
      hide: {
        type: Boolean,
        default: true
      }
    },
    methods: {
      /**
       * Current index
       */
      _getIndex (index) {
        this.localParamsIndex = index
      },
      /**
       * 获取参数位置
       */
      _handlePositionChanged () {
        this._verifProp('value')
      },
      /**
       * delete item
       */
      _removeUdp (index) {
        this.localParamsList.splice(index, 1)
        this._verifProp('value')
      },
      /**
       * add
       */
      _addUdp () {
        this.localParamsList.push({
          prop: '',
          direct: 'IN',
          type: 'VARCHAR',
          value: ''
        })
      },
      /**
       * blur verification
       */
      _handleValue () {
        this._verifProp('value')
      },
      /**
       * Verify that the value exists or is empty
       */
      _verifProp (type) {
        let arr = []
        let flag = true
        _.map(this.localParamsList, v => {
          arr.push(v.prop)
          if (!v.prop) {
            flag = false
          }
        })
        if (!flag) {
          if (!type) {
            this.$message.warning(`${i18n.$t('prop is empty')}`)
          }
          return false
        }
        let newArr = _.cloneDeep(_.uniqWith(arr, _.isEqual))
        if (newArr.length !== arr.length) {
          if (!type) {
            this.$message.warning(`${i18n.$t('prop is repeat')}`)
          }
          return false
        }
        this.$emit('on-local-params', _.cloneDeep(this.localParamsList))
        return true
        return true
      }
    },
    watch: {
      // Monitor data changes
      udpList () {
        this.localParamsList = this.udpList
      }
    },
    created () {
      this.localParamsList = this.udpList
    },
    computed: {
      inputStyle () {
        return "width:30%"
      }
    },
    mounted () {
    },
    components: { }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .user-def-params-model {
    .select-listpp {
      margin-bottom: 6px;
      .lt-add {
        padding-left: 4px;
        a {
          .iconfont {
            font-size: 18px;
            vertical-align: middle;
            margin-bottom: -2px;
            display: inline-block;
          }
        }
      }
    }
    .add {
      a {
        color: #000;
        .iconfont {
          font-size: 18px;
          vertical-align: middle;
          display: inline-block;
          margin-top: 1px;
        }
      }
    }
    .add-dp{
      a {
        color: #0097e0;
        .iconfont {
          font-size: 18px;
          vertical-align: middle;
          display: inline-block;
          margin-top: 2px;
        }
      }
    }
  }
</style>
