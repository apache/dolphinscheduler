<template>
  <div class="pre_tasks-model">
    <div class="clearfix list">
      <div class="text-box">
        <span>{{$t('Pre tasks')}}</span>
      </div>
      <div class="cont-box">
        <div class="label-box">
          <x-select
              ref="preTasksSelector"
              style="width: 100%;"
              filterable
              multiple
              v-model="preTasks"
              :disabled="isDetails"
              :id="preTasksSelectorId">
            <x-option
                v-for="task in preTaskList"
                :key="task.id"
                :value="task.id"
                :label="task.name">
            </x-option>
          </x-select>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
  import disabledState from '@/module/mixin/disabledState'
  export default {
    name: 'pre_tasks',
    mixins: [disabledState],
    props: {
      backfillItem: Object
    },
    data () {
      return {
        preTasksSelectorId: '_preTasksSelectorId',    // 通过改变元素id来使子元素刷新
        preTasks: [],
        preTasksOld: [],
      }
    },
    mounted () {
      this.preTasks = this.backfillItem['preTasks'] || this.preTasks
      this.preTasksOld = this.preTasks
    
      // 通过改变元素id来使子元素刷新
      this.$nextTick(() => {
        this.preTasksSelectorId = 'preTasksSelectorId'
      })
    },
    computed: {
      // 下拉菜单的选项
      preTaskList: function () {
        let currentTaskId = this.backfillItem['id'] || this.id
        let cacheTasks = Object.assign({}, this.store.state.dag.tasks)
        let keys = Object.keys(cacheTasks)
        for (let i = 0; i < keys.length; i++) {
          let key = keys[i]
          if (!cacheTasks[key].id || !cacheTasks[key].name) {
            // 删掉undefined的数据
            delete cacheTasks[key]
          }
          else if (currentTaskId && cacheTasks[key].id === currentTaskId) {
            // 去掉当前task，自己不能当作自己的preTask
            delete cacheTasks[key]
          }
        }

        return cacheTasks
      },
      // 等待添加连线的preTasks
      preTasksToAdd: function () {
        let toAddTasks = this.preTasks.filter(taskId => {
          return (this.preTasksOld.indexOf(taskId) === -1)
        })
        return toAddTasks
      },
      // 等待删除连线的preTasks
      preTasksToDelete: function () {
        return this.preTasksOld.filter(taskId => this.preTasks.indexOf(taskId) === -1)
      },
    },
    methods: {
      // 供上层formModal点击提交按钮时调用的方法，会触发事件提交数据给上层
      _verification () {
        this.$emit('on-pre-tasks', {
          preTasks: this.preTasks,
          preTasksToAdd: this.preTasksToAdd,
          preTasksToDelete: this.preTasksToDelete,
        })
        return true
      }
    }
  }
</script>
