export default {
  name: 'CellRenderer',
  functional: true,
  props: {
    expand: {
      type: Boolean,
      default: false
    },
    header: {
      type: Boolean,
      default: false
    },
    render: Function,
    row: Object,
    column: Object,
    content: [String, Number, Object],
    rIndex: Number,
    cIndex: Number
  },
  render: (h, ctx) => {
    let params
    if (ctx.props.expand) {
      params = {
        row: ctx.props.row,
        $index: ctx.props.rIndex
      }
    } else if (ctx.props.header) {
      params = {
        column: ctx.props.column,
        $index: ctx.props.cIndex
      }
    } else {
      params = {
        row: ctx.props.row,
        column: ctx.props.column,
        content: ctx.props.content,
        $rowIndex: ctx.props.rIndex,
        $columnIndex: ctx.props.cIndex
      }
    }
    return ctx.props.render(h, params)
  }
}
