## Cascader

### Cascader props

| 属性 | 说明   | required | 类型 | 默认值 |
| :----| :------| :--------| :---:| :------|
| options | 可选项数据源，键名可通过 props 属性配置, 配置选项: { value, label, html, children, disabled } | Required | Array | - |
| prop | N/A | Optional | Object | {...} |
| value | 选中项绑定值 `v-model` | Optional | Array | {...} |
| separator | N/A | Optional | String | / |
| placeholder | N/A | Optional | String | 请选择 ... |
| disabled | N/A | Optional | Any | - |
| clearable | 是否支持清空选项 | Optional | Any | - |
| change-on-select | 是否允许选择任意一级的选项 | Optional | Any | - |
| popper-class | 自定义浮层类名 | Optional | Any | - |
| expand-trigger | 次级菜单的展开方式 [ click / hover ] | Optional | String | click |
| filterable | 是否可搜索选项 | Optional | Any | - |
| no-data-text | 无数据提示 | Optional | String | 暂无数据 |
| no-match-text | 搜索无结果提示 | Optional | String | 搜索无结果 |
| multiple | 是否多选 | Optional | Boolean | false |
| placement | 弹出位置 | Optional | String | bottom-start |
| distance | 与参考元素距离，单位为 px | Optional | Number | 1 |
| append-to-body | 弹出层是否插入 body | Optional | Boolean | false |
| position-fixed | 弹出层是否 fixed 定位 | Boolean | — | false |
| viewport | 弹出层是否基于 viewport 定位 | Boolean | — | false |
| popper-options | Popper.js 的可选项 | Optional | Object | — |

### Cascader events

 - `on-change` Fired when the selected value is changed.

---
