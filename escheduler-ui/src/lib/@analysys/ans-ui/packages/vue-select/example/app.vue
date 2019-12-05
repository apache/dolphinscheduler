<template>
  <div class="select-demo-wrapper">
    <section class="demo-section">
      <h4>基本用法</h4>
      <div>
        <x-select>
          <x-option
            v-for="city in cities"
            :key="city.value"
            :value="city.value"
            :label="city.label">
          </x-option>
        </x-select>
      </div>
    </section>
    <section class="demo-section">
      <h4>禁用选项</h4>
      <div>
        <x-select>
          <x-option
            v-for="city in disabledCities"
            :key="city.value"
            :disabled="city.disabled"
            :value="city.value"
            :label="city.label">
          </x-option>
        </x-select>
      </div>
    </section>
    <section class="demo-section">
      <h4>禁用状态</h4>
      <div>
        <x-select disabled>
          <x-option
            v-for="city in cities"
            :key="city.value"
            :value="city.value"
            :label="city.label">
          </x-option>
        </x-select>
        <x-select disabled v-model="defaultValue">
          <x-option
            v-for="city in cities"
            :key="city.value"
            :value="city.value"
            :label="city.label">
          </x-option>
        </x-select>
      </div>
    </section>
    <section class="demo-section">
      <h4>多选</h4>
      <div>
        <x-select multiple>
          <x-option
            v-for="city in cities"
            :key="city.value"
            :value="city.value"
            :label="city.label">
          </x-option>
        </x-select>
      </div>
    </section>
    <section class="demo-section">
      <h4>默认选项</h4>
      <div>
        <x-select v-model="defaultValue">
          <x-option
            v-for="city in cities"
            :key="city.value"
            :value="city.value"
            :label="city.label">
          </x-option>
        </x-select>
        <x-select multiple v-model="defaultMultipleValue">
          <x-option
            v-for="city in cities"
            :key="city.value"
            :value="city.value"
            :label="city.label">
          </x-option>
        </x-select>
      </div>
    </section>
    <section class="demo-section">
      <h4>可清空</h4>
      <div>
        <x-select clearable v-model="defaultValue">
          <x-option
            v-for="city in cities"
            :key="city.value"
            :value="city.value"
            :label="city.label">
          </x-option>
        </x-select>
        <x-select multiple clearable v-model="defaultMultipleValue">
          <x-option
            v-for="city in cities"
            :key="city.value"
            :value="city.value"
            :label="city.label">
          </x-option>
        </x-select>
      </div>
    </section>
    <section class="demo-section">
      <h4>分组</h4>
      <div>
        <x-select clearable v-model="defaultValue">
          <x-option-group v-for="(group, index) in groupCities" :key="index" :label="group.label">
            <x-option
              v-for="city in group.options"
              :key="city.value"
              :value="city.value"
              :label="city.label">
            </x-option>
          </x-option-group>
        </x-select>
        <x-select multiple clearable v-model="defaultMultipleValue">
          <x-option-group v-for="(group, index) in groupCities" :key="index" :label="group.label">
            <x-option
              v-for="city in group.options"
              :key="city.value"
              :value="city.value"
              :label="city.label">
            </x-option>
          </x-option-group>
        </x-select>
      </div>
    </section>
    <section class="demo-section">
      <h4>搜索</h4>
      <div>
        <x-select filterable>
          <x-option
            v-for="city in cities"
            :key="city.value"
            :value="city.value"
            :label="city.label">
          </x-option>
        </x-select>
        <x-select multiple filterable>
          <x-option-group v-for="(group, index) in groupCities" :key="index" :label="group.label">
            <x-option
              v-for="city in group.options"
              :key="city.value"
              :value="city.value"
              :label="city.label">
            </x-option>
          </x-option-group>
        </x-select>
      </div>
    </section>
    <section class="demo-section">
      <h4>Select 插槽</h4>
      <div>
        <x-select>
          <x-button slot="trigger" slot-scope="{ selectedModel }">
            <span v-if="selectedModel">
              {{selectedModel.label}}-{{selectedModel.value}}
            </span>
            <span v-else>当前未选中</span>
          </x-button>
          <x-option
            v-for="city in cities"
            :key="city.value"
            :value="city.value"
            :label="city.label">
          </x-option>
        </x-select>
        <x-select clearable multiple ref="custom">
          <div slot="multiple" slot-scope="{ selectedList }">
            <span class="tag-wrapper" v-for="o in selectedList" :key="o.value">
              <span class="tag-text">{{o.label}}-{{o.value}}</span>
              <i class="remove-tag ans-icon-close" @click.stop="removeSelected(o)"></i>
            </span>
          </div>
          <x-option
            v-for="city in cities"
            :key="city.value"
            :value="city.value"
            :label="city.label">
          </x-option>
        </x-select>
      </div>
    </section>
    <section class="demo-section">
      <h4>OptionGroup 插槽</h4>
      <div>
        <x-select clearable>
          <x-option-group v-for="(group, index) in groupCities" :key="index">
            <div class="custom-group" slot="content">
              <i class="ans-icon-notice-solid"></i>
              <span>{{group.label}}</span>
            </div>
            <x-option
              v-for="city in group.options"
              :key="city.value"
              :value="city.value"
              :label="city.label">
            </x-option>
          </x-option-group>
        </x-select>
      </div>
    </section>
    <section class="demo-section">
      <h4>Option 默认插槽</h4>
      <div>
        <x-select clearable>
          <x-option
            v-for="city in cities"
            :key="city.value"
            :value="city.value"
            :label="city.label">
            <div class="custom">
              <span class="left">{{ city.label }}</span>
              <span class="right">{{ city.value }}</span>
            </div>
          </x-option>
        </x-select>
      </div>
    </section>
  </div>
