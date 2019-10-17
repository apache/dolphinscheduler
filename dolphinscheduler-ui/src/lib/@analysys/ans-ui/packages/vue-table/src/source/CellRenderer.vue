<script>
export default {
  name: 'CellRenderer',
  props: {
    expand: {
      type: Boolean,
      default: false
    },
    header: {
      type: Boolean,
      default: false
    },
    customRender: Function,
    row: Object,
    column: Object,
    content: [String, Number, Object],
    rIndex: Number,
    cIndex: Number
  },
  render: function (h) {
    let params
    if (this.expand) {
      params = {
        row: this.row,
        $index: this.rIndex
      }
    } else if (this.header) {
      params = {
        column: this.column,
        $index: this.cIndex
      }
    } else {
      params = {
        row: this.row,
        column: this.column,
        content: this.content,
        $rowIndex: this.rIndex,
        $columnIndex: this.cIndex
      }
    }
    const result = this.customRender(h, params)
    if (!result) {
      return null
    }
    if (Array.isArray(result)) {
      return result.length === 1 ? result[0] : h('div', result)
    }
    return result
  }
}
</script>
