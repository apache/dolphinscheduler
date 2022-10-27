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
from pydolphinscheduler.models import Project, Tenant, User

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
    "--set",
    "-s",
    "setter",
    is_flag=True,
    help="Set user to pydolphinscheduler."
    "Use ``--set --name <NAME> --password <PASSWORD> --email <EMAIL> --phone <PHONE> --tenant <TENANT>"
    "--queue <QUEUE> --status <STATUS>`` options to set user",
)
@click.option(
    "--get",
    "-g",
    "getter",
    is_flag=True,
    help="Get user from pydolphinscheduler."
    "Use ``--get --user_id <USER_ID>`` options to get user",
)
@click.option(
    "--update",
    "-u",
    "updater",
    is_flag=True,
    help="Update user to pydolphinscheduler."
    "Use ``--update --name <NAME> --password <PASSWORD> --email <EMAIL> --phone <PHONE> --tenant <TENANT>"
    " --status <STATUS>`` options to update user",
)
@click.option(
    "--delete",
    "-d",
    "deleter",
    is_flag=True,
    help="Delete user from pydolphinscheduler."
    "Use ``--delete --user_id <USER_ID>`` options to delete user",
)
@click.option(
    "--user_id",
    multiple=False,
    type=int,
    help="<USER_ID>",
)
@click.option(
    "--name",
    multiple=False,
    type=str,
    help="<NAME>",
)
@click.option(
    "--password",
    multiple=False,
    type=str,
    help="<PASSWORD>",
)
@click.option(
    "--email",
    multiple=False,
    type=str,
    help="<EMAIL>",
)
@click.option(
    "--phone",
    multiple=False,
    type=str,
    help="<PHONE>",
)
@click.option(
    "--tenant",
    multiple=False,
    type=str,
    help="<TENANT>",
)
@click.option(
    "--queue",
    multiple=False,
    type=str,
    help="<QUEUE>",
)
@click.option(
    "--status",
    multiple=False,
    type=int,
    help="<STATUS>",
)
def user(
    getter,
    setter,
    updater,
    deleter,
    user_id,
    name,
    password,
    email,
    phone,
    tenant,
    queue,
    status,
) -> None:
    """Manage the user of pydolphinscheduler."""
    if getter:
        click.echo(f"Get user {user_id} from pydolphinscheduler.")
        user_ = User.get_user(user_id)
        click.echo(user_)
    elif setter:
        click.echo("Set user start.")
        user_ = User(name, password, email, phone, tenant, queue, status)
        user_.create_if_not_exists()
        click.echo(user_)
        click.echo("Set user done.")
    elif updater:
        click.echo("Update user start.")
        user_ = User(name)
        user_.update(password, email, phone, tenant, queue, status)
        click.echo(user_)
        click.echo("Update user done.")
    elif deleter:
        click.echo(f"Delete user {user_id} from pydolphinscheduler.")
        user_ = User.get_user(user_id)
        user_.delete()
        click.echo(f"Delete user {user_id} done.")


