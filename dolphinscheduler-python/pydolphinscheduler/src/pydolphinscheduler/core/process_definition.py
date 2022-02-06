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

"""Module process definition, core class for workflow define."""

import json
from datetime import datetime
from typing import Any, Dict, List, Optional, Set

from pydolphinscheduler.constants import (
    ProcessDefinitionDefault,
    ProcessDefinitionReleaseState,
    TaskType,
)
from pydolphinscheduler.core.base import Base
from pydolphinscheduler.exceptions import PyDSParamException, PyDSTaskNoFoundException
from pydolphinscheduler.java_gateway import launch_gateway
from pydolphinscheduler.side import Project, Tenant, User
from pydolphinscheduler.utils.date import MAX_DATETIME, conv_from_str, conv_to_schedule


class ProcessDefinitionContext:
    """Class process definition context, use when task get process definition from context expression."""

    _context_managed_process_definition: Optional["ProcessDefinition"] = None

    @classmethod
    def set(cls, pd: "ProcessDefinition") -> None:
        """Set attribute self._context_managed_process_definition."""
        cls._context_managed_process_definition = pd

    @classmethod
    def get(cls) -> Optional["ProcessDefinition"]:
        """Get attribute self._context_managed_process_definition."""
        return cls._context_managed_process_definition

    @classmethod
    def delete(cls) -> None:
        """Delete attribute self._context_managed_process_definition."""
        cls._context_managed_process_definition = None


