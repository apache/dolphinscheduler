<template>
  <div class="datasource-popup-model">
    <div class="top-p">
      <span>{{item ? `${$t('编辑')}` : `${$t('创建')}`}}{{`${$t('数据源')}`}}</span>
    </div>
    <div class="content-p">
      <div class="create-datasource-model">
        <m-list-box-f>
          <template slot="name"><b>*</b>{{$t('数据源')}}</template>
          <template slot="content">
            <x-radio-group v-model="type" size="small">
              <x-radio :label="'MYSQL'">MYSQL</x-radio>
              <x-radio :label="'POSTGRESQL'">POSTGRESQL</x-radio>
              <x-radio :label="'HIVE'">HIVE</x-radio>
              <x-radio :label="'SPARK'">SPARK</x-radio>
              <x-radio :label="'CLICKHOUSE'">CLICKHOUSE</x-radio>
            </x-radio-group>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><b>*</b>{{$t('数据源名称')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="name"
                    :placeholder="$t('请输入数据源名称')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('描述')}}</template>
          <template slot="content">
            <x-input
                    type="textarea"
                    v-model="note"
                    :placeholder="$t('请输入描述')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><b>*</b>{{$t('IP主机名')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="host"
                    :placeholder="$t('请输入IP主机名')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><b>*</b>{{$t('端口')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="port"
                    :placeholder="$t('请输入端口')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><b>*</b>{{$t('用户名')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="userName"
                    :placeholder="$t('请输入用户名')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('密码')}}</template>
          <template slot="content">
            <x-input
                    type="password"
                    v-model="password"
                    :placeholder="$t('请输入密码')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><b>*</b>{{$t('数据库名')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="database"
                    :placeholder="$t('请输入数据库名')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('jdbc连接参数')}}</template>
          <template slot="content">
            <x-input
                    type="textarea"
                    v-model="other"
                    :autosize="{minRows:2}"
                    :placeholder="_rtOtherPlaceholder()"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
      </div>
    </div>
    <div class="bottom-p">
      <x-button type="text" @click="_close()"> {{$t('取消')}} </x-button>
      <x-button type="success" shape="circle" @click="_testConnect()" :loading="testLoading">{{testLoading ? 'Loading...' : $t('测试连接')}}</x-button>
      <x-button type="primary" shape="circle" :loading="spinnerLoading" @click="_ok()">{{spinnerLoading ? 'Loading...' :item ? `${$t('确认编辑')}` : `${$t('确认提交')}`}} </x-button>
    </div>
  </div>
</template>
<script>
  import i18n from '@/module/i18n'
  import store from '@/conf/home/store'
  import { isJson } from '@/module/util/util'
  import mPopup from '@/module/components/popup/popup'
  import mListBoxF from '@/module/components/listBoxF/listBoxF'

  export default {
    name: 'create-datasource',
    data () {
      return {
        store,
        // btn loading
        spinnerLoading: false,
        // Data source type
        type: 'MYSQL',
        // name
        name: '',
        // desc
        note: '',
        // host
        host: '',
        // port
        port: '',
        // data storage name
        database: '',
        // database username
        userName: '',
        // Database password
        password: '',
        // Jdbc connection parameter
        other: '',
        // btn test loading
        testLoading: false
      }
    },
    props: {
      item: Object
    },
    methods: {
      _rtOtherPlaceholder () {
        return `${i18n.$t('请输入格式为')} {"key1":"value1","key2":"value2"...} ${i18n.$t('连接参数')}`
      },
      /**
       * submit
       */
      _ok () {
        if (this._verification()) {
          this._verifName().then(res => {
            this._submit()
          })
        }
      },
      /**
       * close
       */
      _close () {
        this.$emit('close')
      },
      /**
       * return param
       */
      _rtParam () {
        return {
          type: this.type,
          name: this.name,
          note: this.note,
          host: this.host,
          port: this.port,
          database: this.database,
          userName: this.userName,
          password: this.password,
          other: this.other
        }
      },
      /**
       * test connect
       */
      _testConnect () {
        if (this._verification()) {
          this.testLoading = true
          this.store.dispatch('datasource/connectDatasources', this._rtParam()).then(res => {
            setTimeout(() => {
              this.$message.success(res.msg)
              this.testLoading = false
            }, 800)
          }).catch(e => {
            this.$message.error(e.msg || '')
            this.testLoading = false
          })
        }
      },
      /**
       * Verify that the data source name exists
       */
      _verifName () {
        return new Promise((resolve, reject) => {
          if (this.name === this.item.name) {
            resolve()
            return
          }
          this.store.dispatch('datasource/verifyName', { name: this.name }).then(res => {
            resolve()
          }).catch(e => {
            this.$message.error(e.msg || '')
            reject(e)
          })
        })
      },
      /**
       * verification
       */
      _verification () {
        if (!this.name) {
          this.$message.warning(`${i18n.$t('请输入资源名称')}`)
          return false
        }
        if (!this.host) {
          this.$message.warning(`${i18n.$t('请输入IP/主机名')}`)
          return false
        }
        if (!this.port) {
          this.$message.warning(`${i18n.$t('请输入端口')}`)
          return false
        }
        if (!this.userName) {
          this.$message.warning(`${i18n.$t('请输入用户名')}`)
          return false
        }

        if (!this.database) {
          this.$message.warning(`${i18n.$t('请输入数据库名')}`)
          return false
        }
        if (this.other) {
          if (!isJson(this.other)) {
            this.$message.warning(`${i18n.$t('jdbc连接参数不是一个正确的JSON格式')}`)
            return false
          }
        }

        return true
      },
      /**
       * submit => add/update
       */
      _submit () {
        this.spinnerLoading = true
        let param = this._rtParam()
        // edit
        if (this.item) {
          param.id = this.item.id
        }
        this.store.dispatch(`datasource/${this.item ? `updateDatasource` : `createDatasources`}`, param).then(res => {
          this.$message.success(res.msg)
          this.spinnerLoading = false
          this.$emit('onUpdate')
        }).catch(e => {
          this.$message.error(e.msg || '')
          this.spinnerLoading = false
        })
      },
      /**
       * Get modified data
       */
      _getEditDatasource () {
        this.store.dispatch('datasource/getEditDatasource', { id: this.item.id }).then(res => {
          this.type = res.type
          this.name = res.name
          this.note = res.note
          this.host = res.host
          this.port = res.port
          this.database = res.database
          this.userName = res.userName
          this.password = res.password
          this.other = JSON.stringify(res.other) === '{}' ? '' : JSON.stringify(res.other)
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      }
    },
    watch: {},
    created () {
      // Backfill
      if (this.item.id) {
        this._getEditDatasource()
      }
    },
    mounted () {
    },
    components: { mPopup, mListBoxF }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .datasource-popup-model {
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
      min-width: 500px;
      min-height: 100px;
    }
  }
</style>
