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

"""Parse YAML file to create process."""

import logging
import os
import re
from pathlib import Path
from typing import Any, Dict

from pydolphinscheduler import configuration, tasks
from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.core.task import Task
from pydolphinscheduler.exceptions import PyDSTaskNoFoundException
from pydolphinscheduler.utils.yaml_parser import YamlParser

logger = logging.getLogger(__file__)

KEY_PROCESS = "workflow"
KEY_TASK = "tasks"
KEY_TASK_TYPE = "task_type"
KEY_DEPS = "deps"
KEY_OP = "op"

TASK_SPECIAL_KEYS = [KEY_TASK_TYPE, KEY_DEPS]


class ParseTool:
    """Enhanced parsing tools."""

    @staticmethod
    def parse_string_param_if_file(string_param: str, **kwargs):
        """Use $FILE{"data_path"} to load file from "data_path"."""
        if string_param.startswith("$FILE"):
            path = re.findall(r"\$FILE\{\"(.*?)\"\}", string_param)[0]
            base_folder = kwargs.get("base_folder", ".")
            path = ParseTool.get_possible_path(path, base_folder)
            with open(path, "r") as read_file:
                string_param = "".join(read_file)
        return string_param

    @staticmethod
    def parse_string_param_if_env(string_param: str, **kwargs):
        """Use $ENV{env_name} to load environment variable "env_name"."""
        if "$ENV" in string_param:
            key = re.findall(r"\$ENV\{(.*?)\}", string_param)[0]
            env_value = os.environ.get(key, "$%s" % key)
            string_param = string_param.replace("$ENV{%s}" % key, env_value)
        return string_param

    @staticmethod
    def parse_string_param_if_config(string_param: str, **kwargs):
        """Use ${CONFIG.var_name} to load variable "var_name" from configuration."""
        if "${CONFIG" in string_param:
            key = re.findall(r"\$\{CONFIG\.(.*?)\}", string_param)[0]
            if hasattr(configuration, key):
                string_param = getattr(configuration, key)
            else:
                string_param = configuration.get_single_config(key)

        return string_param

    @staticmethod
    def get_possible_path(file_path, base_folder):
        """Get file possible path.

        Return new path if file_path is not exists, but base_folder + file_path exists
        """
        possible_path = file_path
        if not Path(file_path).exists():
            new_path = Path(base_folder).joinpath(file_path)
            if new_path.exists():
                possible_path = new_path
                logger.info(f"{file_path} not exists, convert to {possible_path}")

        return possible_path


def get_task_cls(task_type) -> Task:
    """Get the task class object by task_type (case compatible)."""
    # only get task class from tasks.__all__
    all_task_types = {type_.capitalize(): type_ for type_ in tasks.__all__}
    task_type_cap = task_type.capitalize()
    if task_type_cap not in all_task_types:
        raise PyDSTaskNoFoundException("cant not find task %s" % task_type)

    standard_name = all_task_types[task_type_cap]
    return getattr(tasks, standard_name)


