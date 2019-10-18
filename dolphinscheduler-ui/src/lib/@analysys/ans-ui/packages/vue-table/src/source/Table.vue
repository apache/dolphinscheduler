<template>
  <div :class="wrapperClasses">
    <div class="hidden-content" ref="hiddenContent"><slot></slot></div>
    <div
      v-if="showHeader"
      class="table-header-wrapper flex-wrapper"
      :class="{ 'affix': activeAffix }"
      :style="{
        top: activeAffix ? affixDistance + 'px' : null,
        width: affixHeaderWidth
      }"
      ref="headerWrapper">
      <x-scroller
        ref="headerScroller"
        width="100%"
        height="100%"
        :show-scrollbar="false"
        :disabled="scrollerDisabled"
        :reverse-scroll-y="reverseScrollY"
        @on-scroll-x="handleHeaderScrollX"
        @on-x-start="handleBodyScrollXStart"
        @on-x-end="handleBodyScrollXEnd">
        <x-table-header
          :style="{
            width: layout.bodyWidth ? layout.bodyWidth + 'px' : ''
          }"
          :store="store"
          :stripe="stripe"
          :border="hasBorder">
        </x-table-header>
      </x-scroller>
    </div>
    <div
      class="table-body-wrapper"
      :style="[
        bodyHeight,
        { marginTop: bodyTopMargin }
      ]"
      v-spin.lock.fullscreen="layout.loading"
      ref="bodyWrapper">
      <x-scroller
        width="100%"
        height="100%"
        ref="scroller"
        :scrollbar-class="bodyScrollbarClass"
        :reverse-scroll-y="reverseScrollY"
        :disabled="scrollerDisabled"
        show-scrollbar="active"
        :bar-offset-left="layout.fixedLeftWidth"
        :bar-offset-right="layout.fixedRightWidth"
        @on-scroll-x="handleBodyScrollX"
        @on-scroll-y="handleBodyScrollY"
        @on-x-start="handleBodyScrollXStart"
        @on-x-end="handleBodyScrollXEnd"
        @on-y-start="handleBodyScrollYStart"
        @on-y-end="handleBodyScrollYEnd"
        @on-start-drag-bar="handleStartDragBar"
        @on-end-drag-bar="handleEndDragBar">
        <x-table-body
          ref="bodyTable"
          :style="{
            width: bodyWidth
          }"
          :store="store"
          :stripe="stripe"
          :border="hasBorder">
        </x-table-body>
      </x-scroller>
      <div
        v-if="!data || data.length === 0"
        class="table-empty-wrapper"
        :style="{
          width: bodyWidth
        }">
        <slot name="empty">
          <span class="table-empty-text">{{emptyText}}</span>
        </slot>
      </div>
    </div>
    <div
      v-if="fixedLeftColumns.length > 0"
      :style="{
        width: layout.fixedLeftWidth + 'px',
        height: layout.tableHeight + 'px'
      }"
      :class="{ 'no-shadow': !layout.scrollX || isScrollXStart }"
      class="fixed-left-table-wrapper">
      <div
        v-if="showHeader"
        :class="{ 'affix': activeAffix }"
        :style="fixedLeftHeaderStyles"
        class="table-header-wrapper">
        <x-table-header
          fixed="left"
          :style="{
            width: '100%'
          }"
          :store="store"
          :stripe="stripe"
          :border="hasBorder">
        </x-table-header>
      </div>
      <div
        :style="[
          { top: layout.headerHeight + 'px' },
          fixedBodyHeight
        ]"
        class="table-body-wrapper"
        ref="fixedLeftBodyWrapper">
        <x-scroller
          ref="fixedLeftScroller"
          width="100%"
          height="100%"
          :show-scrollbar="false"
          :disabled="scrollerDisabled"
          @on-y-start="handleBodyScrollYStart"
          @on-y-end="handleBodyScrollYEnd"
          @on-scroll-y="handleFixedScrollY">
          <x-table-body
            fixed="left"
            :style="{
              width: '100%'
            }"
            :store="store"
            :stripe="stripe"
            :border="hasBorder">
          </x-table-body>
        </x-scroller>
      </div>
    </div>
    <div
      v-if="fixedRightColumns.length > 0"
      :style="{
        width: layout.fixedRightWidth + 'px',
        height: layout.tableHeight + 'px',
        right: 0
      }"
      :class="{ 'no-shadow': !layout.scrollX || isScrollXEnd }"
      class="fixed-right-table-wrapper">
      <div
        v-if="showHeader"
        :class="{ 'affix': activeAffix }"
        :style="fixedRightHeaderStyles"
        class="table-header-wrapper">
        <x-table-header
          fixed="right"
          :style="{
            width: '100%'
          }"
          :store="store"
          :stripe="stripe"
          :border="hasBorder">
        </x-table-header>
      </div>
      <div
        :style="[
          { top: layout.headerHeight + 'px' },
          fixedBodyHeight
        ]"
        class="table-body-wrapper"
        ref="fixedRightBodyWrapper">
        <x-scroller
          ref="fixedRightScroller"
          width="100%"
          height="100%"
          :show-scrollbar="false"
          :disabled="scrollerDisabled"
          @on-y-start="handleBodyScrollYStart"
          @on-y-end="handleBodyScrollYEnd"
          @on-scroll-y="handleFixedScrollY">
          <x-table-body
            fixed="right"
            :style="{
              width: '100%'
            }"
            :store="store"
            :stripe="stripe"
            :border="hasBorder">
          </x-table-body>
        </x-scroller>
      </div>
    </div>
    <div class="table-column-resize-proxy" ref="resizeProxy" v-show="resizeProxyVisible"></div>
  </div>
