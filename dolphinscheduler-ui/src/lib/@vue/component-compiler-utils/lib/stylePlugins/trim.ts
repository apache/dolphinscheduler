import { Root } from 'postcss'
import * as postcss from 'postcss'

export default postcss.plugin('trim', () => (css: Root) => {
  css.walk(({ type, raws }) => {
    if (type === 'rule' || type === 'atrule') {
      raws.before = raws.after = '\n'
    }
  })
})