class YamlProcess(YamlParser):
    """Yaml parser for create process.

    :param yaml_file: yaml file path.

        examples1 ::

            parser = YamlParser(yaml_file=...)
            parser.create_process_definition()

        examples2 ::

            YamlParser(yaml_file=...).create_process_definition()

    """

    _parse_rules = [
        ParseTool.parse_string_param_if_file,
        ParseTool.parse_string_param_if_env,
        ParseTool.parse_string_param_if_config,
    ]

    def __init__(self, yaml_file: str):
        with open(yaml_file, "r") as f:
            content = f.read()

        self._base_folder = Path(yaml_file).parent
        content = self.prepare_refer_process(content)
        super().__init__(content)

    def create_process_definition(self):
        """Create process main function."""
        # get process parameters with key "workflow"
        process_params = self[KEY_PROCESS]

        # pop "run" parameter, used at the end
        is_run = process_params.pop("run", False)

        # use YamlProcess._parse_rules to parse special value of yaml file
        process_params = self.parse_params(process_params)

        process_name = process_params["name"]
        logger.info(f"Create Process: {process_name}")
        with ProcessDefinition(**process_params) as pd:

            # save dependencies between tasks
            dependencies = {}

            # save name and task mapping
            name2task = {}

            # get task datas with key "tasks"
            for task_data in self[KEY_TASK]:
                task = self.parse_task(task_data, name2task)

                deps = task_data.get(KEY_DEPS, [])
                if deps:
                    dependencies[task.name] = deps
                name2task[task.name] = task

            # build dependencies between task
            for downstream_task_name, deps in dependencies.items():
                downstream_task = name2task[downstream_task_name]
                for upstream_task_name in deps:
                    upstream_task = name2task[upstream_task_name]
                    upstream_task >> downstream_task

            pd.submit()
            # if set is_run, run the process after submit
            if is_run:
                logger.info(f"run workflow: {pd}")
                pd.run()

        return process_name

    def parse_params(self, params: Any):
        """Recursively resolves the parameter values.

        The function operates params only when it encounters a string; other types continue recursively.
        """
        if isinstance(params, str):
            for parse_rule in self._parse_rules:
                params_ = params
                params = parse_rule(params, base_folder=self._base_folder)
                if params_ != params:
                    logger.info(f"parse {params_} -> {params}")

        elif isinstance(params, list):
            for index in range(len(params)):
                params[index] = self.parse_params(params[index])

        elif isinstance(params, dict):
            for key, value in params.items():
                params[key] = self.parse_params(value)

        return params

    @classmethod
    def parse(cls, yaml_file: str):
        """Recursively resolves the parameter values.

        The function operates params only when it encounters a string; other types continue recursively.
        """
        process_name = cls(yaml_file).create_process_definition()
        return process_name

    def prepare_refer_process(self, content):
        """Allow YAML files to reference process derived from other YAML files."""
        process_paths = re.findall(r"\$WORKFLOW\{\"(.*?)\"\}", content)
        for process_path in process_paths:
            logger.info(
                f"find special token {process_path}, load process form {process_path}"
            )
            possible_path = ParseTool.get_possible_path(process_path, self._base_folder)
            process_name = YamlProcess.parse(possible_path)
            content = content.replace('$WORKFLOW{"%s"}' % process_path, process_name)

        return content

    def parse_task(self, task_data: dict, name2task: Dict[str, Task]):
        """Parse various types of tasks.

        :param task_data: dict.
                {
                    "task_type": "Shell",
                    "params": {"name": "shell_task", "command":"ehco hellp"}
                }

        :param name2task: Dict[str, Task]), mapping of task_name and task


        Some task type have special parse func:
            if task type is Switch, use parse_switch;
            if task type is Condition, use parse_condition;
            if task type is Dependent, use parse_dependent;
            other, we pass all task_params as input to task class, like "task_cls(**task_params)".
        """
        task_type = task_data["task_type"]
        # get params without special key
        task_params = {k: v for k, v in task_data.items() if k not in TASK_SPECIAL_KEYS}

        task_cls = get_task_cls(task_type)

        # use YamlProcess._parse_rules to parse special value of yaml file
        task_params = self.parse_params(task_params)

        if task_cls == tasks.Switch:
            task = self.parse_switch(task_params, name2task)

        elif task_cls == tasks.Condition:
            task = self.parse_condition(task_params, name2task)

        elif task_cls == tasks.Dependent:
            task = self.parse_dependent(task_params, name2task)

        else:
            task = task_cls(**task_params)
        logger.info(task_type, task)
        return task

    def parse_switch(self, task_params, name2task):
        """Parse Switch Task.

        This is an example Yaml fragment of task_params

        name: switch
        condition:
          - ["${var} > 1", switch_child_1]
          - switch_child_2
        """
        from pydolphinscheduler.tasks.switch import (
            Branch,
            Default,
            Switch,
            SwitchCondition,
        )

        condition_datas = task_params["condition"]
        conditions = []
        for condition_data in condition_datas:
            assert "task" in condition_data, "task must be in %s" % condition_data
            task_name = condition_data["task"]
            condition_string = condition_data.get("condition", None)

            # if condition_string is None, for example: {"task": "switch_child_2"}, set it to Default branch
            if condition_string is None:
                conditions.append(Default(task=name2task.get(task_name)))

            # if condition_string is not None, for example:
            # {"task": "switch_child_2", "condition": "${var} > 1"} set it to Branch
            else:
                conditions.append(
                    Branch(condition_string, task=name2task.get(task_name))
                )

        switch = Switch(
            name=task_params["name"], condition=SwitchCondition(*conditions)
        )
        return switch

    def parse_condition(self, task_params, name2task):
        """Parse Condition Task.

        This is an example Yaml fragment of task_params

        name: condition
        success_task: success_branch
        failed_task: fail_branch
        OP: AND
        groups:
          -
            OP: AND
            groups:
              - [pre_task_1, true]
              - [pre_task_2, true]
              - [pre_task_3, false]
          -
            OP: AND
            groups:
              - [pre_task_1, false]
              - [pre_task_2, true]
              - [pre_task_3, true]

        """
        from pydolphinscheduler.tasks.condition import (
            FAILURE,
            SUCCESS,
            And,
            Condition,
            Or,
        )

        def get_op_cls(op):
            cls = None
            if op.lower() == "and":
                cls = And
            elif op.lower() == "or":
                cls = Or
            else:
                raise Exception("OP must be in And or Or, but get: %s" % op)
            return cls

        second_cond_ops = []
        for first_group in task_params["groups"]:
            second_op = first_group["op"]
            task_ops = []
            for condition_data in first_group["groups"]:
                assert "task" in condition_data, "task must be in %s" % condition_data
                assert "flag" in condition_data, "flag must be in %s" % condition_data
                task_name = condition_data["task"]
                flag = condition_data["flag"]
                task = name2task[task_name]

                # for example: task = pre_task_1, flag = true
                if flag:
                    task_ops.append(SUCCESS(task))
                else:
                    task_ops.append(FAILURE(task))

            second_cond_ops.append(get_op_cls(second_op)(*task_ops))

        first_op = task_params["op"]
        cond_operator = get_op_cls(first_op)(*second_cond_ops)

        condition = Condition(
            name=task_params["name"],
            condition=cond_operator,
            success_task=name2task[task_params["success_task"]],
            failed_task=name2task[task_params["failed_task"]],
        )
        return condition

    def parse_dependent(self, task_params, name2task):
        """Parse Dependent Task.

        This is an example Yaml fragment of task_params

        name: dependent
        denpendence:
        OP: AND
        groups:
          -
            OP: Or
            groups:
              - [pydolphin, task_dependent_external, task_1]
              - [pydolphin, task_dependent_external, task_2]
          -
            OP: And
            groups:
              - [pydolphin, task_dependent_external, task_1, LAST_WEDNESDAY]
              - [pydolphin, task_dependent_external, task_2, last24Hours]

        """
        from pydolphinscheduler.tasks.dependent import (
            And,
            Dependent,
            DependentDate,
            DependentItem,
            Or,
        )

        def process_dependent_date(dependent_date):
            """Parse dependent date (Compatible with key and value of DependentDate)."""
            dependent_date_upper = dependent_date.upper()
            if hasattr(DependentDate, dependent_date_upper):
                dependent_date = getattr(DependentDate, dependent_date_upper)
            return dependent_date

        def get_op_cls(op):
            cls = None
            if op.lower() == "and":
                cls = And
            elif op.lower() == "or":
                cls = Or
            else:
                raise Exception("OP must be in And or Or, but get: %s" % op)
            return cls

        def create_dependent_item(source_items):
            """Parse dependent item.

            project_name: pydolphin
            process_definition_name: task_dependent_external
            dependent_task_name: task_1
            dependent_date: LAST_WEDNESDAY
            """
            project_name = source_items["project_name"]
            process_definition_name = source_items["process_definition_name"]
            dependent_task_name = source_items["dependent_task_name"]
            dependent_date = source_items.get("dependent_date", DependentDate.TODAY)
            dependent_item = DependentItem(
                project_name=project_name,
                process_definition_name=process_definition_name,
                dependent_task_name=dependent_task_name,
                dependent_date=process_dependent_date(dependent_date),
            )

            return dependent_item

        second_dependences = []
        for first_group in task_params["groups"]:
            second_op = first_group[KEY_OP]
            dependence_items = []
            for source_items in first_group["groups"]:
                dependence_items.append(create_dependent_item(source_items))

            second_dependences.append(get_op_cls(second_op)(*dependence_items))

        first_op = task_params[KEY_OP]
        dependence = get_op_cls(first_op)(*second_dependences)

        task = Dependent(
            name=task_params["name"],
            dependence=dependence,
        )
        return task


def create_process_definition(yaml_file):
    """CLI."""
    YamlProcess.parse(yaml_file)