class ProcessDefinition(Base):
    """process definition object, will define process definition attribute, task, relation.

    TODO: maybe we should rename this class, currently use DS object name.
    """

    # key attribute for identify ProcessDefinition object
    _KEY_ATTR = {
        "name",
        "project",
        "tenant",
        "release_state",
        "param",
    }

    _DEFINE_ATTR = {
        "name",
        "description",
        "_project",
        "_tenant",
        "worker_group",
        "timeout",
        "release_state",
        "param",
        "tasks",
        "task_definition_json",
        "task_relation_json",
    }

    def __init__(
        self,
        name: str,
        description: Optional[str] = None,
        schedule: Optional[str] = None,
        start_time: Optional[str] = None,
        end_time: Optional[str] = None,
        timezone: Optional[str] = ProcessDefinitionDefault.TIME_ZONE,
        user: Optional[str] = ProcessDefinitionDefault.USER,
        project: Optional[str] = ProcessDefinitionDefault.PROJECT,
        tenant: Optional[str] = ProcessDefinitionDefault.TENANT,
        queue: Optional[str] = ProcessDefinitionDefault.QUEUE,
        worker_group: Optional[str] = ProcessDefinitionDefault.WORKER_GROUP,
        timeout: Optional[int] = 0,
        release_state: Optional[str] = ProcessDefinitionReleaseState.ONLINE,
        param: Optional[Dict] = None,
    ):
        super().__init__(name, description)
        self.schedule = schedule
        self._start_time = start_time
        self._end_time = end_time
        self.timezone = timezone
        self._user = user
        self._project = project
        self._tenant = tenant
        self._queue = queue
        self.worker_group = worker_group
        self.timeout = timeout
        self.release_state = release_state
        self.param = param
        self.tasks: dict = {}
        # TODO how to fix circle import
        self._task_relations: set["TaskRelation"] = set()  # noqa: F821
        self._process_definition_code = None

    def __enter__(self) -> "ProcessDefinition":
        ProcessDefinitionContext.set(self)
        return self

    def __exit__(self, exc_type, exc_val, exc_tb) -> None:
        ProcessDefinitionContext.delete()

    @property
    def tenant(self) -> Tenant:
        """Get attribute tenant."""
        return Tenant(self._tenant)

    @tenant.setter
    def tenant(self, tenant: Tenant) -> None:
        """Set attribute tenant."""
        self._tenant = tenant.name

    @property
    def project(self) -> Project:
        """Get attribute project."""
        return Project(self._project)

    @project.setter
    def project(self, project: Project) -> None:
        """Set attribute project."""
        self._project = project.name

    @property
    def user(self) -> User:
        """Get user object.

        For now we just get from python side but not from java gateway side, so it may not correct.
        """
        return User(
            self._user,
            ProcessDefinitionDefault.USER_PWD,
            ProcessDefinitionDefault.USER_EMAIL,
            ProcessDefinitionDefault.USER_PHONE,
            self._tenant,
            self._queue,
            ProcessDefinitionDefault.USER_STATE,
        )

    @staticmethod
    def _parse_datetime(val: Any) -> Any:
        if val is None or isinstance(val, datetime):
            return val
        elif isinstance(val, str):
            return conv_from_str(val)
        else:
            raise PyDSParamException("Do not support value type %s for now", type(val))

    @property
    def start_time(self) -> Any:
        """Get attribute start_time."""
        return self._parse_datetime(self._start_time)

    @start_time.setter
    def start_time(self, val) -> None:
        """Set attribute start_time."""
        self._start_time = val

    @property
    def end_time(self) -> Any:
        """Get attribute end_time."""
        return self._parse_datetime(self._end_time)

    @end_time.setter
    def end_time(self, val) -> None:
        """Set attribute end_time."""
        self._end_time = val

    @property
    def param_json(self) -> Optional[List[Dict]]:
        """Return param json base on self.param."""
        # Handle empty dict and None value
        if not self.param:
            return []
        return [
            {
                "prop": k,
                "direct": "IN",
                "type": "VARCHAR",
                "value": v,
            }
            for k, v in self.param.items()
        ]

    @property
    def task_definition_json(self) -> List[Dict]:
        """Return all tasks definition in list of dict."""
        if not self.tasks:
            return [self.tasks]
        else:
            return [task.get_define() for task in self.tasks.values()]

    @property
    def task_relation_json(self) -> List[Dict]:
        """Return all relation between tasks pair in list of dict."""
        if not self.tasks:
            return [self.tasks]
        else:
            self._handle_root_relation()
            return [tr.get_define() for tr in self._task_relations]

    @property
    def schedule_json(self) -> Optional[Dict]:
        """Get schedule parameter json object. This is requests from java gateway interface."""
        if not self.schedule:
            return None
        else:
            start_time = conv_to_schedule(
                self.start_time if self.start_time else datetime.now()
            )
            end_time = conv_to_schedule(
                self.end_time if self.end_time else MAX_DATETIME
            )
            return {
                "startTime": start_time,
                "endTime": end_time,
                "crontab": self.schedule,
                "timezoneId": self.timezone,
            }

    # TODO inti DAG's tasks are in the same location with default {x: 0, y: 0}
    @property
    def task_location(self) -> List[Dict]:
        """Return all tasks location for all process definition.

        For now, we only set all location with same x and y valued equal to 0. Because we do not
        find a good way to set task locations. This is requests from java gateway interface.
        """
        if not self.tasks:
            return [self.tasks]
        else:
            return [{"taskCode": task_code, "x": 0, "y": 0} for task_code in self.tasks]

    @property
    def task_list(self) -> List["Task"]:  # noqa: F821
        """Return list of tasks objects."""
        return list(self.tasks.values())

    def _handle_root_relation(self):
        """Handle root task property :class:`pydolphinscheduler.core.task.TaskRelation`.

        Root task in DAG do not have dominant upstream node, but we have to add an exactly default
        upstream task with task_code equal to `0`. This is requests from java gateway interface.
        """
        from pydolphinscheduler.core.task import TaskRelation

        post_relation_code = set()
        for relation in self._task_relations:
            post_relation_code.add(relation.post_task_code)
        for task in self.task_list:
            if task.code not in post_relation_code:
                root_relation = TaskRelation(pre_task_code=0, post_task_code=task.code)
                self._task_relations.add(root_relation)

    def add_task(self, task: "Task") -> None:  # noqa: F821
        """Add a single task to process definition."""
        self.tasks[task.code] = task
        task._process_definition = self

    def add_tasks(self, tasks: List["Task"]) -> None:  # noqa: F821
        """Add task sequence to process definition, it a wrapper of :func:`add_task`."""
        for task in tasks:
            self.add_task(task)

    def get_task(self, code: str) -> "Task":  # noqa: F821
        """Get task object from process definition by given code."""
        if code not in self.tasks:
            raise PyDSTaskNoFoundException(
                "Task with code %s can not found in process definition %",
                (code, self.name),
            )
        return self.tasks[code]

    # TODO which tying should return in this case
    def get_tasks_by_name(self, name: str) -> Set["Task"]:  # noqa: F821
        """Get tasks object by given name, if will return all tasks with this name."""
        find = set()
        for task in self.tasks.values():
            if task.name == name:
                find.add(task)
        return find

    def get_one_task_by_name(self, name: str) -> "Task":  # noqa: F821
        """Get exact one task from process definition by given name.

        Function always return one task even though this process definition have more than one task with
        this name.
        """
        tasks = self.get_tasks_by_name(name)
        if not tasks:
            raise PyDSTaskNoFoundException(f"Can not find task with name {name}.")
        return tasks.pop()

    def run(self):
        """Submit and Start ProcessDefinition instance.

        Shortcut for function :func:`submit` and function :func:`start`. Only support manual start workflow
        for now, and schedule run will coming soon.
        :return:
        """
        self.submit()
        self.start()

    def _ensure_side_model_exists(self):
        """Ensure process definition side model exists.

        For now, side object including :class:`pydolphinscheduler.side.project.Project`,
        :class:`pydolphinscheduler.side.tenant.Tenant`, :class:`pydolphinscheduler.side.user.User`.
        If these model not exists, would create default value in
        :class:`pydolphinscheduler.constants.ProcessDefinitionDefault`.
        """
        # TODO used metaclass for more pythonic
        self.tenant.create_if_not_exists(self._queue)
        # model User have to create after Tenant created
        self.user.create_if_not_exists()
        # Project model need User object exists
        self.project.create_if_not_exists(self._user)

    def _pre_submit_check(self):
        """Check specific condition satisfy before.

        This method should be called before process definition submit to java gateway
        For now, we have below checker:
        * `self.param` should be set if task `switch` in this workflow.
        """
        if (
            any([task.task_type == TaskType.SWITCH for task in self.tasks.values()])
            and self.param is None
        ):
            raise PyDSParamException(
                "Parameter param must be provider if task Switch in process definition."
            )

    def submit(self) -> int:
        """Submit ProcessDefinition instance to java gateway."""
        self._ensure_side_model_exists()
        self._pre_submit_check()

        gateway = launch_gateway()
        self._process_definition_code = gateway.entry_point.createOrUpdateProcessDefinition(
            self._user,
            self._project,
            self.name,
            str(self.description) if self.description else "",
            json.dumps(self.param_json),
            json.dumps(self.schedule_json) if self.schedule_json else None,
            json.dumps(self.task_location),
            self.timeout,
            self.worker_group,
            self._tenant,
            # TODO add serialization function
            json.dumps(self.task_relation_json),
            json.dumps(self.task_definition_json),
            None,
        )
        return self._process_definition_code

    def start(self) -> None:
        """Create and start ProcessDefinition instance.

        which post to `start-process-instance` to java gateway
        """
        gateway = launch_gateway()
        gateway.entry_point.execProcessInstance(
            self._user,
            self._project,
            self.name,
            "",
            self.worker_group,
            24 * 3600,
        )
