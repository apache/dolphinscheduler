/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {defineComponent, onMounted, toRefs, watch, nextTick, reactive} from 'vue'
import {useI18n} from 'vue-i18n'
import {useRoute} from 'vue-router'
import Card from '@/components/card'
import {IGanttRes, ITask, ITaskGantt} from "@/views/projects/workflow/instance/gantt/type"
import {queryProcessInstanceById, queryTaskListByProcessId, viewGanttTree} from "@/service/modules/process-instances"
import {TotalList} from "@/service/modules/task-instances/types";
import "./index.scss"

const workflowRelation = defineComponent({
    name: 'workflow-relation',
    setup() {
        const {t, locale} = useI18n()
        const route = useRoute()

        const variables = reactive({
            taskInfo: {},
            tasks: [] as ITask[],
            taskList: [] as TotalList[]
        })

        const id = Number(route.params.id)
        const code = Number(route.params.projectCode)

        const formatterGantt = () => {
            let taskGantt = [] as ITaskGantt[]
            let list = {} as ITaskGantt
            variables.tasks.forEach(item => {
                variables.taskList.forEach(task => {
                    if (item.taskName === task.name) {
                        list = {
                            ...task,
                            ...item,
                            startDateTime: task.startTime === null ? new Date().getTime() : new Date(task.startTime).getTime(),
                            endDateTime: task.endTime === null ? new Date().getTime() : new Date(task.endTime).getTime(),
                            submitDateTime: task.submitTime === null ? new Date().getTime() : new Date(task.submitTime).getTime()
                        }
                        taskGantt.push(list)
                    }
                })
            })
            createGanttChart(taskGantt)
        }

        const refreshGantt = () => {
            queryProcessInstanceById(id, code).then((res: any) => {
                variables.taskInfo = res.data
                if (res.state !== 'SUCCESS') {
                    getTaskList()
                    nextTick(() => {
                        setTimeout(() => {
                            refreshGantt()
                        }, 4000)
                    }).then(r => null)
                } else {
                    console.log("complete")
                }
            })
        }

        const getTaskList = () => {
            queryTaskListByProcessId(id, code).then((res: any) => {
                variables.taskList = res.taskList
                getGantt()
            })
        }

        const getGantt = () => {
            viewGanttTree(id, code).then((res: IGanttRes) => {
                variables.tasks = res.tasks
                formatterGantt()
            })
        }


        const createGanttChart = (tasks: ITaskGantt[]) => {
            const gantt: HTMLElement = document.getElementById('gantt') as HTMLElement;
            gantt.innerHTML = ''
            tasks.sort((a, b) => a.submitDateTime - b.startDateTime); // 按照开始时间排序

            // 检查元素是否已存在
            try {
                const querySelector: HTMLElement = document.querySelector('.tips-show') as HTMLElement
                querySelector.remove()
            } catch {
            }
            const tip: HTMLDivElement = document.createElement('div') as HTMLDivElement;
            tip.className = 'tips-show'
            tip.style.display = 'none'; // 默认隐藏
            document.body.appendChild(tip);

            for (let i = 0; i < tasks.length; i++) {
                const task = tasks[i];

                const row: HTMLDivElement = document.createElement('div') as HTMLDivElement;
                row.className = 'gantt-row';

                const taskName: HTMLDivElement = document.createElement('div') as HTMLDivElement;
                taskName.className = 'task-name';
                taskName.textContent = task.name;
                row.appendChild(taskName);

                const running_width = ((task.endDateTime - task.startDateTime) / (tasks[tasks.length - 1].endDateTime - tasks[0].startDateTime)) * 100
                const submitted_width = ((task.startDateTime - task.submitDateTime) / (tasks[tasks.length - 1].endDateTime - tasks[0].startDateTime)) * 100 // 修改为百分比


                const bar: HTMLDivElement = document.createElement('div') as HTMLDivElement;
                bar.className = 'bar';
                const bar_margin_left = ((task.startDateTime - tasks[0].startDateTime) / (tasks[tasks.length - 1].endDateTime - tasks[0].startDateTime)) * (100 - 200 / window.innerWidth * 100);

                bar.style.marginLeft = Math.max(bar_margin_left - (running_width === 0 ? 10 : 0), 0) + '%';

                const submitted: HTMLDivElement = document.createElement('div') as HTMLDivElement;
                submitted.className = 'submitted';
                submitted.style.width = submitted_width + '%';
                bar.appendChild(submitted);

                const running: HTMLDivElement = document.createElement('div') as HTMLDivElement;
                running.style.width = (running_width === 0 ? 0.5 : running_width) + '%'; // 修改为百分比

                if (task.status === 'SUCCESS') {
                    running.className = 'success';
                } else if (task.status === 'RUNNING_EXECUTION') {
                    running.className = 'running';
                } else if (task.status === 'FAILURE') {
                    running.className = 'failure';
                } else if (task.status === 'KILL') {
                    running.className = 'kill';
                }

                submitted.onmouseover = function (e) {
                    tipShow(e, task)
                };
                running.onmouseover = function (e) {
                    tipShow(e, task)
                };

                gantt.onmouseover = function (e) {
                    hideTip(e)
                };
                bar.appendChild(running);
                row.appendChild(bar);
                gantt.appendChild(row);
            }
            const tipShow = (e: MouseEvent, task: ITaskGantt) => {
                tip.innerHTML = '任务名称: ' + task.name + '<br>' +
                    '提交时间: ' + task.submitTime + '<br>' +
                    '等待时长: ' + (new Date(task.startTime).getTime() - new Date(task.submitTime).getTime()) / 1000 + ' 秒<br>' +
                    '开始时间: ' + task.startTime + '<br>' +
                    '结束时间: ' + task.endTime + '<br>' +
                    '执行时长: ' + task.duration;
                tip.style.display = 'block';
                tip.style.left = (e.pageX - (e.pageX < 300 ? 0 : 300)) + 'px';
                tip.style.top = e.pageY + 'px';
            }
            const hideTip = (e: MouseEvent) => {
                const rect = tip.getBoundingClientRect();
                if (e.clientX >= rect.left && e.clientX <= rect.right && e.clientY >= rect.top && e.clientY <= rect.bottom) {
                    return;
                }
                tip.style.display = 'none';
            }

        }

        onMounted(() => {
            getTaskList()
            refreshGantt()
        })


        return {t, ...toRefs(variables)}
    },
    render() {
        const {t} = this
        return (
            <Card title={t('project.workflow.gantt')}>
                <div class="exp">
                    <span>运行中</span><div class="running"></div>
                    <span>已提交</span><div class="submitted"></div>
                    <span>成功</span><div class="success"></div>
                    <span>失败</span><div class="failure"></div>
                    <span>Kill</span><div class="kill"></div>
                </div>
                <div id="gantt"></div>
            </Card>
        )
    }
})

export default workflowRelation
