<template>
  <div><slot></slot></div>
</template>

<script>
let columnIdSeed = 1

const defaults = {
  default: {
    order: ''
  },
  selection: {
    width: 38,
    minWidth: 38
  },
  expand: {
    width: 38,
    minWidth: 38
  }
}

const getDefaultColumn = (type, options) => {
  const column = Object.assign({}, defaults[type || 'default'])

  for (let name in options) {
    if (options.hasOwnProperty(name)) {
      const value = options[name]
      if (typeof value !== 'undefined') {
        column[name] = value
      }
    }
  }

  if (column.width !== undefined) {
    column.width = parseInt(column.width)
  }

  column.currentWidth = column.width || column.minWidth

  return column
}

export default {
  name: 'xTableColumn',

  props: {
    // 列类型，可选 selection——复选框列
    type: {
      type: String,
      default: 'default'
    },

    // 当前列对应的表头文本
    label: String,

    // 当前列属性值
    prop: String,

    // 列宽
    width: [Number, String],

    // 是否固定列，也可传字符串 `left` 或  `right`
    fixed: [Boolean, String],

    // 是否可排序
    sortable: {
      type: [String, Boolean],
      default: false
    },

    // 排序方法
    sortMethod: Function,

    // 排序轮询顺序，默认使用 table 的 defaultSortOrders
    sortOrders: Array,

    // 是否可以通过拖拽右侧边框改变列宽
    resizable: {
      type: Boolean,
      default: true
    },

    formatter: Function,

    align: {
      type: String,
      validator (v) {
        return ['left', 'center', 'right'].includes(v)
      },
      default: 'left'
    },

    headerAlign: {
      type: String,
      validator (v) {
        return ['left', 'center', 'right'].includes(v)
      },
      default: 'left'
    }
  },

  computed: {
    owner () {
      let parent = this.$parent
      while (parent && !parent.tableId) {
        parent = parent.$parent
      }
      return parent
    },

    columnOrTableParent () {
      let parent = this.$parent
      while (parent && !parent.tableId && !parent.columnId) {
        parent = parent.$parent
      }
      return parent
    }
  },

  watch: {
    label (newVal) {
      if (this.columnConfig) {
        this.columnConfig.label = newVal
      }
    },

    prop (newVal) {
      if (this.columnConfig) {
        this.columnConfig.prop = newVal
      }
    },

    width (newVal) {
      if (this.columnConfig) {
        this.columnConfig.width = typeof newVal === 'string' ? parseInt(newVal) : newVal
        this.owner.doLayout()
      }
    },

    fixed (newVal) {
      if (this.columnConfig) {
        this.columnConfig.fixed = newVal
        this.owner.store.updateColumns()
        this.owner.doLayout()
      }
    },

    sortable (newVal) {
      if (this.columnConfig) {
        this.columnConfig.sortable = newVal
      }
    }
  },

  created () {
    const { owner, columnOrTableParent: parent } = this

    this.isSubColumn = owner !== parent
    this.columnId = (parent.tableId || parent.columnId) + '_column_' + columnIdSeed++

    defaults.default.minWidth = this.owner.defaultColumnWidth

    this.columnConfig = getDefaultColumn(this.type, {
      id: this.columnId,
      type: this.type,
      label: this.label,
      prop: this.prop,
      width: this.width,
      fixed: this.fixed,
      sortable: this.sortable === '' ? true : this.sortable,
      sortMethod: this.sortMethod,
      sortOrders: this.sortOrders,
      resizable: this.resizable,
      formatter: this.formatter,
      align: this.align,
      headerAlign: this.headerAlign
    })
  },

  mounted () {
    const {
      owner,
      columnOrTableParent: parent,
      isSubColumn,
      type,
      columnConfig,
      $el,
      $scopedSlots
    } = this

    let columnIndex
    if (!isSubColumn) {
      columnIndex = [].indexOf.call(parent.$refs.hiddenContent.children, $el)
    } else {
      columnIndex = [].indexOf.call(parent.$el.children, $el)
    }

    // 自定义表头
    if ($scopedSlots.header) {
      if (type === 'selection') {
        throw new Error('Table Column: Selection column doesn\'t allow to set scoped-slot header.')
      } else {
        columnConfig.customHeader = (h, scope) => $scopedSlots.header(scope)
      }
    }
    // 自定义列
    if ($scopedSlots.content) {
      columnConfig.customRender = (h, scope) => $scopedSlots.content(scope)
    }

    // 展开行
    if ($scopedSlots.expand) {
      owner.expendRender = (h, scope) => $scopedSlots.expand(scope)
    }

    // 表头前置插槽
    if ($scopedSlots.prepend) {
      columnConfig.prependHeader = (h, scope) => $scopedSlots.prepend(scope)
    }

    // 表头后置插槽
    if ($scopedSlots.append) {
      columnConfig.appendHeader = (h, scope) => $scopedSlots.append(scope)
    }

    // 表头文本插槽
    if ($scopedSlots.headerText) {
      columnConfig.headerText = (h, scope) => $scopedSlots.headerText(scope)
    }

    owner.store.commit('insertColumn', columnConfig, columnIndex, isSubColumn ? parent.columnConfig : null)
  },

  destroyed () {
    if (!this.$parent) return
    const parent = this.$parent
    this.owner.store.commit('removeColumn', this.columnConfig, this.isSubColumn ? parent.columnConfig : null)
  }
}
</script>