</template>

<script>
import { LIB_NAME } from '../../../../src/util'
import directive from '../../../vue-spin/src/source/directive.js'
import { xScroller } from '../../../vue-scroller/src'
import TableStore from './store.js'
import TableLayout from './layout.js'
import xTableBody from './TableBody'
import xTableHeader from './TableHeader'
import Vue from 'vue'
import { t } from '../../../../src/locale'

Vue.directive('spin', directive)

let tableIdSeed = 1

export default {
  name: 'xTable',

  components: { xTableBody, xTableHeader, xScroller },

  data () {
    const store = new TableStore(this)
    const layout = new TableLayout({
      store,
      table: this,
      fit: this.fit,
      showHeader: this.showHeader
    })
    return {
      store,
      layout,
      multiLayerHeader: false,
      resizeState: {
        width: null,
        height: null
      },
      activeAffix: false,
      defaultAffixDistance: 0,
      affixHeaderWidth: '',
      isScrollXStart: true,
      isScrollXEnd: false,
      expendRender: null,
      resizeProxyVisible: false,
      windowScrolled: false,
      scrollerDisabled: false
    }
  },

  props: {
    data: {
      type: Array,
      default () {
        return []
      }
    },

    defaultColumnWidth: {
      type: Number,
      default: 80
    },

    // 列的宽度是否自撑开
    fit: {
      type: Boolean,
      default: true
    },

    // 是否斑马线
    stripe: Boolean,

    // 是否全边框
    border: Boolean,

    height: [String, Number],

    // 是否将 Table 限定在父容器内，当 Table 的高度超过父容器时，将出现垂直滚动条
    restrict: {
      type: Boolean,
      default: false
    },

    // 吸附在距离窗口顶部固定位置的效果
    affix: [Boolean, Number],

    emptyText: {
      tepe: String,
      default () {
        return t('ans.table.emptyText')
      }
    },

    defaultSort: Object,

    defaultSortOrders: Array,

    defaultExpandAll: {
      type: Boolean,
      default: false
    },

    expandRowKeys: Array,

    // 合并行或列的计算方法
    cellSpanMethod: Function,

    // 表格行的 key 值，用于优化渲染
    rowKey: String,

    reserveStates: {
      type: Boolean,
      default: false
    },

    childrenProp: {
      type: String,
      default: 'children'
    },

    // tree table 是否默认打开
    defaultUnfoldChildren: {
      type: Boolean,
      default: false
    },

    showHeader: {
      type: Boolean,
      default: true
    },

    // 是否启用内部分页
    internalPaging: {
      type: Boolean,
      default: false
    },

    // 内部分页模式下，最多同时存在的行数
    rowLimit: {
      type: Number,
      default: 100
    },

    // 未设置 table 高度时，激活上一页/下一页时的边界距离
    pagingActiveDistance: {
      type: Number,
      default: 300
    },

    // 是否反转 Y 轴滚轮，当该值为 true 时，滚动 Y 轴将控制水平方向的滚动
    reverseScrollY: {
      type: Boolean,
      default: false
    },

    scrollBarClass: String
  },

  computed: {
    hasBorder () {
      return this.border || this.multiLayerHeader
    },

    wrapperClasses () {
      return [
        `${LIB_NAME}-table`,
        { [`${LIB_NAME}-table--border`]: this.hasBorder },
        { 'scrollable-x': this.layout.scrollX },
        { 'scrollable-y': this.layout.scrollY },
        { 'affix-table-header': this.affix }
      ]
    },

    bodyHeight () {
      if (this.height || this.restrict) {
        return {
          height: this.layout.bodyHeight ? this.layout.bodyHeight + 'px' : ''
        }
      }
      return {}
    },

    fixedBodyHeight () {
      if (this.height || this.restrict) {
        const { bodyHeight } = this.layout
        const fixedBodyHeight = bodyHeight
        return {
          height: fixedBodyHeight ? fixedBodyHeight + 'px' : ''
        }
      }
      return {}
    },

    fixedLeftHeaderStyles () {
      if (this.activeAffix) {
        return {
          width: this.layout.fixedLeftWidth + 'px',
          position: 'fixed',
          top: this.affixDistance + 'px'
        }
      } else {
        return null
      }
    },

    fixedRightHeaderStyles () {
      if (this.activeAffix) {
        return {
          width: this.layout.fixedRightWidth + 'px',
          position: 'fixed',
          top: this.affixDistance + 'px'
        }
      } else {
        return null
      }
    },

    bodyWidth () {
      const { bodyWidth } = this.layout
      return bodyWidth ? bodyWidth + 'px' : ''
    },

    shouldUpdateHeight () {
      return this.height ||
        this.restrict ||
        this.fixedLeftColumns.length > 0 ||
        this.fixedRightColumns.length > 0
    },

    affixDistance () {
      return typeof this.affix === 'number' ? this.affix : this.defaultAffixDistance
    },

    bodyTopMargin () {
      return this.showHeader && this.activeAffix ? this.$refs.headerWrapper.offsetHeight + 'px' : 0
    },

    fixedLeftColumns () {
      return this.store.states.fixedLeftColumns
    },

    fixedRightColumns () {
      return this.store.states.fixedRightColumns
    },

    bodyScrollbarClass () {
      return this.scrollBarClass ? `${this.scrollBarClass} table-body-scroller` : 'table-body-scroller'
    }
  },

  watch: {
    data: {
      immediate: true,
      handler (newVal) {
        this.store.setData(newVal)
        if (this.$ready) {
          this.$nextTick(() => {
            this.doLayout()
          })
        }
      }
    },

    expandRowKeys: {
      immediate: true,
      handler (newVal) {
        if (newVal) {
          this.store.setExpandRowKeys(newVal)
        }
      }
    },

    height: {
      immediate: true,
      handler (newVal) {
        this.layout.setHeight(newVal)
      }
    },

    multiLayerHeader () {
      this.$nextTick(() => this.doLayout())
    }
  },

  methods: {
    setScrollPosition (position) {
      if (['left', 'right'].includes(position) && !this.layout.scrollX) {
        position = 'left'
      } else if (['top', 'bottom'].includes(position) && !this.layout.scrollY) {
        position = 'top'
      }
      if (position === 'left') {
        this.$refs.headerScroller && this.$refs.headerScroller.stickToBoundary(false, true, false)
        this.$refs.scroller.stickToBoundary(false, true, false)
        this.isScrollXStart = true
        this.isScrollXEnd = false
      } else if (position === 'right') {
        this.$refs.headerScroller && this.$refs.headerScroller.stickToBoundary(false, false, false)
        this.$refs.scroller.stickToBoundary(false, false, false)
        this.isScrollXStart = false
        this.isScrollXEnd = true
      } else if (position === 'top') {
        this.$refs.fixedLeftScroller && this.$refs.fixedLeftScroller.stickToBoundary(true, true, false)
        this.$refs.fixedRightScroller && this.$refs.fixedRightScroller.stickToBoundary(true, true, false)
        this.$refs.scroller.stickToBoundary(true, true, false)
      } else if (position === 'bottom') {
        this.$refs.fixedLeftScroller && this.$refs.fixedLeftScroller.stickToBoundary(true, false, false)
        this.$refs.fixedRightScroller && this.$refs.fixedRightScroller.stickToBoundary(true, false, false)
        this.$refs.scroller.stickToBoundary(true, false, false)
      }
    },

    toggleAllSelection () {
      this.store.toggleAllSelection()
    },

    toggleRowSelection (row, selected) {
      this.store.toggleRowSelection(row, selected)
    },

    clearSelection () {
      this.store.clearSelection()
    },

    toggleRowExpansion (row, expanded) {
      this.store.toggleRowExpansion(row, expanded)
    },

    toggleRowUnfolding (row, unfolded) {
      this.store.toggleRowUnfolding(row, unfolded)
    },

    clearSort () {
      this.store.clearSort()
    },

    sort (prop, order) {
      this.store.sort(prop, order)
    },

    bindEvent () {
      if (this.affix || this.internalPaging) {
        window.addEventListener('scroll', this.windowScrollListener)
      }

      if (this.fit) {
        window.addEventListener('resize', this.resizeListener)
      }
    },

    handleStartDragBar (vertical) {
      if (vertical) {
        if (this.$refs.fixedLeftScroller) this.$refs.fixedLeftScroller.bodyDragging = true
        if (this.$refs.fixedRightScroller) this.$refs.fixedRightScroller.bodyDragging = true
      } else {
        this.$refs.headerScroller.bodyDragging = true
      }
    },

    handleEndDragBar (vertical) {
      if (vertical) {
        if (this.$refs.fixedLeftScroller) this.$refs.fixedLeftScroller.bodyDragging = false
        if (this.$refs.fixedRightScroller) this.$refs.fixedRightScroller.bodyDragging = false
      } else {
        this.$refs.headerScroller.bodyDragging = false
      }
    },

    handleHeaderScrollX (left) {
      this.isScrollXStart = false
      this.isScrollXEnd = false
      if (this.layout.scrollX) {
        this.$refs.scroller.setContentLeft(left)
      }
    },

    handleFixedScrollY (top) {
      this.$refs.scroller.setContentTop(top)
      if (this.$refs.fixedLeftScroller) {
        this.$refs.fixedLeftScroller.setContentTop(top)
      }
      if (this.$refs.fixedRightScroller) {
        this.$refs.fixedRightScroller.setContentTop(top)
      }
    },

    windowScrollListener () {
      if (this.affix) {
        const boundingRect = this.$el.getBoundingClientRect()
        this.activeAffix = boundingRect.top < this.affixDistance &&
          boundingRect.top + this.$el.offsetHeight > this.$refs.headerWrapper.offsetHeight
        this.updateAffixHeaderWidth()
      }
      if (this.windowScrolled && !this.layout.scrollY && this.store.states.paging) {
        const bodyWrapper = this.$refs.bodyWrapper
        const y = bodyWrapper.getBoundingClientRect().top
        if (y + bodyWrapper.clientHeight < window.innerHeight + this.pagingActiveDistance) {
          if (!this.layout.slicing) {
            this.store.nextPage()
          }
        } else if (y > -this.pagingActiveDistance) {
          if (!this.layout.slicing) {
            this.store.prevPage()
          }
        }
      }
      this.windowScrolled = true
    },

    handleBodyScrollX (left) {
      this.isScrollXStart = false
      this.isScrollXEnd = false
      if (this.showHeader) {
        this.$refs.headerScroller.setContentLeft(left)
      }
    },

    handleBodyScrollY (top) {
      if (this.$refs.fixedLeftScroller) {
        this.$refs.fixedLeftScroller.setContentTop(top)
      }
      if (this.$refs.fixedRightScroller) {
        this.$refs.fixedRightScroller.setContentTop(top)
      }
    },

    handleBodyScrollXStart () {
      this.isScrollXStart = true
      this.$emit('on-hit', 'left')
    },

    handleBodyScrollXEnd () {
      this.isScrollXEnd = true
      this.$emit('on-hit', 'right')
    },

    handleBodyScrollYStart () {
      if (this.store.states.paging) {
        if (!this.layout.slicing) {
          this.store.prevPage()
        }
      }
      this.$emit('on-hit', 'top')
    },

    handleBodyScrollYEnd () {
      if (this.store.states.paging) {
        if (!this.layout.slicing) {
          this.store.nextPage()
        }
      }
      this.$emit('on-hit', 'bottom')
    },

    resizeListener () {
      if (!this.$ready) return
      let shouldUpdateLayout = false
      const el = this.$el
      const { width: oldWidth, height: oldHeight } = this.resizeState

      const width = el.offsetWidth
      if (oldWidth !== width) {
        shouldUpdateLayout = true
      }

      const height = el.offsetHeight
      if ((this.height || this.shouldUpdateHeight) && oldHeight !== height) {
        shouldUpdateLayout = true
      }

      if (shouldUpdateLayout) {
        this.resizeState.width = width
        this.resizeState.height = height
        this.doLayout()
      }
    },

    doLayout (updateHeight) {
      this.layout.updateColumnsWidth()
      if (this.affix) {
        this.updateAffixHeaderWidth()
      }
      if (updateHeight || this.shouldUpdateHeight) {
        this.layout.updateElsHeight()
      }
    },

    updateAffixHeaderWidth () {
      this.affixHeaderWidth = this.activeAffix ? this.$el.clientWidth + 'px' : ''
    },

    checkScrollPosition () {
      if (this.store.states.hasFixedTable && this.isScrollXEnd) {
        this.$nextTick(() => {
          this.$refs.headerScroller.stickToBoundary(false, false, false)
          this.$refs.scroller.stickToBoundary(false, false, false)
        })
      }
    },

    checkScrollable () {
      if (this.$refs.scroller) {
        this.$refs.scroller.checkScrollable()
      }
      if (this.$refs.headerScroller) {
        this.$refs.headerScroller.checkScrollable()
      }
      if (this.$refs.fixedLeftScroller) {
        this.$refs.fixedLeftScroller.checkScrollable()
      }
      if (this.$refs.fixedRightScroller) {
        this.$refs.fixedRightScroller.checkScrollable()
      }
    },

    moveBodyTopByDiff (diff) {
      const top = this.$refs.scroller.currentTop - diff
      this.setBodyTop(top)
    },

    setBodyTop (top) {
      this.$refs.scroller.setContentTop(top, false)
      if (this.$refs.fixedLeftScroller) {
        this.$refs.fixedLeftScroller.setContentTop(top, false)
      }
      if (this.$refs.fixedRightScroller) {
        this.$refs.fixedRightScroller.setContentTop(top, false)
      }
    },

    windowLoadListener () {
      this.doLayout()
    }
  },

  created () {
    this.tableId = `${LIB_NAME}-table_${tableIdSeed++}`
    if (this.internalPaging && !this.rowKey) {
      throw new Error('Table: Prop row-key should not be empty when internal-paging enabled.')
    }
    window.addEventListener('load', this.windowLoadListener)
  },

  mounted () {
    this.resizeState = {
      width: this.$el.offsetWidth,
      height: this.$el.offsetHeight
    }
    this.$nextTick(() => {
      this.$ready = true
      this.store.updateColumns()
      this.doLayout(true)
      if (this.defaultSort && typeof this.defaultSort === 'object') {
        this.sort(this.defaultSort.prop, this.defaultSort.order)
      }
      this.bindEvent()
    })
  },

  destroyed () {
    window.removeEventListener('load', this.windowLoadListener)
    window.removeEventListener('scroll', this.windowScrollListener)
    window.removeEventListener('resize', this.resizeListener)
  }
}
</script>
