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
        :inner-width="scrollerContentWidth"
        :show-scrollbar="false"
        :disabled="scrollerDisabled"
        :reverse-scroll-y="reverseScrollY"
        @on-scroll-x="handleHeaderScrollX">
        <x-table-header
          :style="{
            width: bodyWidth,
            transform: transformX ? `translateX(${transformX}px)` : undefined
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
      ref="bodyWrapper">
      <x-scroller
        width="100%"
        height="100%"
        :inner-width="scrollerContentWidth"
        :inner-height="scrollerContentHeight"
        ref="scroller"
        :scrollbar-class="bodyScrollbarClass"
        :reverse-scroll-y="reverseScrollY"
        :disabled="scrollerDisabled"
        :bar-offset-left="layout.fixedLeftWidth"
        :bar-offset-right="layout.fixedRightWidth"
        @on-scroll-x="handleBodyScrollX"
        @on-scroll-y="handleBodyScrollY"
        @on-start-drag-bar="handleStartDragBar"
        @on-end-drag-bar="handleEndDragBar">
        <x-table-body
          ref="bodyTable"
          :style="tableBodyStyles"
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
      v-show="layout.scrollX"
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
          fixedBodyHeight,
          { width: this.layout.tableWrapperWidth + 'px' }
        ]"
        class="table-body-wrapper"
        ref="fixedLeftBodyWrapper">
        <x-scroller
          ref="fixedLeftScroller"
          width="100%"
          height="100%"
          :inner-width="scrollerContentWidth"
          :inner-height="scrollerContentHeight"
          :show-scrollbar="false"
          :disabled="scrollerDisabled || 'x'"
          @on-scroll-y="handleFixedScrollY">
          <x-table-body
            fixed="left"
            :style="fixedBodyStyles"
            :store="store"
            :stripe="stripe"
            :border="hasBorder">
          </x-table-body>
        </x-scroller>
      </div>
    </div>
    <div
      v-if="fixedRightColumns.length > 0"
      v-show="layout.scrollX"
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
          :style="fixedRightTableHeaderStyles"
          :store="store"
          :stripe="stripe"
          :border="hasBorder">
        </x-table-header>
      </div>
      <div
        :style="[
          { top: layout.headerHeight + 'px' },
          fixedBodyHeight,
          { width: this.layout.tableWrapperWidth + 'px' }
        ]"
        class="table-body-wrapper"
        ref="fixedRightBodyWrapper">
        <x-scroller
          ref="fixedRightScroller"
          width="100%"
          height="100%"
          :inner-width="scrollerContentWidth"
          :inner-height="scrollerContentHeight"
          :show-scrollbar="false"
          :disabled="scrollerDisabled || 'x'"
          @on-scroll-y="handleFixedScrollY">
          <x-table-body
            fixed="right"
            :style="[fixedBodyStyles, fixedRightBodyStyles]"
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
import { debounce } from 'throttle-debounce'
import { LIB_NAME } from '../../../../src/util'
import { xScroller } from '../../../vue-scroller/src'
import TableStore from './store.js'
import TableLayout from './layout.js'
import xTableBody from './TableBody'
import xTableHeader from './TableHeader'
import { t } from '../../../../src/locale'

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
      resizeState: {
        width: null,
        height: null
      },
      activeAffix: false,
      defaultAffixDistance: 0,
      affixHeaderWidth: '',
      isScrollXStart: true,
      isScrollXEnd: false,
      isScrollYStart: true,
      isScrollYEnd: false,
      expendRender: null,
      resizeProxyVisible: false,
      scrollerDisabled: false,
      translateY: 0
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

    // 表格行的 key 值，当 reserve-states 为 true 时，必须设置该属性
    rowKey: String,

    currentRowKey: [String, Number],

    reserveStates: {
      type: Boolean,
      default: false
    },

    childrenProp: {
      type: String,
      default: 'children'
    },

    // 树结构的第一列是否需要加上 title 属性
    treeTitle: {
      type: Boolean,
      default: false
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

    // 是否启用虚拟滚动
    virtualScroll: {
      type: Boolean,
      default: false
    },

    // 行高，启动虚拟滚动时需要设置
    rowHeight: {
      type: Number,
      default: 38
    },

    // 当表格为树结构时，开启虚拟滚动后，最大渲染的行数
    maxTreeRow: {
      type: Number,
      default: 15
    },

    // 是否反转 Y 轴滚轮，当该值为 true 时，滚动 Y 轴将控制水平方向的滚动
    reverseScrollY: {
      type: Boolean,
      default: false
    },

    scrollBarClass: String,

    frozenData: {
      type: Boolean,
      default: true
    }
  },

  computed: {
    hasBorder () {
      return this.border || this.store.states.multiLayer
    },

    wrapperClasses () {
      return [
        `${LIB_NAME}-table`,
        { [`${LIB_NAME}-table--border`]: this.hasBorder },
        { 'scrollable-x': this.layout.scrollX },
        { 'scrollable-y': this.layout.scrollY },
        { 'hit-left': this.layout.scrollX && this.isScrollXStart },
        { 'hit-right': this.layout.scrollX && this.isScrollXEnd },
        { 'hit-top': this.layout.scrollY && this.isScrollYStart },
        { 'hit-bottom': this.layout.scrollY && this.isScrollYEnd },
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
          width: this.layout.tableWrapperWidth + 'px',
          position: 'fixed',
          top: this.affixDistance + 'px'
        }
      } else {
        return {
          width: this.layout.tableWrapperWidth + 'px'
        }
      }
    },

    fixedRightHeaderStyles () {
      if (this.activeAffix) {
        return {
          width: this.layout.tableWrapperWidth + 'px',
          position: 'fixed',
          top: this.affixDistance + 'px',
          left: this.$el.getBoundingClientRect().left + 'px'
        }
      } else {
        return {
          width: this.layout.tableWrapperWidth + 'px'
        }
      }
    },

    bodyWidth () {
      const { bodyWidth, tableWidth, scrollX } = this.layout
      if (this.virtualScroll && scrollX) {
        return tableWidth ? tableWidth + 'px' : ''
      }
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
    },

    scrollerContentWidth () {
      return this.layout.scrollX && this.virtualScroll ? this.layout.bodyWidth + 'px' : undefined
    },

    scrollerContentHeight () {
      return this.store.states.transformBodyY ? this.store.states.contentHeight + 'px' : undefined
    },

    transformX () {
      return this.layout.scrollX && this.store.states.transformXAmount
        ? this.store.states.transformXAmount
        : 0
    },

    tableBodyStyles () {
      const result = {
        width: this.bodyWidth
      }
      if (this.virtualScroll) {
        result['will-change'] = 'transform'
        const x = this.transformX
        if (this.store.states.transformBodyY) {
          result.transform = `translate(${x}px, ${this.translateY}px)`
        } else {
          result.transform = `translateX(${x}px)`
        }
      }
      return result
    },

    fixedBodyStyles () {
      const result = {}
      if (this.store.states.transformBodyY) {
        result['will-change'] = 'transform'
        result.transform = `translateY(${this.translateY}px)`
      }
      if (this.layout.scrollX && this.virtualScroll) {
        result.width = this.bodyWidth
      } else {
        result.width = '100%'
      }
      return result
    },

    fixedRightTransformX () {
      const { tableWidth, bodyWidth } = this.layout
      return bodyWidth - tableWidth
    },

    fixedRightTableHeaderStyles () {
      return {
        width: '100%',
        position: 'relative',
        left: this.$refs.scroller.minLeft + this.fixedRightTransformX + 'px'
      }
    },

    fixedRightBodyStyles () {
      return this.transformX ? { transform: `translateX(${this.fixedRightTransformX}px)` } : {}
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
        this.store.states.transformXAmount = 0
      } else if (position === 'right') {
        this.$refs.headerScroller && this.$refs.headerScroller.stickToBoundary(false, false, false)
        this.$refs.scroller.stickToBoundary(false, false, false)
        this.isScrollXStart = false
        this.isScrollXEnd = true
      } else if (position === 'top') {
        this.$refs.fixedLeftScroller && this.$refs.fixedLeftScroller.stickToBoundary(true, true, false)
        this.$refs.fixedRightScroller && this.$refs.fixedRightScroller.stickToBoundary(true, true, false)
        this.$refs.scroller.stickToBoundary(true, true, false)
        this.isScrollYStart = true
        this.isScrollYEnd = false
      } else if (position === 'bottom') {
        this.$refs.fixedLeftScroller && this.$refs.fixedLeftScroller.stickToBoundary(true, false, false)
        this.$refs.fixedRightScroller && this.$refs.fixedRightScroller.stickToBoundary(true, false, false)
        this.$refs.scroller.stickToBoundary(true, false, false)
        this.isScrollYStart = false
        this.isScrollYEnd = true
      }
      if (this.virtualScroll) {
        this.store.sliceData()
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
      if (this.affix) {
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

    handleHeaderScrollX (left, hitLeft, hitRight) {
      this._setScrollXStates(hitLeft, hitRight)
      if (this.layout.scrollX) {
        this.$refs.scroller.setContentLeft(left)
      }
      this.store.throttleCalculateColumnIndexes(left)
      this.$emit('on-scroll', false, left)
    },

    _setScrollXStates (hitLeft, hitRight) {
      if (hitLeft) {
        this.isScrollXStart = true
        this.$emit('on-hit', 'left')
      } else if (hitRight) {
        this.isScrollXEnd = true
        this.$emit('on-hit', 'right')
      } else {
        this.isScrollXStart = false
        this.isScrollXEnd = false
      }
    },

    handleFixedScrollY (top, hitTop, hitBottom) {
      this._setScrollYStates(hitTop, hitBottom)
      this.$refs.scroller.setContentTop(top)
      this._afterScrollY(top)
    },

    _setScrollYStates (hitTop, hitBottom) {
      if (hitTop) {
        this.isScrollYStart = true
        this.$emit('on-hit', 'top')
      } else if (hitBottom) {
        this.isScrollYEnd = true
        this.$emit('on-hit', 'bottom')
      } else {
        this.isScrollYStart = false
        this.isScrollYEnd = false
      }
    },

    _afterScrollY (top) {
      if (this.$refs.fixedLeftScroller) {
        this.$refs.fixedLeftScroller.setContentTop(top)
      }
      if (this.$refs.fixedRightScroller) {
        this.$refs.fixedRightScroller.setContentTop(top)
      }
      this.store.throttleCalculateIndexes(top)
      this.$emit('on-scroll', true, top)
      this.updateTranslateY(undefined, top)
    },

    handleBodyScrollX (left, hitLeft, hitRight) {
      this._setScrollXStates(hitLeft, hitRight)
      if (this.showHeader) {
        this.$refs.headerScroller.setContentLeft(left)
      }
      this.store.throttleCalculateColumnIndexes(left)
      this.$emit('on-scroll', false, left)
    },

    handleBodyScrollY (top, hitTop, hitBottom) {
      this._setScrollYStates(hitTop, hitBottom)
      this._afterScrollY(top)
    },

    windowScrollListener () {
      const boundingRect = this.$el.getBoundingClientRect()
      this.activeAffix = boundingRect.top < this.affixDistance &&
        boundingRect.top + this.$el.offsetHeight > this.$refs.headerWrapper.offsetHeight
      this.updateAffixHeaderWidth()
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

      if (shouldUpdateLayout || this.restrict) {
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
      const { scroller, headerScroller, fixedLeftScroller, fixedRightScroller } = this.$refs
      if (!scroller) return

      const prevTop = scroller.currentTop
      const prevLeft = scroller.currentLeft
      if (prevLeft === 0) {
        this.store.states.transformXAmount = 0
      }
      scroller.checkScrollable()
      this.isScrollXStart = scroller.currentLeft === 0
      this.isScrollXEnd = scroller.currentLeft === scroller.minLeft
      this.isScrollYStart = scroller.currentTop === 0
      this.isScrollYEnd = scroller.currentTop === scroller.minTop
      if (Math.abs(prevTop - scroller.currentTop) > 1 || Math.abs(prevLeft - scroller.currentLeft) > 1) {
        return this.store.sliceData()
      }

      if (headerScroller) {
        headerScroller.checkScrollable()
      }
      if (fixedLeftScroller) {
        fixedLeftScroller.checkScrollable()
        if (scroller.currentTop !== fixedLeftScroller.currentTop) {
          fixedLeftScroller.setContentTop(scroller.currentTop)
        }
      }
      if (fixedRightScroller) {
        fixedRightScroller.checkScrollable()
        fixedRightScroller.stickToBoundary(false, false, false)
        if (scroller.currentTop !== fixedRightScroller.currentTop) {
          fixedRightScroller.setContentTop(scroller.currentTop)
        }
      }
    },

    windowLoadListener () {
      this.doLayout()
    },

    updateTranslateY (amount, top) {
      if (amount !== undefined) {
        this.translateY = amount
      } else {
        const { bodyHeight } = this.layout
        const tableHeight = this.$refs.bodyTable.$el.offsetHeight
        if (bodyHeight - top > this.translateY + tableHeight) {
          this.translateY = 1.5 * bodyHeight - top - tableHeight
        } else if (top + this.translateY > 0) {
          this.translateY = -top - 0.5 * bodyHeight
        }
      }
    }
  },

  created () {
    this.tableId = `${LIB_NAME}-table_${tableIdSeed++}`
    window.addEventListener('load', this.windowLoadListener)
    this.debouncedUpdateLayout = debounce(50, () => this.doLayout())
    this.debouncedCheckScrollable = debounce(10, () => this.checkScrollable())
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

  beforeDestroy () {
    this.store.states.destroying = true
    window.removeEventListener('load', this.windowLoadListener)
    window.removeEventListener('scroll', this.windowScrollListener)
    window.removeEventListener('resize', this.resizeListener)
  }
}
</script>
