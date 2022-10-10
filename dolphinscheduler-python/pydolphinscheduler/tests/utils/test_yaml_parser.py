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

"""Test utils.path_dict module."""

from typing import Dict

import pytest
from ruamel.yaml import YAML

from pydolphinscheduler.utils.yaml_parser import YamlParser
from tests.testing.path import path_default_config_yaml

yaml = YAML()

expects = [
    {
        # yaml.load("no need test") is a flag about skipping it because it to different to maintainer
        "name": yaml.load("no need test"),
        "name.family": ("Smith", "SmithEdit"),
        "name.given": ("Alice", "AliceEdit"),
        "name.mark": yaml.load("no need test"),
        "name.mark.name_mark": yaml.load("no need test"),
        "name.mark.name_mark.key": ("value", "valueEdit"),
    },
    {
        # yaml.load("no need test") is a flag about skipping it because it to different to maintainer
        "java_gateway": yaml.load("no need test"),
        "java_gateway.address": ("127.0.0.1", "127.1.1.1"),
        "java_gateway.port": (25333, 25555),
        "java_gateway.auto_convert": (True, False),
        "default": yaml.load("no need test"),
        "default.user": yaml.load("no need test"),
        "default.user.name": ("userPythonGateway", "userPythonGatewayEdit"),
        "default.user.password": ("userPythonGateway", "userPythonGatewayEdit"),
        "default.user.email": (
            "userPythonGateway@dolphinscheduler.com",
            "userEdit@dolphinscheduler.com",
        ),
        "default.user.tenant": ("tenant_pydolphin", "tenant_pydolphinEdit"),
        "default.user.phone": (11111111111, 22222222222),
        "default.user.state": (1, 0),
        "default.workflow": yaml.load("no need test"),
        "default.workflow.project": ("project-pydolphin", "project-pydolphinEdit"),
        "default.workflow.tenant": ("tenant_pydolphin", "SmithEdit"),
        "default.workflow.user": ("userPythonGateway", "SmithEdit"),
        "default.workflow.queue": ("queuePythonGateway", "queueEdit"),
        "default.workflow.worker_group": ("default", "wgEdit"),
        "default.workflow.release_state": ("online", "offline"),
        "default.workflow.time_zone": ("Asia/Shanghai", "Europe/Amsterdam"),
        "default.workflow.warning_type": ("NONE", "SUCCESS"),
    },
]

param = [
    """#example
name:
  # details
  family: Smith   # very common
  given: Alice    # one of the siblings
  mark:
    name_mark:
      key: value
"""
]

with open(path_default_config_yaml, "r") as f:
    param.append(f.read())


@pytest.mark.parametrize(
    "src, delimiter, expect",
    [
        (
            param[0],
            "|",
            expects[0],
        ),
        (
            param[1],
            "/",
            expects[1],
        ),
    ],
)
def test_yaml_parser_specific_delimiter(src: str, delimiter: str, expect: Dict):
    """Test specific delimiter for :class:`YamlParser`."""

    def ch_dl(key):
        return key.replace(".", delimiter)

    yaml_parser = YamlParser(src, delimiter=delimiter)
    assert all(
        [
            expect[key][0] == yaml_parser[ch_dl(key)]
            for key in expect
            if expect[key] != "no need test"
        ]
    )
    assert all(
        [
            expect[key][0] == yaml_parser.get(ch_dl(key))
            for key in expect
            if expect[key] != "no need test"
        ]
    )


@pytest.mark.parametrize(
    "src, expect",
    [
        (
            param[0],
            expects[0],
        ),
        (
            param[1],
            expects[1],
        ),
    ],
)
def test_yaml_parser_contains(src: str, expect: Dict):
    """Test magic function :func:`YamlParser.__contain__` also with `key in obj` syntax."""
    yaml_parser = YamlParser(src)
    assert len(expect.keys()) == len(
        yaml_parser.dict_parser.keys()
    ), "Parser keys length not equal to expect keys length"
    assert all(
        [key in yaml_parser for key in expect]
    ), "Parser keys not equal to expect keys"


@pytest.mark.parametrize(
    "src, expect",
    [
        (
            param[0],
            expects[0],
        ),
        (
            param[1],
            expects[1],
        ),
    ],
)
def test_yaml_parser_get(src: str, expect: Dict):
    """Test magic function :func:`YamlParser.__getitem__` also with `obj[key]` syntax."""
    yaml_parser = YamlParser(src)
    assert all(
        [
            expect[key][0] == yaml_parser[key]
            for key in expect
            if expect[key] != "no need test"
        ]
    )
    assert all(
        [
            expect[key][0] == yaml_parser.get(key)
            for key in expect
            if expect[key] != "no need test"
        ]
    )


@pytest.mark.parametrize(
    "src, expect",
    [
        (
            param[0],
            expects[0],
        ),
        (
            param[1],
            expects[1],
        ),
    ],
)
def test_yaml_parser_set(src: str, expect: Dict):
    """Test magic function :func:`YamlParser.__setitem__` also with `obj[key] = val` syntax."""
    yaml_parser = YamlParser(src)
    for key in expect:
        assert key in yaml_parser.dict_parser.keys()
        if expect[key] == "no need test":
            continue
        assert expect[key][0] == yaml_parser.dict_parser[key]
        assert expect[key][1] != yaml_parser.dict_parser[key]

        yaml_parser[key] = expect[key][1]
        assert expect[key][0] != yaml_parser.dict_parser[key]
        assert expect[key][1] == yaml_parser.dict_parser[key]


@pytest.mark.parametrize(
    "src, setter, expect",
    [
        (
            param[0],
            {"name.mark.name_mark.key": "edit"},
            """#example
name:
  # details
  family: Smith   # very common
  given: Alice    # one of the siblings
  mark:
    name_mark:
      key: edit
""",
        ),
        (
            param[0],
            {
                "name.family": "SmithEdit",
                "name.given": "AliceEdit",
                "name.mark.name_mark.key": "edit",
            },
            """#example
name:
  # details
  family: SmithEdit # very common
  given: AliceEdit # one of the siblings
  mark:
    name_mark:
      key: edit
""",
        ),
    ],
)
def test_yaml_parser_str_repr(src: str, setter: Dict, expect: str):
    """Test function :func:`YamlParser.to_string`."""
    yaml_parser = YamlParser(src)

    # Equal before change
    assert f"YamlParser({src})" == repr(yaml_parser)
    assert src == str(yaml_parser)

    for key, val in setter.items():
        yaml_parser[key] = val

    # Equal after changed
    assert expect == str(yaml_parser)
    assert f"YamlParser({expect})" == repr(yaml_parser)
