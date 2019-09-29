<template>
  <div :class="[prefixCls, 'ans-select', popperClass]" v-click-outside="handleClose">
    <span>
      <div
        :class="['tag-container', scrollbar]"
        ref="multiple"
        v-if="multiple"
        :style="{maxWidth: (inputWidth - 25) + 'px'}"
        @click="toggle"
        @keydown.esc="handleClose">
        <span
          class="tag-wrapper"
          v-for="(o, index) in selectedOptions"
          :key="typeof o.value === 'object' ? index : o.value">
          <span class="tag-text">{{o.label}}</span>
          <i class="remove-tag ans-icon-close" @click.stop="handleRemoveTag(selected, o)"></i>
        </span>
      </div>
      <x-input
        class="inner-input"
        size="default"
        ref="input"
        :value="displayRender"
        :readonly="!filterable"
        :disabled="disabled"
        @input="handleQuery"
        :placeholder="multiple && selectedOptions.length ? '' : placeholder"
        @on-click="toggle"
        @on-click-icon="toggle"
      >
        <template slot="suffix">
          <i v-if="clearable" v-show="showClear" class="ans-icon-fail-solid clear" @click.stop="handleClear"></i>
          <i v-show="!showClear" class="ans-icon-arrow-down arrow-down" :style="{ transform : visible ? 'rotateZ(180deg)' : '' }"></i>
        </template>
      </x-input>
    </span>
    <transition :name="transitionName">
      <div ref="popper" :class="getCls('drop')" v-show="visible">
        <caspanel :data="list"
                  :multiple="multiple"
                  :prefix-cls="getCls('drop')"
                  :trigger="expandTrigger"
                  :change-on-select="changeOnSelect"
                  v-show="!showFilterList && list.length">
        </caspanel>
        <div v-show="!showFilterList && !list.length">
          <ul :class="[getCls('drop__menu'), 'nodata']">
            <li><i class="ans-icon-no-data"></i></li>
            <li>{{noDataText}}</li>
          </ul>
        </div>
        <div v-show="showFilterList">
          <ul v-show="querySelections.length" :class="getCls('drop__menu')" >
            <li
              v-for="(item, i) in querySelections"
              :key="i"
              v-html="item.display"
              :class="getCls('drop__list')"
              @click="handleSelectItem(item)">
            </li>
          </ul>
          <ul v-show="!querySelections.length" :class="[getCls('drop__menu'), 'nodata']">
            <li><i class="ans-icon-search-no-data"></i></li>
            <li>{{noMatchText}}</li>
          </ul>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { LIB_NAME, Popper, emitter, clickOutside, ANIMATION_PREFIX } from '../../../../src/util'
import { xInput } from '../../../vue-input/src'
import Caspanel from './Caspanel.vue'
import { t } from '../../../../src/locale'

const popperMixin = Object.assign({}, Popper, {
  props: {
    placement: {
      type: String,
      default: 'bottom-start'
    },
    reference: HTMLElement,
    // 与参考元素距离，单位为 px
    distance: {
      type: Number,
      default: 1
    },
    appendToBody: {
      type: Boolean,
      default: false
    },
    positionFixed: {
      type: Boolean,
      default: false
    },
    viewport: {
      type: Boolean,
      default: false
    },
    popperOptions: Object
  }
})
const CHILDREN_NAME = 'Caspanel'

