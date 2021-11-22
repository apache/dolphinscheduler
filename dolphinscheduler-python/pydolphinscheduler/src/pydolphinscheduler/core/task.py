# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

"""DolphinScheduler ObjectJsonBase, TaskParams and Task object."""

from typing import Dict, List, Optional, Sequence, Set, Tuple, Union

from pydolphinscheduler.constants import (
    ProcessDefinitionDefault,
    TaskFlag,
    TaskPriority,
    TaskTimeoutFlag,
)
from pydolphinscheduler.core.base import Base
from pydolphinscheduler.core.process_definition import (
    ProcessDefinition,
    ProcessDefinitionContext,
)
from pydolphinscheduler.java_gateway import launch_gateway
from pydolphinscheduler.utils.string import class_name2camel, snake2camel


class ObjectJsonBase:
    """Task base class, define `__str__` and `to_dict` function would be use in other task related class."""

    DEFAULT_ATTR = {}

    def __int__(self, *args, **kwargs):
        pass

    def __str__(self) -> str:
        content = []
        for attribute, value in self.__dict__.items():
            content.append(f'"{snake2camel(attribute)}": {value}')
        content = ",".join(content)
        return f'"{class_name2camel(type(self).__name__)}":{{{content}}}'

    # TODO check how Redash do
    # TODO DRY
    def to_dict(self) -> Dict:
        """Get object key attribute dict which determine by attribute `DEFAULT_ATTR`."""
        content = {snake2camel(attr): value for attr, value in self.__dict__.items()}
        content.update(self.DEFAULT_ATTR)
        return content


class TaskParams(ObjectJsonBase):
    """TaskParams object, describe the key parameter of a single task."""

    DEFAULT_CONDITION_RESULT = {"successNode": [""], "failedNode": [""]}

    def __init__(
        self,
        local_params: Optional[List] = None,
        resource_list: Optional[List] = None,
        dependence: Optional[Dict] = None,
        wait_start_timeout: Optional[Dict] = None,
        condition_result: Optional[Dict] = None,
        *args,
        **kwargs,
    ):
        super().__init__(*args, **kwargs)
        self.local_params = local_params or []
        self.resource_list = resource_list or []
        self.dependence = dependence or {}
        self.wait_start_timeout = wait_start_timeout or {}
        # TODO need better way to handle it, this code just for POC
        self.condition_result = condition_result or self.DEFAULT_CONDITION_RESULT


class TaskRelation(ObjectJsonBase):
    """TaskRelation object, describe the relation of exactly two tasks."""

    DEFAULT_ATTR = {
        "name": "",
        "preTaskVersion": 1,
        "postTaskVersion": 1,
        "conditionType": 0,
        "conditionParams": {},
    }

    def __init__(
        self,
        pre_task_code: int,
        post_task_code: int,
    ):
        super().__init__()
        self.pre_task_code = pre_task_code
        self.post_task_code = post_task_code

    def __hash__(self):
        return hash(f"{self.post_task_code}, {self.post_task_code}")


