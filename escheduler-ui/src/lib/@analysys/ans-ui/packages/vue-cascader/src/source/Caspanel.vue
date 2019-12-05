<template>
  <span style="white-space: nowrap">
    <ul v-if="data && data.length" :class="prefixCls+ '__menu'">
      <li v-for="item in data"
          @click.stop="handleClickItem(item)"
          @mouseenter.stop="handleHoverItem(item)"
          :class="itemClass(item)">
        <a v-html="item.html || item.label" href="javascript:;"></a>
        <i :class="iconClass(item)"></i>
      </li>
    </ul>
    <Caspanel
      :data="subList" :trigger="trigger"
      :prefix-cls="prefixCls"
      :multiple="multiple"
      v-show="curItem.length"
      :change-on-select=changeOnSelect
      style="margin-left: -5px;"
      v-if="subList && subList.length">
    </Caspanel>
  </span>
</template>
<script>
  import { emitter, findComponentDownward, scrollIntoView } from '../../../../src/util'
  const PARENT_NAME = 'xCascader'
  const PANEL_NAME = 'Caspanel'

  export default {
    name: PANEL_NAME,
    mixins: [emitter],
    data() {
      return {
        subList: [],

        // 选中的item
        curItem: [],
      }
    },
    watch: {
      data () {
        this.subList = []
        this.curItem = []
      }
    },
    props: {
      data: {
        type: Array,
        default: () => []
      },
      trigger: {
        validator (value) {
          return ['click', 'hover'].indexOf(value) > -1
        },
        default: 'click'
      },
      changeOnSelect: Boolean,
      prefixCls: String,
      multiple: Boolean
    },
    methods: {
      getCurItem(item, fromInit) {
        const baseItem = this.getBaseItem(item)

        if (fromInit) {
          this.curItem.push(baseItem)
        } else if (this.multiple) {
          const index = this.inTmp(baseItem)
          if (!~index) {
            this.hasChildren(item) && (this.curItem = [])
            this.curItem.forEach((v, i) => {
              const id = this.data.findIndex(t => t.value === v.value && this.hasChildren(t));
              ~id && this.curItem.splice(i, 1)
            })
            this.curItem.push(baseItem)
          } else {
            this.curItem.splice(index, 1)
          }
        } else {
          this.curItem = [baseItem]
        }
      },
      // 处理item触发
      handleTriggerItem (item, fromInit) {
        if (item.disabled) return

        this.subList = []

        this.hasChildren(item) && (this.subList = item.children)
        this.getCurItem(item, fromInit)
        this.emitUpdate(this.curItem);

        this.dispatch(PARENT_NAME, 'on-result-change', {
          lastValue: !this.hasChildren(item),
          changeOnSelect: this.changeOnSelect,
          fromInit: fromInit
        });
      },
      handleClickItem(item) {
        if (this.trigger !== 'click' && this.hasChildren(item)) return
        this.handleTriggerItem(item)
      },
      handleHoverItem(item) {
        if (this.trigger !== 'hover' || !this.hasChildren(item)) return
        this.handleTriggerItem(item)
      },
      getBaseItem (item) {
        let backItem = Object.assign({}, item);
        backItem.children && delete backItem.children;

        return backItem;
      },
      updateResult (item) {
        this.emitUpdate(this.result = this.curItem.concat(item))
      },
      // 调用父级方法 更新result
      emitUpdate (result) {
        this.$parent.updateResult(result[0] && result[0].__label != undefined  ? [result] : result);
      },
      inTmp(item) {
        return this.curItem.findIndex(t => t.value === item.value && t.label === item.label)
      },
      itemClass(item) {
        return [
          `${this.prefixCls}__list`,
          {
            'active': ~this.inTmp(item),
            'disabled': item.disabled
          }
        ]
      },
      iconClass(item) {
        return [
          {
            'selected-mark': !this.hasChildren(item) && this.multiple,
            'ans-icon-arrow-right': this.hasChildren(item)
          }
        ]
      },
      hasChildren(item) {
        return item && item.children && item.children.length
      },
      onFindSelected() {
        this.$on('on-find-selected', (params) => {
          const val = params.value;
          let value = [...val];

          for (let i = 0; i < value.length; i++) {
            for (let j = 0; j < this.data.length; j++) {
              if (typeof value[i] === 'object') {
                value[i].forEach(t => {
                  if (t === this.data[j].value) {
                    value[i].splice(0, 1)
                    this.handleTriggerItem(this.data[j], true);
                    this.$nextTick(() => {
                      this.broadcast(PANEL_NAME, 'on-find-selected', {value: value});
                    });
                    return false
                  }
                })
              } else if (value[i] === this.data[j].value) {
                value.splice(0, 1);
                this.handleTriggerItem(this.data[j], true);
                this.$nextTick(() => {
                  this.broadcast(PANEL_NAME, 'on-find-selected', {value: value});
                });
                return false;
              }
            }
          }
        });
      },
      onClear() {
        this.$on('on-clear', (deep = false) => {
          this.sublist = [];
          this.curItem = [];
          if (deep) {
            const panel = findComponentDownward(this, PANEL_NAME);
            panel && panel.$emit('on-clear', true);
          }
        });
      },
      onVisibleChange() {
        let _this = this

        _this.$on('on-visible-change', (val) => {
          val && _this.$nextTick(() => {
            let actives = document.getElementsByClassName('ans-cascader-drop__list active')
            for (let item = 0; item < actives.length; item ++) {
              scrollIntoView(actives[item].parentNode, actives[item])
            }

            this.broadcast(PANEL_NAME, 'drop-visible-change', val);
            actives = null
          })
        })
      }
    },
    mounted () {
      // 初始化已选的值
      this.onFindSelected()

      // 清空
      this.onClear()

      this.onVisibleChange()
    },
    beforeDestroy() {
      this.$off('on-find-selected')
      this.$off('on-clear')
      this.$off('on-visible-change')
    }
  }
</script>