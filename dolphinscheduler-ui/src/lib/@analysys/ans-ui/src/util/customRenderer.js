export default {
  name: 'CustomRenderer',
  functional: true,
  props: {
    render: Function,
    params: {
      default () {
        return {}
      }
    }
  },
  render: (h, ctx) => {
    return ctx.props.render(h, ctx.params)
  }
}
