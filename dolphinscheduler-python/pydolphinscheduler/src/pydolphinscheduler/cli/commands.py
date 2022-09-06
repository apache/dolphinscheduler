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
    "--yaml_file",
    "-f",
    required=True,
    help="YAML file path",
    type=click.Path(exists=True),
)
def yaml(yaml_file) -> None:
    """Create process definition using YAML file."""
    create_process_definition(yaml_file)
