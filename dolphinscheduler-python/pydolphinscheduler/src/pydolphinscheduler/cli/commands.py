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

"""Commands line interface's command of pydolphinscheduler."""

import click
from click import echo

import pydolphinscheduler
from pydolphinscheduler.configuration import (
    get_single_config,
    init_config_file,
    set_single_config,
)
from pydolphinscheduler.core.yaml_process_define import create_process_definition
from pydolphinscheduler.models import User, Tenant, Project

version_option_val = ["major", "minor", "micro"]


@click.group()
def cli():
    """Apache DolphinScheduler Python API's command line interface."""


@cli.command()
@click.option(
    "--part",
    "-p",
    required=False,
    type=click.Choice(version_option_val, case_sensitive=False),
    multiple=False,
    help="The part of version your want to get.",
)
def version(part: str) -> None:
    """Show current version of pydolphinscheduler."""
    if part:
        idx = version_option_val.index(part)
        echo(f"{pydolphinscheduler.__version__.split('.')[idx]}")
    else:
        echo(f"{pydolphinscheduler.__version__}")


@cli.command()
@click.option(
    "--init",
    "-i",
    is_flag=True,
    help="Initialize and create configuration file to `PYDS_HOME`.",
)
@click.option(
    "--set",
    "-s",
    "setter",
    multiple=True,
    type=click.Tuple([str, str]),
    help="Set specific setting to config file."
    "Use multiple ``--set <KEY> <VAL>`` options to set multiple configs",
)
@click.option(
    "--get",
    "-g",
    "getter",
    multiple=True,
    type=str,
    help="Get specific setting from config file."
    "Use multiple ``--get <KEY>`` options to get multiple configs",
)
def config(getter, setter, init) -> None:
    """Manage the configuration for pydolphinscheduler."""
    if init:
        init_config_file()
    elif getter:
        click.echo("The configuration query as below:\n")
        configs_kv = [f"{key} = {get_single_config(key)}" for key in getter]
        click.echo("\n".join(configs_kv))
    elif setter:
        for key, val in setter:
            set_single_config(key, val)
        click.echo("Set configuration done.")


@cli.command()
@click.option(
    "--setter",
    "-s",
    multiple=True,
    type=click.Tuple([str, str, str, str, str, str, int]),
    help="Set user to pydolphinscheduler."
    "Use multiple ``--set <NAME> <PASSWORD> <EMAIL> <PHONE> <TENANT> <QUEUE> <STATUS>`` options to set multiple users",
)
@click.option(
    "--getter",
    "-g",
    multiple=True,
    type=int,
    help="Get user from pydolphinscheduler."
    "Use multiple ``--get <USER_ID>`` options to get multiple users",
)
@click.option(
    "--updater",
    "-u",
    multiple=True,
    type=click.Tuple([str, str, str, str, str, str, int]),
    help="Update user to pydolphinscheduler."
    "Use multiple ``--update <NAME> <PASSWORD> <EMAIL> <PHONE> <TENANT> <QUEUE> <STATUS>`` options to update "
         "multiple users",
)
@click.option(
    "--deleter",
    "-d",
    multiple=True,
    type=int,
    help="Delete user from pydolphinscheduler."
    "Use multiple ``--delete <USER_ID>`` options to delete multiple users",
)
def user(getter, setter, updater, deleter) -> None:
    """Manage the user of pydolphinscheduler."""
    if getter:
        click.echo(f"Get user {getter} from pydolphinscheduler.")
        for user_id in getter:
            user_ = User.get_user(user_id)
            click.echo(user_)
    elif setter:
        click.echo("Set user start.")
        user_ = User(setter[0], setter[1], setter[2], setter[3], setter[4], setter[5], setter[6])
        user_.create_if_not_exists()
        click.echo(user_)
        click.echo("Set user done.")
    elif updater:
        click.echo("Update user start.")
        user_ = User(updater[0])
        user_.update(updater[1], updater[2], updater[3], updater[4], updater[5], updater[6])
        click.echo(user_)
        click.echo("Update user done.")
    elif deleter:
        click.echo(f"Delete user {deleter} from pydolphinscheduler.")
        for user_id in deleter:
            user_ = User.get_user(user_id)
            user_.delete()
            click.echo(f"Delete user {user_id} done.")