class Task(Base):
    """Task object, parent class for all exactly task type."""

    DEFAULT_DEPS_ATTR = {
        "name": "",
        "preTaskVersion": 1,
        "postTaskVersion": 1,
        "conditionType": 0,
        "conditionParams": {},
    }

    def __init__(
        self,
        name: str,
        task_type: str,
        task_params: TaskParams,
        description: Optional[str] = None,
        flag: Optional[str] = TaskFlag.YES,
        task_priority: Optional[str] = TaskPriority.MEDIUM,
        worker_group: Optional[str] = ProcessDefinitionDefault.WORKER_GROUP,
        delay_time: Optional[int] = 0,
        fail_retry_times: Optional[int] = 0,
        fail_retry_interval: Optional[int] = 1,
        timeout_flag: Optional[int] = TaskTimeoutFlag.CLOSE,
        timeout_notify_strategy: Optional = None,
        timeout: Optional[int] = 0,
        process_definition: Optional[ProcessDefinition] = None,
    ):

        super().__init__(name, description)
        self.task_type = task_type
        self.task_params = task_params
        self.flag = flag
        self.task_priority = task_priority
        self.worker_group = worker_group
        self.fail_retry_times = fail_retry_times
        self.fail_retry_interval = fail_retry_interval
        self.delay_time = delay_time
        self.timeout_flag = timeout_flag
        self.timeout_notify_strategy = timeout_notify_strategy
        self.timeout = timeout
        self._process_definition = None
        self.process_definition: ProcessDefinition = (
            process_definition or ProcessDefinitionContext.get()
        )
        self._upstream_task_codes: Set[int] = set()
        self._downstream_task_codes: Set[int] = set()
        self._task_relation: Set[TaskRelation] = set()
        # move attribute code and version after _process_definition and process_definition declare
        self.code, self.version = self.gen_code_and_version()
        # Add task to process definition, maybe we could put into property process_definition latter
        if (
            self.process_definition is not None
            and self.code not in self.process_definition.tasks
        ):
            self.process_definition.add_task(self)

    @property
    def process_definition(self) -> Optional[ProcessDefinition]:
        """Get attribute process_definition."""
        return self._process_definition

    @process_definition.setter
    def process_definition(self, process_definition: Optional[ProcessDefinition]):
        """Set attribute process_definition."""
        self._process_definition = process_definition

    def __hash__(self):
        return hash(self.code)

    def __lshift__(self, other: Union["Task", Sequence["Task"]]):
        """Implement Task << Task."""
        self.set_upstream(other)
        return other

    def __rshift__(self, other: Union["Task", Sequence["Task"]]):
        """Implement Task >> Task."""
        self.set_downstream(other)
        return other

    def __rrshift__(self, other: Union["Task", Sequence["Task"]]):
        """Call for Task >> [Task] because list don't have __rshift__ operators."""
        self.__lshift__(other)
        return self

    def __rlshift__(self, other: Union["Task", Sequence["Task"]]):
        """Call for Task << [Task] because list don't have __lshift__ operators."""
        self.__rshift__(other)
        return self

    def _set_deps(
        self, tasks: Union["Task", Sequence["Task"]], upstream: bool = True
    ) -> None:
        """
        Set parameter tasks dependent to current task.

        it is a wrapper for :func:`set_upstream` and :func:`set_downstream`.
        """
        if not isinstance(tasks, Sequence):
            tasks = [tasks]

        for task in tasks:
            if upstream:
                self._upstream_task_codes.add(task.code)
                task._downstream_task_codes.add(self.code)

                if self._process_definition:
                    task_relation = TaskRelation(
                        pre_task_code=task.code,
                        post_task_code=self.code,
                    )
                    self.process_definition._task_relations.add(task_relation)
            else:
                self._downstream_task_codes.add(task.code)
                task._upstream_task_codes.add(self.code)

                if self._process_definition:
                    task_relation = TaskRelation(
                        pre_task_code=self.code,
                        post_task_code=task.code,
                    )
                    self.process_definition._task_relations.add(task_relation)

    def set_upstream(self, tasks: Union["Task", Sequence["Task"]]) -> None:
        """Set parameter tasks as upstream to current task."""
        self._set_deps(tasks, upstream=True)

    def set_downstream(self, tasks: Union["Task", Sequence["Task"]]) -> None:
        """Set parameter tasks as downstream to current task."""
        self._set_deps(tasks, upstream=False)

    # TODO code should better generate in bulk mode when :ref: processDefinition run submit or start
    def gen_code_and_version(self) -> Tuple:
        """
        Generate task code and version from java gateway.

        If task name do not exists in process definition before, if will generate new code and version id
        equal to 0 by java gateway, otherwise if will return the exists code and version.
        """
        # TODO get code from specific project process definition and task name
        gateway = launch_gateway()
        result = gateway.entry_point.getCodeAndVersion(
            self.process_definition._project, self.name
        )
        # result = gateway.entry_point.genTaskCodeList(DefaultTaskCodeNum.DEFAULT)
        # gateway_result_checker(result)
        return result.get("code"), result.get("version")

    def to_dict(self, camel_attr=True) -> Dict:
        """Task `to_dict` function which will return key attribute for Task object."""
        content = {}
        for attr, value in self.__dict__.items():
            # Don't publish private variables
            if attr.startswith("_"):
                continue
            elif isinstance(value, TaskParams):
                content[snake2camel(attr)] = value.to_dict()
            else:
                content[snake2camel(attr)] = value
        return content
