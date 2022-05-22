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

"""Task dependent."""

from typing import Dict, Optional, Tuple

from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.core.base import Base
from pydolphinscheduler.core.task import Task
from pydolphinscheduler.exceptions import PyDSJavaGatewayException, PyDSParamException
from pydolphinscheduler.java_gateway import launch_gateway

DEPENDENT_ALL_TASK_IN_WORKFLOW = "0"


class DependentDate(str):
    """Constant of Dependent date value.

    These values set according to Java server side, if you want to add and change it,
    please change Java server side first.
    """

    # TODO Maybe we should add parent level to DependentDate for easy to use, such as
    # DependentDate.MONTH.THIS_MONTH

    # Hour
    CURRENT_HOUR = "currentHour"
    LAST_ONE_HOUR = "last1Hour"
    LAST_TWO_HOURS = "last2Hours"
    LAST_THREE_HOURS = "last3Hours"
    LAST_TWENTY_FOUR_HOURS = "last24Hours"

    # Day
    TODAY = "today"
    LAST_ONE_DAYS = "last1Days"
    LAST_TWO_DAYS = "last2Days"
    LAST_THREE_DAYS = "last3Days"
    LAST_SEVEN_DAYS = "last7Days"

    # Week
    THIS_WEEK = "thisWeek"
    LAST_WEEK = "lastWeek"
    LAST_MONDAY = "lastMonday"
    LAST_TUESDAY = "lastTuesday"
    LAST_WEDNESDAY = "lastWednesday"
    LAST_THURSDAY = "lastThursday"
    LAST_FRIDAY = "lastFriday"
    LAST_SATURDAY = "lastSaturday"
    LAST_SUNDAY = "lastSunday"

    # Month
    THIS_MONTH = "thisMonth"
    LAST_MONTH = "lastMonth"
    LAST_MONTH_BEGIN = "lastMonthBegin"
    LAST_MONTH_END = "lastMonthEnd"


class DependentItem(Base):
    """Dependent item object, minimal unit for task dependent.

    It declare which project, process_definition, task are dependent to this task.
    """

    _DEFINE_ATTR = {
        "project_code",
        "definition_code",
        "dep_task_code",
        "cycle",
        "date_value",
    }

    # TODO maybe we should conside overwrite operator `and` and `or` for DependentItem to
    #  support more easy way to set relation
    def __init__(
        self,
        project_name: str,
        process_definition_name: str,
        dependent_task_name: Optional[str] = DEPENDENT_ALL_TASK_IN_WORKFLOW,
        dependent_date: Optional[DependentDate] = DependentDate.TODAY,
    ):
        obj_name = f"{project_name}.{process_definition_name}.{dependent_task_name}.{dependent_date}"
        super().__init__(obj_name)
        self.project_name = project_name
        self.process_definition_name = process_definition_name
        self.dependent_task_name = dependent_task_name
        if dependent_date is None:
            raise PyDSParamException(
                "Parameter dependent_date must provider by got None."
            )
        else:
            self.dependent_date = dependent_date
        self._code = {}

    def __repr__(self) -> str:
        return "depend_item_list"

    @property
    def project_code(self) -> str:
        """Get dependent project code."""
        return self.get_code_from_gateway().get("projectCode")

    @property
    def definition_code(self) -> str:
        """Get dependent definition code."""
        return self.get_code_from_gateway().get("processDefinitionCode")

    @property
    def dep_task_code(self) -> str:
        """Get dependent tasks code list."""
        if self.is_all_task:
            return DEPENDENT_ALL_TASK_IN_WORKFLOW
        else:
            return self.get_code_from_gateway().get("taskDefinitionCode")

    # TODO Maybe we should get cycle from dependent date class.
    @property
    def cycle(self) -> str:
        """Get dependent cycle."""
        if "Hour" in self.dependent_date:
            return "hour"
        elif self.dependent_date == "today" or "Days" in self.dependent_date:
            return "day"
        elif "Month" in self.dependent_date:
            return "month"
        else:
            return "week"

    @property
    def date_value(self) -> str:
        """Get dependent date."""
        return self.dependent_date

    @property
    def is_all_task(self) -> bool:
        """Check whether dependent all tasks or not."""
        return self.dependent_task_name == DEPENDENT_ALL_TASK_IN_WORKFLOW

    @property
    def code_parameter(self) -> Tuple:
        """Get name info parameter to query code."""
        param = (
            self.project_name,
            self.process_definition_name,
            self.dependent_task_name if not self.is_all_task else None,
        )
        return param

    def get_code_from_gateway(self) -> Dict:
        """Get project, definition, task code from given parameter."""
        if self._code:
            return self._code
        else:
            gateway = launch_gateway()
            try:
                self._code = gateway.entry_point.getDependentInfo(*self.code_parameter)
                return self._code
            except Exception:
                raise PyDSJavaGatewayException("Function get_code_from_gateway error.")


class DependentOperator(Base):
    """Set DependentItem or dependItemList with specific operator."""

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
        for dependent in self.args:
            if isinstance(dependent, (DependentItem, DependentOperator)):
                if attr is None:
                    attr = repr(dependent)
                elif repr(dependent) != attr:
                    raise PyDSParamException(
                        "Dependent %s operator parameter only support same type.",
                        self.relation,
                    )
            else:
                raise PyDSParamException(
                    "Dependent %s operator parameter support DependentItem and "
                    "DependentOperator but got %s.",
                    (self.relation, type(dependent)),
                )
            result.append(dependent.get_define())
        setattr(self, attr, result)
        return attr

    def get_define(self, camel_attr=True) -> Dict:
        """Overwrite Base.get_define to get task dependent specific get define."""
        attr = self.set_define_attr()
        dependent_define_attr = self._DEFINE_ATTR.union({attr})
        return super().get_define_custom(
            camel_attr=True, custom_attr=dependent_define_attr
        )


class And(DependentOperator):
    """Operator And for task dependent.

    It could accept both :class:`DependentItem` and children of :class:`DependentOperator`,
    and set AND condition to those args.
    """

    def __init__(self, *args):
        super().__init__(*args)


class Or(DependentOperator):
    """Operator Or for task dependent.

    It could accept both :class:`DependentItem` and children of :class:`DependentOperator`,
    and set OR condition to those args.
    """

    def __init__(self, *args):
        super().__init__(*args)


class Dependent(Task):
    """Task dependent object, declare behavior for dependent task to dolphinscheduler."""

    def __init__(self, name: str, dependence: DependentOperator, *args, **kwargs):
        super().__init__(name, TaskType.DEPENDENT, *args, **kwargs)
        self.dependence = dependence

    @property
    def task_params(self, camel_attr: bool = True, custom_attr: set = None) -> Dict:
        """Override Task.task_params for dependent task.

        Dependent task have some specials attribute `dependence`, and in most of the task
        this attribute is None and use empty dict `{}` as default value. We do not use class
        attribute `_task_custom_attr` due to avoid attribute cover.
        """
        params = super().task_params
        params["dependence"] = self.dependence.get_define()
        return params