export default {
  name: 'xCascader',
  components: { xInput, Caspanel },
  mixins: [popperMixin, emitter],
  directives: { clickOutside: clickOutside },
  data () {
    return {
      prefixCls: `${LIB_NAME}-cascader`,
      scrollbar: `${LIB_NAME}-scrollbar`,
      queryStr: '',
      tmpSelected: [],
      selected: [],
      currentValue: [],
      updatingValue: false,
      list: [],
      childrenKey: 'children',
      transitionName: `${ANIMATION_PREFIX}drop`,
      selectedOptions: [],
      inputWidth: 0
    }
  },
  props: {
    // 下拉级连框数据
    options: {
      type: Array,
      default: () => []
    },

    // 字段code
    prop: {
      type: Object,
      default () {
        return {
          children: 'children',
          label: 'label',
          value: 'value',
          disabled: 'disabled'
        }
      }
    },

    // 子菜单触发方式
    expandTrigger: {
      validator (value) {
        return ['click', 'hover'].indexOf(value) > -1
      },
      default: 'click'
    },

    // 是否可清除
    clearable: {
      type: Boolean,
      default: true
    },

    // 是否禁用
    disabled: {
      type: Boolean,
      default: false
    },

    placeholder: {
      type: String,
      default () {
        return t('ans.cascader.placeholder')
      }
    },

    // 是否可搜索
    filterable: Boolean,
    // 无数据提示文字
    noDataText: {
      type: String,
      default () {
        return t('ans.cascader.noData')
      }
    },
    // 无数据提示文字
    noMatchText: {
      type: String,
      default () {
        return t('ans.cascader.noMatch')
      }
    },
    changeOnSelect: Boolean,
    value: {
      type: Array,
      default: () => []
    },
    separator: {
      type: String,
      default: '/'
    },
    popperClass: String,
    multiple: Boolean
  },
  watch: {
    value: {
      deep: true,
      immediate: true,
      handler (val, oldVal) {
        if (JSON.stringify(val) !== JSON.stringify(oldVal)) {
          this.currentValue = val
          if (!val.length) this.selected = []
        }
      }
    },
    currentValue (val, oldVal) {
      if (JSON.stringify(val) !== oldVal) {
        this.$emit('input', this.currentValue)
        if (this.updatingValue) {
          this.updatingValue = false
          return
        }
        this.updateSelected(true)
      }
    },
    options: {
      deep: true,
      immediate: true,
      handler (val) {
        this.list = this.handlerData(val)
      }
    },
    visible (val) {
      this.broadcast(CHILDREN_NAME, 'on-visible-change', val)
    }
  },
  computed: {
    showFilterList () {
      return this.filterable && this.queryStr !== ''
    },
    // 是否显示清除按钮
    showClear () {
      return this.currentValue && this.currentValue.length && this.clearable && !this.disabled
    },

    displayRender () {
      let label = []

      for (let i = 0; i < this.selected.length; i++) {
        let item = this.isArray(this.selected[i]) && this.selected[i].length
          ? this.selected[i][0].label
          : this.selected[i].label
        label.push(item)
      }
      return this.multiple ? '' : label.join(this.separator)
    },

    // filter list
    querySelections () {
      let selections = []
      let _this = this
      function getSelections (arr, label, value) {
        for (let i = 0; i < arr.length; i++) {
          let item = arr[i]
          item.__label = label ? label + ' / ' + item.label : item.label
          item.__value = value ? value + ',' + item.value : item.value

          let obj = {
            label: item.__label,
            value: item.__value,
            display: item.__label,
            item: item,
            disabled: !!item.disabled
          }
          if (item.children && item.children.length) {
            getSelections(item.children, item.__label, item.__value)
            _this.changeOnSelect && selections.unshift(obj)
            delete item.__label
            delete item.__value
          } else {
            selections.unshift(obj)
          }
        }
      }
      getSelections(this.list)
      selections = selections.filter(item => {
        return item.label ? item.label.indexOf(this.queryStr) > -1 : false
      }).map(item => {
        item.display = item.display.replace(new RegExp(this.queryStr, 'g'), `<span>${this.queryStr}</span>`)
        return item
      })
      return selections
    }
  },
  methods: {
    toggle () {
      if (this.disabled) return
      this.visible = !this.visible
      this.$refs.input.focus()
    },
    handleClear () {
      const oldVal = JSON.stringify(this.currentValue)
      this.currentValue = this.selected = this.selectedOptions = this.tmpSelected = []
      this.handleClose()
      this.emitValue(this.currentValue, oldVal)
      this.broadcast(CHILDREN_NAME, 'on-clear', true)
      this.setInputHeight()
    },
    updateResult (result) {
      this.tmpSelected = result
    },
    emitValue (val, oldVal) {
      if (JSON.stringify(val) !== oldVal) {
        this.$emit('on-change', this.currentValue, JSON.parse(JSON.stringify(this.selected)))
      }
    },
    updateSelected (init = false) {
      if (!this.changeOnSelect || init) {
        this.broadcast(CHILDREN_NAME, 'on-find-selected', { value: this.currentValue })
      }
    },
    handleClose () {
      this.visible = false
    },
    handleQuery () {
      this.visible = true
      this.queryStr = this.$refs.input.currentValue.trim()
      this.updateElementHandler()
    },
    handleSelectItem (item) {
      this.queryStr = ''
      this.$refs.input.currentValue = ''
      const oldVal = JSON.stringify(this.currentValue)
      this.currentValue = item.value.split(',')
      this.emitValue(this.currentValue, oldVal)
      this.handleClose()
    },
    handlerData (arr) {
      let list = JSON.parse(JSON.stringify(arr))

      list.forEach(t => {
        Object.keys(this.prop).forEach(v => {
          if (!t[v]) {
            let val = t[this.prop[v]]
            t[v] = (v === this.childrenKey && val) ? this.handlerData(val) : val
          }
        })
      })

      return list
    },
    getCls (cls) {
      return this.prefixCls + '-' + cls
    },

    // 多选时，删除单个
    handleRemoveTag (arr, o) {
      if (!arr) return

      arr.forEach((v, i) => {
        if (v.value === o.value) {
          arr.splice(i, 1)
          this.setInputHeight()
          this.updateValue(arr)
          return false
        } else {
          this.isArray(v) && this.handleRemoveTag(v, o)
        }
      })
    },
    isArray (obj) {
      return Object.prototype.toString.call(obj) === '[object Array]'
    },
    setInputHeight () {
      const { input, multiple } = this.$refs
      if (!input || !multiple) return

      this.$nextTick(() => {
        const calculateHeight = multiple.clientHeight + 4
        input.$refs.input.style.height = Math.max(32, calculateHeight) + 'px'
        this.updateElementHandler()
      })
    },

    // 根据selected,得到只包含value的对应数组
    getNewVal (selected) {
      let newVal = []

      selected.forEach((item) => {
        if (this.isArray(item)) {
          const tmp = []
          this.selectedOptions = item
          item.forEach(v => {
            tmp.push(v.value)
          })
          newVal.push(tmp)
        } else {
          newVal.push(item.value)
        }
      })

      return newVal
    },
    updateValue (newVal) {
      this.updatingValue = true
      let oldVal = this.currentValue
      this.currentValue = newVal
      this.emitValue(this.currentValue, oldVal)
    },
    onResultChange () {
      this.$on('on-result-change', (params) => {
        let { lastValue, changeOnSelect, formInit } = params
        !this.multiple && lastValue && !formInit && (this.handleClose())

        let newVal = []
        if (lastValue || changeOnSelect) {
          this.selected = this.tmpSelected
          newVal = this.getNewVal(this.selected)
        }

        if (!formInit) {
          this.setInputHeight()
          this.updateValue(newVal)
        }
      })
    }
  },
  created () {
    this.onResultChange()
  },
  mounted () {
    this.updateSelected(true)
    this.$refs.reference = this.$el
    if (this.$refs.input) {
      this.inputWidth = this.$refs.input.$el.clientWidth
    }
  },
  beforeDestroy () {
    this.$off('on-result-change')
  }
}
</script>
