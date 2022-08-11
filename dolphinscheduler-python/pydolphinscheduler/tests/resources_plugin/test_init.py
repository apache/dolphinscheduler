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

"""Test ResourcePlugin."""
from collections import Counter
from pathlib import Path

import pytest

from pydolphinscheduler.constants import ResourcePluginType
from pydolphinscheduler.exceptions import PyDSConfException, PyResPluginException
from pydolphinscheduler.resources_plugin import ResourcePlugin

all_res = ["local"]
project_root = Path(__file__).parent.parent.parent
resources_plugin_path = project_root.joinpath(
    "src", "pydolphinscheduler", "resources_plugin"
)


@pytest.mark.parametrize(
    "attr, expected", [({"type": "res_type", "prefix": "res_prefix",}, all_res)],
)
def test_resources_get_all_modules(attr, expected):
    """Test resource plugin to get all res plugin names"""
    res = ResourcePlugin(**attr)
    assert dict(Counter(expected)) == dict(
        Counter([ex.stem for ex in res.get_all_modules()])
    )


@pytest.mark.parametrize(
    "attrs, expected",
    [
        (
            {
                "type": ResourcePluginType.LOCAL,
                "module_attr": {
                    "script_name": "local.py",
                    "script_path": resources_plugin_path.joinpath("local.py"),
                },
            },
            "Local",
        ),
    ],
)
def test_resources_import_modules(attrs, expected):
    """Test resource plug-in to import model"""
    res_plugin = ResourcePlugin(attrs.get("type"), "plugin-prefix")
    res = res_plugin.import_module(**attrs.get("module_attr"))
    assert expected == res.__class__.__name__


@pytest.mark.parametrize(
    "attr, expected", [(ResourcePluginType.LOCAL, "Local"),],
)
def test_resources_resources(attr, expected):
    """Test resource plugin factory"""
    res_plugin = ResourcePlugin(attr, "/tmp/")
    res = res_plugin.resource
    assert expected == res.__class__.__name__


@pytest.mark.parametrize(
    "attr", [{"type": "a", "prefix": "/tmp/",}],
)
def test_resources_unsupported_res(attr):
    """Test unsupported plug-ins"""
    with pytest.raises(
        PyResPluginException, match="{} type is not supported".format(attr.get("type"))
    ):
        res_plugin = ResourcePlugin(**attr)
        res_plugin.resource()
