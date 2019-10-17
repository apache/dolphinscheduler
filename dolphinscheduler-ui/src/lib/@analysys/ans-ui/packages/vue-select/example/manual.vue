<template>
  <div class="select-demo-wrapper">
    <section class="demo-section">
      <h4>手动触发</h4>
      <div>
        <x-select ref="select" v-model="value" valueKey="id" multiple :popperOptions="{placement:'bottom-start'}">
          <x-option
            v-for="city in cities"
            :key="city.value"
            :value="city"
            :label="city.label"
          >
          </x-option>
          <div slot="trigger">
            <x-button @click="handle">第一个</x-button>
            <x-button @click="handle">第二个</x-button>
            <x-button @click="handle">第三个</x-button>
          </div>
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
      value: 1,
      cities: [{
        id: 1,
        value: 'Beijing',
        label: '北京'
      }, {
        id: 2,
        value: 'Shanghai',
        label: '上海'
      }, {
        id: 3,
        value: 'Nanjing',
        label: '南京'
      }, {
        id: 4,
        value: 'Chengdu',
        label: '成都'
      }, {
        id: 5,
        value: 'Shenzhen',
        label: '深圳'
      }, {
        id: 6,
        value: 'Guangzhou',
        label: '广州'
      }]
    }
  },
  methods: {
    handle (e) {
      this.value = 1
      this.$refs.select.setDropdownReference(e.currentTarget)
      this.$nextTick(() => this.$refs.select.focus())
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
