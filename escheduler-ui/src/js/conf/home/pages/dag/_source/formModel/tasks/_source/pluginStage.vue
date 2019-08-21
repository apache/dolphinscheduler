<template>
  <div class="plugin-stage-model">
    <div class="list-model">
      <div class="table-box">
        <table>
          <tr>
            <th width="30">
              <span>{{'#'}}</span>
            </th>
            <th width="80">
              <span>{{'Parameter Group'}}</span>
            </th>
            <th width="180">
              <span>{{'Parameter Name'}}</span>
            </th>
            <th width="25">
              <span>I</span>
            </th>
            <th width="60">
              <span>{{'Parameter Type'}}</span>
            </th>
            <th>
              <span>{{'Parameter Value'}}</span>
            </th>
          </tr>
          <tr v-for="(item, $index) in stageDefinition" :key="$index" v-if="configElementVisible[$index]===true">
            <td>
              <span>{{$index + 1}}</span>
            </td>
            <td>
              <span>{{item.group}}</span>
            </td>
            <td>
              <span v-bind:class="{required: item.required}">{{item.label}}</span>
            </td>
            <td>
              <i v-if="item.description.length > 0" class="i ans-icon-notice-empty" data-toggle="tooltip" :title="item.description"></i>
            </td>
            <td>
              <span>{{item.type}}</span>
            </td>
            <td>
              <span v-if="item.type==='STRING' || item.type==='CREDENTIAL' || item.type==='CHARACTER'">
                <x-input
                  :disabled="isDetails"
                  type="text"
                  v-model="configValue[$index].value"
                  @on-change="_handleConfigValueChanged_TEXT($event, $index)"
                  style="width: 200px;">
                </x-input>
              </span>
              <span v-if="item.type==='TEXT'">
                <x-input
                  :disabled="isDetails"
                  type="textarea"
                  resize="none"
                  :autosize="{minRows:2}"
                  v-model="configValue[$index].value"
                  @on-change="_handleConfigValueChanged_TEXT($event, $index)"
                  style="width: 200px;">
                </x-input>
              </span>
              <span v-if="item.type==='NUMBER'">
                <x-input
                  :disabled="isDetails"
                  type="text"
                  v-model="configValue[$index].value"
                  @on-change="_handleConfigValueChanged_TEXT($event, $index)"
                  style="width: 200px;">
                </x-input>
              </span>
              <span v-if="item.type==='BOOLEAN'">
                <x-switch v-model="configValue[$index].value" @on-change="_handleConfigValueChanged_TEXT($event, $index)" :disabled="isDetails"></x-switch>
              </span>
              <span v-if="item.type==='MODEL' && item.model.modelType==='VALUE_CHOOSER'">
                <x-select
                  v-model="configValue[$index].value"
                  @on-change="_handleConfigValueChanged_VALUE_CHOOSER($event, $index)"
                  :disabled="isDetails"
                  style="width: 200px;">
                  <x-option
                    v-for="(vitem, $vindex) in item.model.values" :key="$vindex"
                    :value="vitem"
                    :label="item.model.labels[$vindex]">
                  </x-option>
                </x-select>
              </span>
              <span style="background-color: yellow;" v-if="item.type !== 'STRING' && item.type !== 'CREDENTIAL' && item.type !== 'CHARACTER' && item.type !=='TEXT' && item.type !=='NUMBER' && item.type !=='BOOLEAN' && !(item.type === 'MODEL' && item.model.modelType === 'VALUE_CHOOSER')">
                  {{JSON.stringify(configValue[$index].value)}}
              </span>
            </td>
          </tr>
        </table>
      </div>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import disabledState from '@/module/mixin/disabledState'
  export default {
    name: 'plugin-stage',
    data () {
      return {
        configValue: Array,
        configElementVisible: Array
      }
    },
    mixins: [disabledState],
    props: {
      stageConfig: Array,
      stageDefinition: Array
    },
    methods: {
      _updateCurrentConfig() {
        this.configValue = this.stageConfig.configValue
        this._refreshVisiable()
      },
      _refreshVisiable() {
        let self = this
        let newVisibles = _.map(self.stageDefinition, (cd) => {
          if (!_.isEmpty(cd.dependsOnMap)) {
            return _.every(cd.dependsOnMap, (varray, k) => {
              let foundDepCurrentValue = _.find(self.configValue, (x) => x.name === k)
              if (!_.isNil(foundDepCurrentValue)) {
                return _.includes(varray, foundDepCurrentValue.value)
              } else {
                return true;
              }
            })
          } else {
            return true
          }
        })
        _.map(_.range(0, newVisibles.length), (i) => {
          if (newVisibles[i] !== self.configElementVisible[i]) {
            Vue.set(self.configElementVisible, i, newVisibles[i])
          }
        })
      },
      _handleConfigValueChanged_TEXT(newVal, $index) {
        this._onConfigChanged(newVal, $index)
      },
      _handleConfigValueChanged_VALUE_CHOOSER(newVal, $index) {
        this._onConfigChanged(newVal.value, $index)
      },
      _onConfigChanged(newVal, $index) {
        let keyName = this.configValue[$index].name
        Vue.set(this.configValue, $index, {"name": keyName, "value": newVal})
        this._refreshVisiable()
        this.$nextTick(() => {
          this.$emit('on-stageChange', {
            "configValue": this.configValue
          })
        })
      }
    },
    watch: {
    },
    created () {
      this.$nextTick(() => {
        this._updateCurrentConfig()
      })
    },
    mounted () {
    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  span.required:before {
    content:"* ";
    color: red;
  }
</style>
