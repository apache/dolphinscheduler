<template>
  <div :class="wrapClasses">
    <ul :class="simpleClasses" v-if="simple">
      <li :class="prevClasses" @click="prevPage">
        <i class="ans-icon-arrow-left"></i>
      </li>
      <div :class="simpleContentClasses">
        <input
          type="text"
          class="page-input"
          :value="currentPage"
          autocomplete="off"
          @keydown="keyDown"
          @keyup="keyUp"
          @change="keyUp">
        <span>/</span>
        <span>{{lastPage}}</span>
      </div>
      <li :class="nextClasses" @click="nextPage">
        <i class="ans-icon-arrow-right"></i>
      </li>
    </ul>

    <ul :class="normalClasses" v-else>
      <!-- `共100条` -->
      <li class="data-show" v-if="showTotal">
        <span>{{t('ans.page.total', { total })}}</span>
      </li>
      <!-- `<` -->
      <li :class="prevClasses" @click="prevPage">
        <i class="ans-icon-arrow-left"></i>
      </li>
      <!-- `1` -->
      <li
        v-if="lastPage > 0"
        class="number"
        :class="{active: currentPage === 1}"
        @click="changePage(1)">1</li>
      <!-- `...` -->
      <li
        v-if="showPrevMore"
        class="number"
        @click="fastPrev"
        @mouseenter="onMouseenter('left')"
        @mouseleave="quickPrevIconClass = 'ans-icon-more'">
        <i :class="quickPrevIconClass"></i>
      </li>
      <!-- `2 3 4...` -->
      <li
        class="number"
        :class="{ active: currentPage === item }"
        v-for="item in pages"
        :key="getKey(item)"
        @click="changePage(item)">{{item}}</li>
      <!-- `...` -->
      <li
        v-if="showNextMore"
        class="number"
        @click="fastNext"
        @mouseenter="onMouseenter('right')"
        @mouseleave="quickNextIconClass = 'ans-icon-more'">
        <i :class="quickNextIconClass"></i>
      </li>
      <!-- 最后一页 -->
      <li
        v-if="lastPage > 1"
        class="number"
        :class="{ active: currentPage === lastPage }"
        @click="changePage(lastPage)">{{lastPage}}</li>
      <!-- `>` -->
      <li :class="nextClasses" @click="nextPage">
        <i class="ans-icon-arrow-right"></i>
      </li>
      <!-- 每页条数 -->
      <li class="sizer" v-if="showSizer">
        <x-select v-model="currentPageSize" @on-visible-change="handleSizerDropdown">
          <span
            class="trigger"
            slot="trigger"
            slot-scope="{ selectedModel }">
            <span>{{selectedModel && selectedModel.label}}</span>
            <i class="ans-icon-arrow-down arrow-down" :class="{reverse: sizerDropdownVisible}"></i>
          </span>
          <x-option
            v-for="size in pageSizeOptions"
            :key="size"
            :value="size"
            :label="`${size}${t('ans.page.pagesize')}`">
          </x-option>
        </x-select>
      </li>
      <!-- `跳转至 页` -->
      <li class="jump" v-if="showElevator">
        <span>{{t('ans.page.goto')}}</span>
        <input type="text" class="page-input" autocomplete="off" @keyup.enter="jumpPage">
        <span>{{t('ans.page.pageClassifier')}}</span>
      </li>
    </ul>
  </div>
</template>

<script>
import { LIB_NAME, Locale } from '../../../../src/util'
import { xSelect, xOption } from '../../../vue-select/src'

const prefixCls = `${LIB_NAME}-page`

// 清除 ul 元素内的空文本
const deleteSpace = node => {
  let childs = node.childNodes
  for (let i = 0; i < childs.length; i++) {
    if (childs[i].nodeType === 3 && /^\s+$/.test(childs[i].nodeValue)) {
      node.removeChild(childs[i])
    }
  }
}

