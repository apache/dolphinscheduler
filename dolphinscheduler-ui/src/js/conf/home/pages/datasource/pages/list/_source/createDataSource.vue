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
  <div class="datasource-popup-model">
    <div class="top-p">
      <span>{{item ? `${$t('Edit')}` : `${$t('Create')}`}}{{`${$t('Datasource')}`}}</span>
    </div>
    <div class="content-p">
      <div class="create-datasource-model">
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Datasource')}}</template>
          <template slot="content">
            <x-radio-group v-model="type" size="small">
              <x-radio :label="'MYSQL'">MYSQL</x-radio>
              <x-radio :label="'POSTGRESQL'">POSTGRESQL</x-radio>
              <x-radio :label="'HIVE'">HIVE/IMPALA</x-radio>
              <x-radio :label="'SPARK'">SPARK</x-radio>
              <x-radio :label="'CLICKHOUSE'">CLICKHOUSE</x-radio>
              <x-radio :label="'ORACLE'">ORACLE</x-radio>
              <x-radio :label="'SQLSERVER'">SQLSERVER</x-radio>
              <x-radio :label="'DB2'" class="radio-label-last" >DB2</x-radio>
            </x-radio-group>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Datasource Name')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="name"
                    maxlength="60"
                    :placeholder="$t('Please enter datasource name')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('Description')}}</template>
          <template slot="content">
            <x-input
                    type="textarea"
                    v-model="note"
                    :placeholder="$t('Please enter description')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('IP')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="host"
                    maxlength="60"
                    :placeholder="$t('Please enter IP')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Port')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="port"
                    :placeholder="$t('Please enter port')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f :class="{hidden:showPrincipal}">
          <template slot="name"><strong>*</strong>Principal</template>
          <template slot="content">
            <x-input
              type="input"
              v-model="principal"
              :placeholder="$t('Please enter Principal')"
              autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('User Name')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="userName"
                    maxlength="60"
                    :placeholder="$t('Please enter user name')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('Password')}}</template>
          <template slot="content">
            <x-input
                    type="password"
                    v-model="password"
                    :placeholder="$t('Please enter your password')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong :class="{hidden:showdDatabase}">*</strong>{{$t('Database Name')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="database"
                    maxlength="60"
                    :placeholder="$t('Please enter database name')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f v-if="showConnectType">
          <template slot="name"><strong>*</strong>{{$t('Oracle Connect Type')}}</template>
          <template slot="content">
            <x-radio-group v-model="connectType" size="small">
              <x-radio :label="'ORACLE_SERVICE_NAME'">{{$t('Oracle Service Name')}}</x-radio>
              <x-radio :label="'ORACLE_SID'">{{$t('Oracle SID')}}</x-radio>
            </x-radio-group>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('jdbc connect parameters')}}</template>
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
      <x-button type="text" @click="_close()"> {{$t('Cancel')}} </x-button>
      <x-button type="success" shape="circle" @click="_testConnect()" :loading="testLoading">{{testLoading ? 'Loading...' : $t('Test Connect')}}</x-button>
      <x-button type="primary" shape="circle" :loading="spinnerLoading" @click="_ok()">{{spinnerLoading ? 'Loading...' :item ? `${$t('Edit')}` : `${$t('Submit')}`}} </x-button>
    </div>
  </div>
