## Progress

用于展示操作进度，告知用户当前状态和预期。

### Progress props

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
percentage | 百分比，必选项 | Number | 0-100 | 0
type | 进度条类型 | String | line / circle | line
stroke-width | 进度条的宽度，单位 px | Number | — | 8
status | 进度条当前状态 | String | success / exception | —
color | 进度条背景色（会覆盖 status 状态颜色） | String | — | —
width | 环形进度条宽度（只在 type=circle 时有效） | Number | — | 100
show-inline-text | 是否显示进度条上（type=line 时）文字内容 | Boolean | — | false
show-outside-text | 是否显示进度条右侧文字内容（只在 type=line 时有效） | Boolean | — | true
show-circle-text | 是否显示环形进度条里面（type=circle 时）文字内容 | Boolean | — | true

### Progress slots

名称 | 说明
--- | ---
inline | type=line 时进度条上内容插槽
outside | type=line 时进度条右侧的内容插槽
circle | type=circle 时环形进度条里面的内容插槽