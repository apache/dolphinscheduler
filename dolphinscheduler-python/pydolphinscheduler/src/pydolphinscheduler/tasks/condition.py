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

"""Task Conditions."""

from typing import Dict, List

from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.core.task import Task
from pydolphinscheduler.exceptions import PyDSParamException
from pydolphinscheduler.models.base import Base


class Status(Base):
    """Base class of Condition task status.

    It a parent class for :class:`SUCCESS` and :class:`FAILURE`. Provider status name
    and :func:`get_define` to sub class.
    """

    def __init__(self, *tasks):
        super().__init__(f"Condition.{self.status_name()}")
        self.tasks = tasks

    def __repr__(self) -> str:
        return "depend_item_list"

    @classmethod
    def status_name(cls) -> str:
        """Get name for Status or its sub class."""
        return cls.__name__.upper()

    def get_define(self, camel_attr: bool = True) -> List:
        """Get status definition attribute communicate to Java gateway server."""
        content = []
        for task in self.tasks:
            if not isinstance(task, Task):
                raise PyDSParamException(
                    "%s only accept class Task or sub class Task, but get %s",
                    (self.status_name(), type(task)),
                )
            content.append({"depTaskCode": task.code, "status": self.status_name()})
        return content


class SUCCESS(Status):
    """Class SUCCESS to task condition, sub class of :class:`Status`."""

    def __init__(self, *tasks):
        super().__init__(*tasks)


class FAILURE(Status):
    """Class FAILURE to task condition, sub class of :class:`Status`."""

    def __init__(self, *tasks):
        super().__init__(*tasks)


class ConditionOperator(Base):
    """Set ConditionTask or ConditionOperator with specific operator."""

    _DEFINE_ATTR = {
        "relation",
    }

    def __init__(self, *args):
        super().__init__(self.__class__.__name__)
        self.args = args

    def __repr__(self) -> str:
        return "depend_task_list"

    @classmethod
    def operator_name(cls) -> str:
        """Get operator name in different class."""
        return cls.__name__.upper()

    @property
    def relation(self) -> str:
        """Get operator name in different class, for function :func:`get_define`."""
        return self.operator_name()

    def set_define_attr(self) -> str:
        """Set attribute to function :func:`get_define`.

        It is a wrapper for both `And` and `Or` operator.
        """
        result = []
        attr = None
        for condition in self.args:
            if isinstance(condition, (Status, ConditionOperator)):
                if attr is None:
                    attr = repr(condition)
                elif repr(condition) != attr:
                    raise PyDSParamException(
                        "Condition %s operator parameter only support same type.",
                        self.relation,
                    )
            else:
                raise PyDSParamException(
                    "Condition %s operator parameter support ConditionTask and ConditionOperator but got %s.",
                    (self.relation, type(condition)),
                )
            if attr == "depend_item_list":
                result.extend(condition.get_define())
            else:
                result.append(condition.get_define())
        setattr(self, attr, result)
        return attr

    def get_define(self, camel_attr=True) -> Dict:
        """Overwrite Base.get_define to get task Condition specific get define."""
        attr = self.set_define_attr()
        dependent_define_attr = self._DEFINE_ATTR.union({attr})
        return super().get_define_custom(
            camel_attr=True, custom_attr=dependent_define_attr
        )


class And(ConditionOperator):
    """Operator And for task condition.

    It could accept both :class:`Task` and children of :class:`ConditionOperator`,
    and set AND condition to those args.
    """

    def __init__(self, *args):
        super().__init__(*args)


class Or(ConditionOperator):
    """Operator Or for task condition.

    It could accept both :class:`Task` and children of :class:`ConditionOperator`,
    and set OR condition to those args.
    """

    def __init__(self, *args):
        super().__init__(*args)


class Condition(Task):
    """Task condition object, declare behavior for condition task to dolphinscheduler."""

    def __init__(
        self,
        name: str,
        condition: ConditionOperator,
        success_task: Task,
        failed_task: Task,
        *args,
        **kwargs,
    ):
        super().__init__(name, TaskType.CONDITIONS, *args, **kwargs)
        self.condition = condition
        self.success_task = success_task
        self.failed_task = failed_task
        # Set condition tasks as current task downstream
        self._set_dep()

    def _set_dep(self) -> None:
        """Set upstream according to parameter `condition`."""
        upstream = []
        for cond in self.condition.args:
            if isinstance(cond, ConditionOperator):
                for status in cond.args:
                    upstream.extend(list(status.tasks))
        self.set_upstream(upstream)
        self.set_downstream([self.success_task, self.failed_task])

    @property
    def condition_result(self) -> Dict:
        """Get condition result define for java gateway."""
        return {
            "successNode": [self.success_task.code],
            "failedNode": [self.failed_task.code],
        }

    @property
    def task_params(self, camel_attr: bool = True, custom_attr: set = None) -> Dict:
        """Override Task.task_params for Condition task.

        Condition task have some specials attribute `dependence`, and in most of the task
        this attribute is None and use empty dict `{}` as default value. We do not use class
        attribute `_task_custom_attr` due to avoid attribute cover.
        """
        params = super().task_params
        params["dependence"] = self.condition.get_define()
        return params