@cli.command()
@click.option(
    "--setter",
    "-s",
    multiple=True,
    type=click.Tuple([str, str, str, str, str]),
    help="Set tenant to pydolphinscheduler."
    "Use multiple ``--set <NAME> <QUEUE> <DESCRIPTION> <TENANT_CODE> <USER_NAME>`` options to set multiple tenants",
)
@click.option(
    "--getter",
    "-g",
    multiple=True,
    type=str,
    help="Get tenant from pydolphinscheduler."
    "Use multiple ``--get <TENANT_CODE>`` options to get multiple tenants",
)
@click.option(
    "--updater",
    "-u",
    multiple=True,
    type=click.Tuple([str, str, int, str]),
    help="Update tenant to pydolphinscheduler."
    "Use multiple ``--update <USER_NAME> <TENANT_CODE> <QUEUE_ID> <DESCRIPTION>`` options to update "
)
@click.option(
    "--deleter",
    "-d",
    multiple=True,
    type=str,
    help="Delete tenant from pydolphinscheduler."
    "Use multiple ``--delete <TENANT_CODE>`` options to delete multiple tenants",
)
def tenant(getter, setter, updater, deleter) -> None:
    """Manage the tenant of pydolphinscheduler."""
    if getter:
        click.echo(f"Get tenant {getter} from pydolphinscheduler.")
        for tenant_code in getter:
            tenant_ = Tenant.get_tenant(tenant_code)
            click.echo(tenant_)
    elif setter:
        click.echo("Set tenant start.")
        tenant_ = Tenant(setter[0], setter[1], setter[2], setter[3], setter[4])
        tenant_.create_if_not_exists(setter[0])
        click.echo(tenant_)
        click.echo("Set tenant done.")
    elif updater:
        click.echo("Update tenant start.")
        tenant_ = Tenant(updater[0])
        tenant_.update(updater[0], updater[1], updater[2], updater[3])
        click.echo(tenant_)
        click.echo("Update tenant done.")
    elif deleter:
        click.echo(f"Delete tenant {deleter} from pydolphinscheduler.")
        for tenant_code in deleter:
            tenant_ = Tenant.get_tenant(tenant_code)
            tenant_.delete()
            click.echo(f"Delete tenant {tenant_code} done.")


@cli.command()
@click.option(
    "--setter",
    "-s",
    multiple=True,
    type=click.Tuple([str, str]),
    help="Set project to pydolphinscheduler."
    "Use multiple ``--set <NAME> <DESCRIPTION>`` options to set multiple projects",
)
@click.option(
    "--getter",
    "-g",
    multiple=True,
    type=str,
    help="Get project from pydolphinscheduler."
    "Use multiple ``--get <PROJECT_NAME>`` options to get multiple projects",
)
@click.option(
    "--updater",
    "-u",
    multiple=True,
    type=click.Tuple([str, str, str, str]),
    help="Update project to pydolphinscheduler."
    "Use multiple ``--update <USER> <PROJECT_CODE> <PROJECT_NAME> <DESCRIPTION>`` options to update multiple projects",
)
@click.option(
    "--deleter",
    "-d",
    multiple=True,
    type=int,
    help="Delete project from pydolphinscheduler."
    "Use multiple ``--delete <PROJECT_CODE>`` options to delete multiple projects",
)
def project(getter, setter, updater, deleter) -> None:
    """Manage the project of pydolphinscheduler."""
    if getter:
        click.echo(f"Get project {getter} from pydolphinscheduler.")
        for project_name in getter:
            project_ = Project.get_project_by_name(project_name)
            click.echo(project_)
    elif setter:
        click.echo("Set project start.")
        project_ = Project(setter[0], setter[1])
        project_.create_if_not_exists()
        click.echo(project_)
        click.echo("Set project done.")
    elif updater:
        click.echo("Update project start.")
        project_ = Project()
        project_.update(updater[0], updater[1], updater[2], updater[3])
        click.echo(project_)
        click.echo("Update project done.")
    elif deleter:
        click.echo(f"Delete project {deleter} from pydolphinscheduler.")
        for project_name in deleter:
            project_ = Project.get_project_by_name(project_name)
            project_.delete()
            click.echo(f"Delete project {project_name} done.")


@cli.command()
@click.option(
    "--yaml_file",
    "-f",
    required=True,
    help="YAML file path",
    type=click.Path(exists=True),
)
def yaml(yaml_file) -> None:
    """Create process definition using YAML file."""
    create_process_definition(yaml_file)
