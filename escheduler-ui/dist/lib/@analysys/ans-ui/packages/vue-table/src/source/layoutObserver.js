export default {
  created () {
    this.tableLayout.addObserver(this)
  },

  destroyed () {
    this.tableLayout.removeObserver(this)
  },

  computed: {
    tableLayout () {
      let layout = this.layout
      if (!layout && this.table) {
        layout = this.table.layout
      }
      if (!layout) {
        throw new Error('Layout Observer: Can\'t find table layout.')
      }
      return layout
    }
  },

  methods: {
    onColumnsChange () {
      const cols = this.$el.querySelectorAll('colgroup > col')
      if (!cols.length) return
      const columnsMap = {}
      this.store.states.leafColumns.forEach((column) => {
        columnsMap[column.id] = column
      })
      for (let i = 0, j = cols.length; i < j; i++) {
        const col = cols[i]
        const name = col.getAttribute('name')
        const column = columnsMap[name]
        if (column) {
          col.setAttribute('width', column.currentWidth)
        }
      }
    }
  }
}