</template>
<script>
  import i18n from '@/module/i18n'
  import store from '@/conf/home/store'
  import {isJson} from '@/module/util/util'
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
        // description
        note: '',
        // host
        host: '',
        // port
        port: '',
        // data storage name
        database: '',
        // principal
        principal:'',
        // database username
        userName: '',
        // Database password
        password: '',
        // Database connect type
        connectType: '',
        // Jdbc connection parameter
        other: '',
        // btn test loading
        testLoading: false,
        showPrincipal: true,
        showdDatabase: false,
        showConnectType: false,
        isShowPrincipal:true,
        prePortMapper:{}
      }
    },
    props: {
      item: Object
    },

    methods: {
      _rtOtherPlaceholder () {
        return `${i18n.$t('Please enter format')} {"key1":"value1","key2":"value2"...} ${i18n.$t('connection parameter')}`
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
          principal: this.principal,
          userName: this.userName,
          password: this.password,
          connectType: this.connectType,
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
          this.$message.warning(`${i18n.$t('Please enter resource name')}`)
          return false
        }
        if (!this.host) {
          this.$message.warning(`${i18n.$t('Please enter IP/hostname')}`)
          return false
        }
        if (!this.port) {
          this.$message.warning(`${i18n.$t('Please enter port')}`)
          return false
        }
        if (!this.userName) {
          this.$message.warning(`${i18n.$t('Please enter user name')}`)
          return false
        }

        if (!this.database && this.showdDatabase == false) {
          this.$message.warning(`${i18n.$t('Please enter database name')}`)
          return false
        }
        if (this.other) {
          if (!isJson(this.other)) {
            this.$message.warning(`${i18n.$t('jdbc connection parameters is not a correct JSON format')}`)
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
      _getEditDatasource() {
        this.store.dispatch('datasource/getEditDatasource', {id: this.item.id}).then(res => {
          this.type = res.type
          this.name = res.name
          this.note = res.note
          this.host = res.host

          //When in Editpage, Prevent default value overwrite backfill value
          let that = this;
          setTimeout(() => {
            this.port = res.port
          },0)

          this.principal = res.principal
          this.database = res.database
          this.userName = res.userName
          this.password = res.password
          this.connectType = res.connectType
          this.other = JSON.stringify(res.other) === '{}' ? '' : JSON.stringify(res.other)
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      /**
       * Set default port for each type.
       */
      _setDefaultValues(value) {

        //Default type is MYSQL
        let type = this.type || 'MYSQL'

        let defaultPort = this._getDefaultPort(type)

        //Backfill the previous input from memcache
        let mapperPort = this.prePortMapper[type]

        this.port = mapperPort || defaultPort

      },

      /**
       * Get default port by type
       */
      _getDefaultPort(type) {
        var defaultPort = ''
        switch (type) {
          case 'MYSQL':
            defaultPort = '3306'
            break
          case 'POSTGRESQL':
            defaultPort = '5432'
            break
          case 'HIVE':
            defaultPort = '10000'
            break
          case 'SPARK':
            defaultPort = '10015'
            break
          case 'CLICKHOUSE':
            defaultPort = '8123'
            break
          case 'ORACLE':
            defaultPort = '1521'
            break
          case 'SQLSERVER':
            defaultPort = '1433'
            break
          case 'DB2':
            defaultPort = '50000'
            break
          default:
            break

        }
        return defaultPort
      },
    },
    created () {
      // Backfill
      if (this.item.id) {
        this._getEditDatasource()
      }

      this._setDefaultValues()

    },
    watch: {
      type(value){
        if(value == 'POSTGRESQL') {
          this.showdDatabase = true;
        } else {
          this.showdDatabase = false;
        }

        if (value== 'ORACLE' && !this.item.id) {
          this.showConnectType = true;
          this.connectType = 'ORACLE_SERVICE_NAME'
        } else if(value== 'ORACLE' && this.item.id) {
          this.showConnectType = true;
        } else {
          this.showConnectType = false;
        }
        //Set default port for each type datasource
        this._setDefaultValues(value)

        return new Promise((resolve, reject) => {
          this.store.dispatch('datasource/getKerberosStartupState').then(res => {
            this.isShowPrincipal=res
            if((value =='HIVE'||value == 'SPARK')&&this.isShowPrincipal== true){
              this.showPrincipal = false
            }else{
              this.showPrincipal = true
            }
          }).catch(e => {
            this.$message.error(e.msg || '')
            reject(e)
          })
        })
      },
      /**
       * Cache the previous input port for each type datasource
       * @param value
       */
      port(value){
        this.prePortMapper[this.type] = value
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
      min-width: 850px;
      min-height: 100px;
      .list-box-f {
        .text {
          width: 166px;
        }
        .cont {
          width: calc(100% - 186px);
        }
      }
    }
    .radio-label-last {
      margin-left: 0px !important;
    }
  }


</style>