@cli.command()
@click.option(
    "--set",
    "-s",
    "setter",
    is_flag=True,
    help="Set tenant to pydolphinscheduler."
    "Use ``--set --name <NAME> --queue <QUEUE> --description <DESCRIPTION> --tenant_code <TENANT_CODE>"
    "--user_name <USER_NAME>`` options to set tenant",
)
@click.option(
    "--get",
    "-g",
    "getter",
    is_flag=True,
    help="Get tenant from pydolphinscheduler."
    "Use ``--get --tenant_code <TENANT_CODE>`` options to get tenant",
)
@click.option(
    "--update",
    "-u",
    "updater",
    is_flag=True,
    help="Update tenant to pydolphinscheduler."
    "Use ``--update --user_name <USER_NAME> --tenant_code <TENANT_CODE> --queue <QUEUE>"
    "--description <DESCRIPTION>`` options to update tenant",
)
@click.option(
    "--delete",
    "-d",
    "deleter",
    is_flag=True,
    help="Delete tenant from pydolphinscheduler."
    "Use ``--delete --user_name <USER_NAME> --tenant_code <TENANT_CODE>`` options to delete tenant",
)
@click.option(
    "--name",
    multiple=False,
    type=str,
    help="<NAME>",
)
@click.option(
    "--queue",
    multiple=False,
    type=str,
    help="<QUEUE>",
)
@click.option(
    "--description",
    multiple=False,
    type=str,
    help="<DESCRIPTION>",
)
@click.option(
    "--tenant_code",
    multiple=False,
    type=str,
    help="<TENANT_CODE>",
)
@click.option(
    "--user_name",
    multiple=False,
    type=str,
    help="<USER_NAME>",
)
def tenant(
    getter, setter, updater, deleter, name, queue, description, tenant_code, user_name
) -> None:
    """Manage the tenant of pydolphinscheduler."""
    if getter:
        click.echo(f"Get tenant {tenant_code} from pydolphinscheduler.")
        tenant_ = Tenant.get_tenant(tenant_code)
        click.echo(tenant_)
    elif setter:
        click.echo("Set tenant start.")
        tenant_ = Tenant(
            name, queue, description, code=tenant_code, user_name=user_name
        )
        tenant_.create_if_not_exists(tenant_code)
        click.echo(tenant_)
        click.echo("Set tenant done.")
    elif updater:
        click.echo("Update tenant start.")
        tenant_ = Tenant.get_tenant(tenant_code)
        tenant_.update(user_name, tenant_code, queue, description)
        click.echo(tenant_)
        click.echo("Update tenant done.")
    elif deleter:
        click.echo(f"Delete tenant {tenant_code} from pydolphinscheduler.")
        tenant_ = Tenant.get_tenant(tenant_code)
        echo(tenant_)
        tenant_.delete(user_name)
        click.echo(f"Delete tenant {tenant_code} done.")


@cli.command()
@click.option(
    "--set",
    "-s",
    "setter",
    is_flag=True,
    help="Set project to pydolphinscheduler."
    "Use ``--set --project_name <PROJECT_NAME> --description <DESCRIPTION> --user_name <USER_NAME>",
)
@click.option(
    "--get",
    "-g",
    "getter",
    is_flag=True,
    help="Get project from pydolphinscheduler."
    "Use ``--get --user_name <USER_NAME> --project_name <PROJECT_NAME>`` options to get project",
)
@click.option(
    "--update",
    "-u",
    "updater",
    is_flag=True,
    help="Update project to pydolphinscheduler."
    "Use ``--update --user_name <USER_NAME> --project_code <PROJECT_CODE> --project_name <PROJECT_NAME>"
    "--description <DESCRIPTION>`` options to update project",
)
@click.option(
    "--delete",
    "-d",
    "deleter",
    is_flag=True,
    help="Delete project from pydolphinscheduler."
    "Use ``--delete --user_name <USER_NAME> --project_name <PROJECT_NAME>`` options to delete project",
)
@click.option(
    "--description",
    multiple=False,
    type=str,
    help="<DESCRIPTION>",
)
@click.option(
    "--user_name",
    multiple=False,
    type=str,
    help="<USER_NAME>",
)
@click.option(
    "--project_code",
    multiple=False,
    type=int,
    help="<PROJECT_CODE>",
)
@click.option(
    "--project_name",
    multiple=False,
    type=str,
    help="<PROJECT_NAME>",
)
def project(
    getter, setter, updater, deleter, description, user_name, project_code, project_name
) -> None:
    """Manage the project of pydolphinscheduler."""
    if getter:
        click.echo(f"Get project {project_name} from pydolphinscheduler.")
        project_ = Project.get_project_by_name(user_name, project_name)
        click.echo(project_)
    elif setter:
        click.echo("Set project start.")
        project_ = Project(project_name, description)
        project_.create_if_not_exists(user_name)
        click.echo(project_)
        click.echo("Set project done.")
    elif updater:
        click.echo("Update project start.")
        project_ = Project()
        project_.update(user_name, project_code, project_name, description)
        click.echo(project_)
        click.echo("Update project done.")
    elif deleter:
        click.echo(f"Delete project {project_name} from pydolphinscheduler.")
        project_ = Project.get_project_by_name(user_name, project_name)
        click.echo(project_)
        project_.delete(user_name)
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
