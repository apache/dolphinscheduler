<template>
  <div class="statement-list-model">
    <div class="select-listpp"
         v-for="(item,$index) in localStatementList"
         :key="item.id"
         @click="_getIndex($index)">
      <x-input
        :disabled="isDetails"
        type="textarea"
        resize="none"
        :autosize="{minRows:1}"
        v-model="localStatementList[$index]"
        @on-blur="_verifProp()"
        style="width: 525px;">
      </x-input>
      <span class="lt-add">
        <a href="javascript:" style="color:red;" @click="!isDetails && _removeStatement($index)" >
          <i class="iconfont" :class="_isDetails" data-toggle="tooltip" :title="$t('delete')" >&#xe611;</i>
        </a>
      </span>
      <span class="add" v-if="$index === (localStatementList.length - 1)">
        <a href="javascript:" @click="!isDetails && _addStatement()" >
          <i class="iconfont" :class="_isDetails" data-toggle="tooltip" :title="$t('Add')">&#xe636;</i>
        </a>
      </span>
    </div>
    <span class="add" v-if="!localStatementList.length">
      <a href="javascript:" @click="!isDetails && _addStatement()" >
        <i class="iconfont" :class="_isDetails" data-toggle="tooltip" :title="$t('Add')">&#xe636;</i>
      </a>
    </span>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import disabledState from '@/module/mixin/disabledState'
  export default {
    name: 'user-def-statements',
    data () {
      return {
        // Increased data
        localStatementList: [],
        // Current execution index
        localStatementIndex: null
      }
    },
    mixins: [disabledState],
    props: {
      statementList: Array
    },
    methods: {
      /**
       * Current index
       */
      _getIndex (index) {
        this.localStatementIndex = index
      },
      /**
       * delete item
       */
      _removeStatement (index) {
        this.localStatementList.splice(index, 1)
        this._verifProp('value')
      },
      /**
       * add
       */
      _addStatement () {
        this.localStatementList.push('')
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
        _.map(this.localStatementList, v => {
          arr.push(v)
          if (!v) {
            flag = false
          }
        })
        if (!flag) {
          if (!type) {
            this.$message.warning(`${i18n.$t('Statement cannot be empty')}`)
          }
          return false
        }

        this.$emit('on-statement-list', _.cloneDeep(this.localStatementList))
        return true
      }
    },
    watch: {
      // Monitor data changes
      statementList () {
        this.localStatementList = this.statementList
      }
    },
    created () {
      this.localStatementList = this.statementList
    },
    mounted () {
    },
    components: { }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .statement-list-model {
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
        .iconfont {
          font-size: 18px;
          vertical-align: middle;
          display: inline-block;
          margin-top: 1px;
        }
      }
    }
  }
</style>