</template>

<script>
import { xButton } from '../../vue-button/src'
import { xSelect, xOption, xOptionGroup } from '../src'

export default {
  name: 'app',
  components: { xSelect, xOption, xOptionGroup, xButton },
  data () {
    return {
      triggerValue: 'Beijing',
      cities: [{
        value: 'Beijing',
        label: '北京'
      }, {
        value: 'Shanghai',
        label: '上海'
      }, {
        value: 'Nanjing',
        label: '南京'
      }, {
        value: 'Chengdu',
        label: '成都'
      }, {
        value: 'Shenzhen',
        label: '深圳'
      }, {
        value: 'Guangzhou',
        label: '广州'
      }],
      defaultValue: 'Nanjing',
      defaultMultipleValue: ['Shenzhen', 'Chengdu'],
      groupCities: [{
        label: '热门城市',
        options: [{
          value: 'Shanghai',
          label: '上海'
        }, {
          value: 'Beijing',
          label: '北京'
        }]
      }, {
        label: '城市名',
        options: [{
          value: 'Chengdu',
          label: '成都'
        }, {
          value: 'Nanjing',
          label: '南京'
        }, {
          value: 'Shenzhen',
          label: '深圳'
        }, {
          value: 'Guangzhou',
          label: '广州'
        }]
      }]
    }
  },
  computed: {
    disabledCities () {
      return this.cities.map((c, i) => {
        const city = Object.assign({}, c)
        city.disabled = i % 2 === 0
        return city
      })
    }
  },
  methods: {
    removeSelected (option) {
      this.$refs.custom.handleRemoveTag(option)
    }
  }
}
</script>

<style lang="scss">
.demo-wrapper {
  .cate {
    height: 40px;
    margin-left: 20px;
    font-size: 16px;
    font-weight: bold;
    line-height: 40px;
  }
  .row {
    display: flex;
    margin: 20px 0;
    padding: 0 50px;
    & > div {
      margin-right: 50px;
    }
  }
  .custom {
    display: flex;
    justify-content: space-between;
    padding: 5px 10px;
    &:hover {
      background: #8492a6;
      .left,
      .right {
        color: #fff;
      }
    }
    .left {
      font-size: 12px;
    }
    .right {
      color: #8492a6;
      font-size: 12px;
      transform: scale(0.8);
    }
  }
  .selected .custom {
    color: #598cd4;
    background: #dfd8e4;
  }
  .custom-group {
    height: 40px;
    line-height: 40px;
    font-size: 14px;
    color: #303030;
    font-weight: bolder;
    background: #b7d1f0;
    i {
      margin: 0 10px;
    }
  }
}
</style>