export default {
  name: 'xPage',

  components: { xSelect, xOption },

  mixins: [Locale],

  props: {
    current: {
      type: Number,
      default: 1
    },

    total: {
      type: Number,
      default: 0
    },

    pageSize: {
      type: Number,
      default: 10
    },

    pageSizeOptions: {
      type: Array,
      default () {
        return [10, 20, 30, 40, 50]
      }
    },

    pagerCount: {
      type: Number,
      default: 7,
      validator (v) {
        return v % 2 === 1
      }
    },

    showTotal: {
      type: Boolean,
      default: false
    },

    showElevator: {
      type: Boolean,
      default: false
    },

    showSizer: {
      type: Boolean,
      default: false
    },

    simple: {
      type: Boolean,
      default: false
    },

    small: {
      type: Boolean,
      default: false
    }
  },

  data () {
    return {
      currentPage: this.current,
      currentPageSize: this.pageSize,
      quickPrevIconClass: 'ans-icon-more',
      quickNextIconClass: 'ans-icon-more',
      sizerDropdownVisible: false
    }
  },

  computed: {
    lastPage () {
      const lastPage = Math.ceil(this.total / this.currentPageSize)
      return lastPage === 0 ? 1 : lastPage
    },

    showPrevMore () {
      const { pagerCount, currentPage, lastPage } = this
      const halfPagerCount = (pagerCount - 1) / 2
      return lastPage > pagerCount && currentPage > pagerCount - halfPagerCount
    },

    showNextMore () {
      const { pagerCount, currentPage, lastPage } = this
      const halfPagerCount = (pagerCount - 1) / 2
      return lastPage > pagerCount && currentPage < lastPage - halfPagerCount
    },

    // 当前的分页页码数组
    pages () {
      const { pagerCount, currentPage, lastPage, showPrevMore, showNextMore } = this

      const array = []

      if (showPrevMore && !showNextMore) {
        const startPage = lastPage - (pagerCount - 2)
        for (let i = startPage; i < lastPage; i++) {
          array.push(i)
        }
      } else if (!showPrevMore && showNextMore) {
        for (let i = 2; i < pagerCount; i++) {
          array.push(i)
        }
      } else if (showPrevMore && showNextMore) {
        const offset = Math.floor(pagerCount / 2) - 1
        for (let i = currentPage - offset; i <= currentPage + offset; i++) {
          array.push(i)
        }
      } else {
        for (let i = 2; i < lastPage; i++) {
          array.push(i)
        }
      }

      return array
    },

    wrapClasses () {
      return [
        `${prefixCls}`
      ]
    },

    simpleClasses () {
      return `${prefixCls}-simple`
    },

    simpleContentClasses () {
      return `${prefixCls}-simple-content`
    },

    normalClasses () {
      return [
        `${prefixCls}-normal`,
        { small: this.small }
      ]
    },

    prevClasses () {
      return [
        `${prefixCls}-prev`,
        {
          [`${prefixCls}-disabled`]: this.currentPage === 1
        }
      ]
    },

    nextClasses () {
      return [
        `${prefixCls}-next`,
        {
          [`${prefixCls}-disabled`]: this.currentPage === this.lastPage
        }
      ]
    }
  },

  watch: {
    current (v) {
      this.currentPage = v
    },

    currentPageSize () {
      if (this.currentPage > this.lastPage) {
        this.changePage(this.lastPage)
      }
    },

    showPrevMore (v) {
      if (!v) {
        this.quickPrevIconClass = 'ans-icon-more'
      }
    },

    showNextMore (v) {
      if (!v) {
        this.quickNextIconClass = 'ans-icon-more'
      }
    }
  },

  methods: {
    handleSizerDropdown (visible) {
      this.sizerDropdownVisible = visible
    },

    // 防止缓存 bug
    getKey () {
      return Math.random()
    },

    changePage (page) {
      if (this.currentPage !== page) {
        this.currentPage = page
        this.$emit('on-change', page)
      }
    },

    prevPage () {
      const _current = this.currentPage

      if (_current <= 1) {
        return
      }

      this.changePage(_current - 1)
    },

    nextPage () {
      const _current = this.currentPage

      if (_current >= this.lastPage) {
        return
      }

      this.changePage(_current + 1)
    },

    fastPrev () {
      const _page = this.currentPage - 5
      if (_page > 0) {
        this.changePage(_page)
      } else {
        this.changePage(1)
      }
    },

    fastNext () {
      const _page = this.currentPage + 5
      if (_page > this.lastPage) {
        this.changePage(this.lastPage)
      } else {
        this.changePage(_page)
      }
    },

    keyDown (e) {
      const _key = e.keyCode
      const _condition = (_key >= 48 && _key <= 57) || (_key >= 96 && _key <= 105) || _key === 8

      if (!_condition) {
        e.preventDefault()
      }
    },

    keyUp (e) {
      const _key = e.keyCode
      const _value = parseInt(e.target.value)

      if (_key === 37 || _key === 38) {
        this.prevPage()
      } else if (_key === 39 || _key === 40) {
        this.nextPage()
      } else if (_key === 13) {
        let _page = 1

        if (_value > this.lastPage) {
          _page = this.lastPage
        } else if (_value <= 0 || !_value) {
          _page = 1
        } else {
          _page = _value
        }

        e.target.value = _page
        this.changePage(_page)
      }
    },

    jumpPage (e) {
      let _value = parseInt(e.target.value.trim())
      let _page = 0

      if (!_value || _value === this.currentPage) {
        return
      }

      if (_value > this.lastPage) {
        _page = this.lastPage
      } else {
        _page = _value
      }

      e.target.value = ''
      this.changePage(_page)
    },

    onMouseenter (direction) {
      if (direction === 'left') {
        this.quickPrevIconClass = 'ans-icon-arrow-to-left'
      } else {
        this.quickNextIconClass = 'ans-icon-arrow-to-right'
      }
    },

    cleanSpace () {
      document.querySelectorAll(`.${prefixCls} ul`).forEach(item => {
        deleteSpace(item)
      })
    }
  },

  updated () {
    this.cleanSpace()
  },

  mounted () {
    this.cleanSpace()
  }
}
</script>
