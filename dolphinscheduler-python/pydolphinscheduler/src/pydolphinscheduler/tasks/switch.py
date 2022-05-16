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

"""Task Switch."""

from typing import Dict, Optional

from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.core.base import Base
from pydolphinscheduler.core.task import Task
from pydolphinscheduler.exceptions import PyDSParamException


class SwitchBranch(Base):
    """Base class of ConditionBranch of task switch.

    It a parent class for :class:`Branch` and :class:`Default`.
    """

    _DEFINE_ATTR = {
        "next_node",
    }

    def __init__(self, task: Task, exp: Optional[str] = None):
        super().__init__(f"Switch.{self.__class__.__name__.upper()}")
        self.task = task
        self.exp = exp

    @property
    def next_node(self) -> str:
        """Get task switch property next_node, it return task code when init class switch."""
        return self.task.code

    @property
    def condition(self) -> Optional[str]:
        """Get task switch property condition."""
        return self.exp

    def get_define(self, camel_attr: bool = True) -> Dict:
        """Get :class:`ConditionBranch` definition attribute communicate to Java gateway server."""
        if self.condition:
            self._DEFINE_ATTR.add("condition")
        return super().get_define()


class Branch(SwitchBranch):
    """Common condition branch for switch task.

    If any condition in :class:`Branch` match, would set this :class:`Branch`'s task as downstream of task
    switch. If all condition branch do not match would set :class:`Default`'s task as task switch downstream.
    """

    def __init__(self, condition: str, task: Task):
        super().__init__(task, condition)


class Default(SwitchBranch):
    """Class default branch for switch task.

    If all condition of :class:`Branch` do not match, task switch would run the tasks in :class:`Default`
    and set :class:`Default`'s task as switch downstream. Please notice that each switch condition
    could only have one single :class:`Default`.
    """

    def __init__(self, task: Task):
        super().__init__(task)


class SwitchCondition(Base):
    """Set switch condition of given parameter."""

    _DEFINE_ATTR = {
        "depend_task_list",
    }

    def __init__(self, *args):
        super().__init__(self.__class__.__name__)
        self.args = args

    def set_define_attr(self) -> None:
        """Set attribute to function :func:`get_define`.

        It is a wrapper for both `And` and `Or` operator.
        """
        result = []
        num_branch_default = 0
        for condition in self.args:
            if isinstance(condition, SwitchBranch):
                if num_branch_default < 1:
                    if isinstance(condition, Default):
                        self._DEFINE_ATTR.add("next_node")
                        setattr(self, "next_node", condition.next_node)
                        num_branch_default += 1
                    elif isinstance(condition, Branch):
                        result.append(condition.get_define())
                else:
                    raise PyDSParamException(
                        "Task Switch's parameter only support exactly one default branch."
                    )
            else:
                raise PyDSParamException(
                    "Task Switch's parameter only support SwitchBranch but got %s.",
                    type(condition),
                )
        # Handle switch default branch, default value is `""` if not provide.
        if num_branch_default == 0:
            self._DEFINE_ATTR.add("next_node")
            setattr(self, "next_node", "")
        setattr(self, "depend_task_list", result)

    def get_define(self, camel_attr=True) -> Dict:
        """Overwrite Base.get_define to get task Condition specific get define."""
        self.set_define_attr()
        return super().get_define()


class Switch(Task):
    """Task switch object, declare behavior for switch task to dolphinscheduler.

    Param of process definition or at least one local param of task must be set
    if task `switch` in this workflow.
    """

    def __init__(self, name: str, condition: SwitchCondition, *args, **kwargs):
        super().__init__(name, TaskType.SWITCH, *args, **kwargs)
        self.condition = condition
        # Set condition tasks as current task downstream
        self._set_dep()

    def _set_dep(self) -> None:
        """Set downstream according to parameter `condition`."""
        downstream = []
        for condition in self.condition.args:
            if isinstance(condition, SwitchBranch):
                downstream.append(condition.task)
        self.set_downstream(downstream)

    @property
    def task_params(self, camel_attr: bool = True, custom_attr: set = None) -> Dict:
        """Override Task.task_params for switch task.

        switch task have some specials attribute `switch`, and in most of the task
        this attribute is None and use empty dict `{}` as default value. We do not use class
        attribute `_task_custom_attr` due to avoid attribute cover.
        """
        params = super().task_params
        params["switchResult"] = self.condition.get_define()
        return params
